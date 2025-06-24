package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.enums.MessageType;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.dto.MessageOperateDTO;
import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mongo.service.MongoMessageService;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.PrivateMessageService;
import com.bx.implatform.service.UserBlacklistService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.MessageIdGenerator;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.MessageDeleteNoticeVO;
import com.bx.implatform.vo.PrivateMessageVO;
import com.bx.implatform.vo.QuoteMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 私聊消息业务实现。
 * <p>
 * 主要职责：
 * <ul>
 *     <li>发送、撤回、编辑私聊消息</li>
 *     <li>查询聊天历史、拉取离线消息</li>
 *     <li>已读回执、删除/清空会话</li>
 *     <li>将业务逻辑与 MongoDB 存储、IM 推送打通</li>
 * </ul>
 * 此类依赖 {@link MongoMessageService} 完成数据落库，依赖 {@link IMClient} 完成即时推送。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl implements PrivateMessageService {

    // ========================= 依赖注入 ========================= //
    private final FriendService friendService;                // 好友关系检查
    private final UserBlacklistService userBlacklistService;  // 黑名单检查
    private final IMClient imClient;                          // IM 推送客户端
    private final SensitiveFilterUtil sensitiveFilterUtil;    // 敏感词过滤器
    private final MongoMessageService mongoMessageService;    // MongoDB 消息存储

    // 单例线程池：用于离线消息异步推送
    private static final ScheduledThreadPoolExecutor EXECUTOR = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    // ========================= 发送消息 ========================= //

    /**
     * 发送私聊消息：校验 => 持久化 => 推送
     *
     * @param dto 前端传入的消息数据
     * @return PrivateMessageVO 发送后的消息视图对象
     */
    @Override
    public PrivateMessageVO sendMessage(PrivateMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        // ---------- 0. 前置校验 ---------- //
        if (!friendService.isFriend(session.getUserId(), dto.getRecvId())) {
            throw new GlobalException("您已不是对方好友，无法发送消息");
        }
        if (userBlacklistService.isInBlacklist(dto.getRecvId(), session.getUserId())) {
            throw new GlobalException("对方已将您拉入黑名单，无法发送消息");
        }

        // ---------- 1. 构造并持久化消息 ---------- //
        PrivateMessage msg = new PrivateMessage();
        BeanUtils.copyProperties(dto, msg);
        msg.setSendId(session.getUserId());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendTime(new Date());
        // 文本消息做敏感词过滤
        if (MessageType.TEXT.code().equals(dto.getType())) {
            msg.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        }
        msg.setId(MessageIdGenerator.nextId());
        mongoMessageService.savePrivateMessage(msg);

        // ---------- 2. 组装推送 VO ---------- //
        PrivateMessageVO vo = new PrivateMessageVO();
        BeanUtils.copyProperties(msg, vo);
        // 如果引用了上一条消息，则补充 Quote 信息
        if (dto.getQuoteMessageId() != null) {
            PrivateMessage quote = mongoMessageService.findPrivateMessageById(dto.getQuoteMessageId());
            vo.setQuoteMessage(BeanUtils.copyProperties(quote, QuoteMessageVO.class));
        }

        // ---------- 3. 即时推送 ---------- //
        IMPrivateMessage<PrivateMessageVO> imMsg = new IMPrivateMessage<>();
        imMsg.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        imMsg.setRecvId(vo.getRecvId());
        imMsg.setSendToSelf(true);    // 自己多端也要同步
        imMsg.setSendResult(true);    // 告诉前端：已发送成功
        imMsg.setData(vo);
        imClient.sendPrivateMessage(imMsg);

        log.info("发送私聊消息，发送id:{}, 接收id:{}, 内容:{}", session.getUserId(), dto.getRecvId(), dto.getContent());
        return vo;
    }

    // ========================= 撤回消息 ========================= //

    /**
     * 撤回消息：仅允许发送者在固定时限内撤回
     */
    @Transactional
    @Override
    public PrivateMessageVO recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        PrivateMessage msg = mongoMessageService.findPrivateMessageById(id);
        if (msg == null) {
            throw new GlobalException("消息不存在");
        }
        // 只能撤回自己发送的消息
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException("这条消息不是由您发送,无法撤回");
        }
        // 超时限制：默认 5 分钟
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException("消息已发送超过5分钟，无法撤回");
        }

        // 1. 将原消息状态改为 RECALL
        msg.setStatus(MessageStatus.RECALL.code());
        mongoMessageService.updatePrivateMessage(msg);

        // 2. 生产一条新的 "撤回通知" 消息（方便多端同步）
        PrivateMessage recall = new PrivateMessage();
        recall.setSendId(session.getUserId());
        recall.setRecvId(msg.getRecvId());
        recall.setType(MessageType.RECALL.code());
        recall.setContent(id.toString());   // content 填原消息 id
        recall.setStatus(MessageStatus.UNSEND.code());
        recall.setSendTime(new Date());
        recall.setId(MessageIdGenerator.nextId());
        mongoMessageService.savePrivateMessage(recall);

        // 3. 推送撤回消息
        PrivateMessageVO vo = BeanUtils.copyProperties(recall, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> imMsg = new IMPrivateMessage<>();
        imMsg.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        imMsg.setRecvId(vo.getRecvId());
        imMsg.setData(vo);
        imClient.sendPrivateMessage(imMsg);

        log.info("撤回私聊消息，发送id:{}, 接收id:{}, 原消息:{}", msg.getSendId(), msg.getRecvId(), msg.getContent());
        return vo;
    }

    // ========================= 编辑消息 ========================= //

    /**
     * 编辑已发送消息：支持 60 分钟内修改内容
     */
    @Transactional
    @Override
    public PrivateMessageVO editMessage(PrivateMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        PrivateMessage msg = mongoMessageService.findPrivateMessageById(dto.getQuoteMessageId());
        if (msg == null) throw new GlobalException("消息不存在");
        if (!msg.getSendId().equals(session.getUserId())) throw new GlobalException("只能编辑自己发的消息");
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_Edit_RECALL_SECOND * 1000)
            throw new GlobalException("消息已发送超过60分钟，无法编辑");
        if (msg.getStatus() == MessageStatus.RECALL.code())
            throw new GlobalException("已撤回消息不能再编辑");

        // 1. 更新消息内容
        msg.setContentEdit(msg.getContent());                  // 记录旧内容（如需展示编辑历史）
        msg.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        msg.setType(dto.getType());
        msg.setQuoteMessageId(dto.getQuoteMessageId());
        mongoMessageService.updatePrivateMessage(msg);

        // 2. 推送编辑后的消息
        PrivateMessageVO vo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> imMsg = new IMPrivateMessage<>();
        imMsg.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        imMsg.setRecvId(vo.getRecvId());
        imMsg.setData(vo);
        imClient.sendPrivateMessage(imMsg);

        log.info("编辑私聊消息，消息id:{}, 新内容:{}", dto.getQuoteMessageId(), dto.getContent());
        return vo;
    }

    // ========================= 查询历史 ========================= //

    /**
     * 分页查询与好友的历史记录（倒序）。
     */
    @Override
    public List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page, Long size) {
        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long skip = (page - 1) * size;

        // 调用 MongoService 查询（已自动过滤自己删除的消息）
        List<PrivateMessage> rawMsgs = mongoMessageService.findPrivateHistory(userId, friendId, skip, size);

        // 转视图 + 加载引用消息
        return rawMsgs.stream().map(m -> {
                    PrivateMessageVO vo = BeanUtils.copyProperties(m, PrivateMessageVO.class);
                    if (m.getQuoteMessageId() != null) {
                        PrivateMessage quote = mongoMessageService.findPrivateMessageById(m.getQuoteMessageId());
                        vo.setQuoteMessage(BeanUtils.copyProperties(quote, QuoteMessageVO.class));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // ========================= 拉取离线消息 ========================= //

    /**
     * 客户端上线后拉离线消息（按 minId 增量）。
     * 注解 {@link OnlineCheck} 会在切面中校验用户是否在线，防止离线多拉。
     */
    @OnlineCheck
    @Override
    public void pullOfflineMessage(Long minId) {
        UserSession session = SessionContext.getSession();

        // 1. 仅拉最近 N 个月消息：APP 1 个月，Web 3 个月
        int months = session.getTerminal().equals(IMTerminalType.APP.code()) ? 1 : 3;
        Date minDate = DateUtils.addMonths(new Date(), -months);

        // 2. 查询 MongoDB
        List<PrivateMessage> messages = mongoMessageService.findPrivateMessages(session.getUserId(), minId, minDate);

        // 3. 批量加载引用消息，避免 N+1
        Map<Long, QuoteMessageVO> quoteMap = batchLoadQuoteMessage(messages);

        // 4. 异步逐条推送到客户端
        EXECUTOR.execute(() -> {
            sendLoadingMessage(true, session); // 前端显示 loading spinner
            for (PrivateMessage m : messages) {
                PrivateMessageVO vo = BeanUtils.copyProperties(m, PrivateMessageVO.class);
                vo.setQuoteMessage(quoteMap.get(m.getQuoteMessageId()));

                IMPrivateMessage<PrivateMessageVO> imMsg = new IMPrivateMessage<>();
                imMsg.setSender(new IMUserInfo(m.getSendId(), IMTerminalType.WEB.code()));
                imMsg.setRecvId(session.getUserId());
                imMsg.setRecvTerminals(List.of(session.getTerminal()));
                imMsg.setSendToSelf(false);
                imMsg.setSendResult(true);
                imMsg.setData(vo);
                imClient.sendPrivateMessage(imMsg);
            }
            sendLoadingMessage(false, session); // 关闭 loading
            log.info("拉取私聊消息，用户id:{}, 数量:{}", session.getUserId(), messages.size());
        });
    }

    // ========================= 已读回执 ========================= //

    /**
     * 将会话标记为已读，并向对方推送回执。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void readedMessage(Long friendId) {
        UserSession session = SessionContext.getSession();

        // 1. 给自己推送“清空未读计数”
        buildAndSendSystemMsg(MessageType.READED, session.getUserId(), friendId, true);

        // 2. 给对方推送“已收回执”
        buildAndSendSystemMsg(MessageType.RECEIPT, session.getUserId(), friendId, false);

        // 3. 更新数据库状态
        mongoMessageService.markPrivateMessagesRead(friendId, session.getUserId(),
                MessageStatus.SENDED.code(), MessageStatus.READED.code());

        log.info("消息已读，接收方id:{}, 发送方id:{}", session.getUserId(), friendId);
    }

    /**
     * 构造并发送系统回执类消息（已读 / 收条）。
     */
    private void buildAndSendSystemMsg(MessageType type, Long selfId, Long peerId, boolean toSelf) {
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setType(type.code());
        vo.setSendId(selfId);
        vo.setRecvId(peerId);

        IMPrivateMessage<PrivateMessageVO> imMsg = new IMPrivateMessage<>();
        imMsg.setData(vo);
        imMsg.setSender(new IMUserInfo(selfId, SessionContext.getSession().getTerminal()));
        imMsg.setRecvId(toSelf ? selfId : peerId);
        imMsg.setSendToSelf(toSelf);
        imMsg.setSendResult(false);
        imClient.sendPrivateMessage(imMsg);
    }

    // ========================= 删除 & 清空 ========================= //

    /**
     * 消息删除 / 清空（单向 or 双向）。
     */
    @Override
    @Transactional
    public void operate(MessageOperateDTO dto) {
        UserSession session = SessionContext.getSession();
        Long selfId = session.getUserId();
        boolean both = dto.isDeleteBoth();

        switch (dto.getAction()) {
            // -------- DELETE: 删除指定消息 -------- //
            case DELETE -> {
                List<Long> ids = dto.getIds();
                if (CollUtil.isEmpty(ids)) throw new GlobalException("ids 不能为空");

                mongoMessageService.updatePrivateMessagesStatus(ids, selfId, both);
                if (both) {
                    // 推送删除通知给对端
                    sendDeleteNotice(selfId, dto.getPeerId(), ids, false);
                }
            }
            // -------- CLEAR: 清空整个会话 -------- //
            case CLEAR -> {
                Long peerId = dto.getPeerId();
                if (peerId == null) throw new GlobalException("peerId 必填");

                mongoMessageService.clearConversation(selfId, peerId, both);
                if (both) {
                    sendDeleteNotice(selfId, peerId, null, true);
                }
            }
        }
    }

    /**
     * 构造并发送删除/清空通知。
     */
    private void sendDeleteNotice(Long selfId, Long peerId, List<Long> ids, boolean isClear) {
        IMPrivateMessage<Object> msg = new IMPrivateMessage<>();
        msg.setSender(new IMUserInfo(selfId, SessionContext.getSession().getTerminal()));
        msg.setRecvId(peerId);
        msg.setSendToSelf(true);
        msg.setSendResult(false);

        MessageDeleteNoticeVO body = new MessageDeleteNoticeVO();
        body.setSendId(selfId);
        body.setRecvId(peerId);
        body.setMsgIds(ids);
        body.setIsClear(isClear);
        body.setType(MessageType.DELETE_MESSAGE.code());
        msg.setData(body);

        imClient.sendPrivateMessage(msg);
    }

    // ========================= 其它辅助 ========================= //

    @Override
    public Long getMaxReadedId(Long friendId) {
        Long userId = SessionContext.getSession().getUserId();
        return mongoMessageService.getMaxReadedId(userId, friendId, MessageStatus.READED.code());
    }

    @Override
    public void markMessagesSendedIfUnsend(Set<Long> messageIds) {
        mongoMessageService.markMessagesSendedIfUnsend(messageIds, MessageStatus.UNSEND.code(), MessageStatus.SENDED.code());
    }

    @Override
    public void save(PrivateMessage msg) {
        mongoMessageService.savePrivateMessage(msg);
    }

    /**
     * 发送 Loading 提示给当前终端（开始/结束）
     */
    private void sendLoadingMessage(Boolean isLoading, UserSession session) {
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setType(MessageType.LOADING.code());
        vo.setContent(isLoading.toString());

        IMPrivateMessage<PrivateMessageVO> imMsg = new IMPrivateMessage<>();
        imMsg.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        imMsg.setRecvId(session.getUserId());
        imMsg.setRecvTerminals(List.of(session.getTerminal()));
        imMsg.setData(vo);
        imMsg.setSendToSelf(false);
        imMsg.setSendResult(false);
        imClient.sendPrivateMessage(imMsg);
    }

    /**
     * 批量加载引用消息，避免 N+1 查询
     */
    private Map<Long, QuoteMessageVO> batchLoadQuoteMessage(List<PrivateMessage> messages) {
        List<Long> quoteIds = messages.stream()
                .map(PrivateMessage::getQuoteMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(quoteIds)) return new HashMap<>();

        List<PrivateMessage> quotes = mongoMessageService.findPrivateMessagesByIds(quoteIds);
        return quotes.stream()
                .collect(Collectors.toMap(PrivateMessage::getId, q -> BeanUtils.copyProperties(q, QuoteMessageVO.class)));
    }
}