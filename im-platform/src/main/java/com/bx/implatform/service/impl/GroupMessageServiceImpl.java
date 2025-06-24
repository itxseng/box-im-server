package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.enums.MessageType;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.util.CommaTextUtils;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.dto.GroupUpdateMessageDTO;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mongo.service.MongoMessageService;
import com.bx.implatform.service.GroupMemberService;
import com.bx.implatform.service.GroupMessageService;
import com.bx.implatform.service.GroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.QuoteMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMessageServiceImpl implements GroupMessageService {
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IMClient imClient;
    private final SensitiveFilterUtil sensitiveFilterUtil;
    private static final ScheduledThreadPoolExecutor EXECUTOR = ThreadPoolExecutorFactory.getThreadPoolExecutor();
    private final MongoMessageService mongoMessageService;

    @Override
    public GroupMessageVO sendMessage(GroupMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = groupService.getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (group.getIsMuted() && !group.getOwnerId().equals(session.getUserId()) && !member.getIsManager()) {
            throw new GlobalException("群主开启了全员禁言模式,无法发送消息");
        }
        // 是否在群聊里面
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您已不在群聊里面，无法发送消息");
        }
        if (member.getIsMuted()) {
            throw new GlobalException("您已被禁言，无法发送消息");
        }
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        if (dto.getReceipt() && userIds.size() > Constant.MAX_NORMAL_GROUP_MEMBER) {
            // 大群的回执消息过于消耗资源，不允许发送
            throw new GlobalException(
                    String.format("当前群聊大于%s人,不支持发送回执消息", Constant.MAX_NORMAL_GROUP_MEMBER));
        }
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(dto, GroupMessage.class);
        msg.setSendId(session.getUserId());
        msg.setSendTime(new Date());
        msg.setSendNickName(member.getShowNickName());
        msg.setAtUserIds(CommaTextUtils.asText(dto.getAtUserIds()));
        // 过滤内容中的敏感词
        if (MessageType.TEXT.code().equals(dto.getType())) {
            msg.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        }
//        this.save(msg);
        mongoMessageService.saveGroupMessage(msg);
        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        // 填充引用消息
        if (!Objects.isNull(dto.getQuoteMessageId())) {
            GroupMessage quoteMessage = mongoMessageService.findGroupMessageById(dto.getQuoteMessageId());
            msgInfo.setQuoteMessage(BeanUtils.copyProperties(quoteMessage, QuoteMessageVO.class));
        }
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), dto.getGroupId(), dto.getContent());
        return msgInfo;
    }

    @Override
    public GroupMessageVO delMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = mongoMessageService.findGroupMessageById(id);

        if (Objects.isNull(msg)) {
            throw new GlobalException("消息不存在");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException("消息已发送超过5分钟，无法删除");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || Boolean.TRUE.equals(member.getQuit())) {
            throw new GlobalException("您已不在群聊里面，无法删除消息");
        }
        //管理员和自己可以删除
        if (!msg.getSendId().equals(session.getUserId()) && !groupService.checkManager(msg.getGroupId(), member)) {
            throw new GlobalException("这条消息不是由您发送,无法删除");
        }
        // 删除数据库消息数据库
//        this.removeById(id);
        // 生成一条删除消息
        GroupMessage recallMsg = new GroupMessage();
        recallMsg.setStatus(MessageStatus.UNSEND.code());
        recallMsg.setType(MessageType.GROUP_MESSAGE_DEL.code());
        recallMsg.setGroupId(msg.getGroupId());
        recallMsg.setSendId(session.getUserId());
        recallMsg.setSendNickName(member.getShowNickName());
        recallMsg.setContent(id.toString());
        recallMsg.setSendTime(new Date());
//        this.save(recallMsg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(recallMsg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("删除群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
        return msgInfo;
    }

    @Transactional
    @Override
    public GroupMessageVO recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = mongoMessageService.findGroupMessageById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException("消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException("这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException("消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || Boolean.TRUE.equals(member.getQuit())) {
            throw new GlobalException("您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
//        msg.setStatus(MessageStatus.RECALL.code());
//        this.updateById(msg);
        mongoMessageService.updateGroupMessageStatus(id, MessageStatus.RECALL.code());
        // 生成一条撤回消息
        GroupMessage recallMsg = new GroupMessage();
        recallMsg.setStatus(MessageStatus.UNSEND.code());
        recallMsg.setType(MessageType.RECALL.code());
        recallMsg.setGroupId(msg.getGroupId());
        recallMsg.setSendId(session.getUserId());
        recallMsg.setSendNickName(member.getShowNickName());
        recallMsg.setContent(id.toString());
        recallMsg.setSendTime(new Date());
        mongoMessageService.saveGroupMessage(recallMsg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(recallMsg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
        return msgInfo;
    }

    @Override
    @Transactional
    public GroupMessageVO manageRecallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = mongoMessageService.findGroupMessageById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException("消息不存在");
        }

        GroupMember operator = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(operator) || Boolean.TRUE.equals(operator.getQuit())) {
            throw new GlobalException("您已不在群聊中");
        }

        // 不是管理员或群主不能操作
        if (!groupService.checkManager(msg.getGroupId(), operator)) {
            throw new GlobalException("您没有权限撤回他人消息");
        }

        // 超时判断  管理员不需要限制时间
//        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
//            throw new GlobalException("消息已发送超过5分钟，无法撤回");
//        }

        // 修改原消息状态
        msg.setStatus(MessageStatus.RECALL.code());
        mongoMessageService.updateGroupMessageStatus(id, MessageStatus.RECALL.code());

        // 生成一条系统撤回消息
        GroupMessage recallMsg = new GroupMessage();
        recallMsg.setStatus(MessageStatus.UNSEND.code());
        recallMsg.setType(MessageType.RECALL.code());
        recallMsg.setGroupId(msg.getGroupId());
        // 管理员ID
        recallMsg.setSendId(session.getUserId());
        // 被撤回消息的ID
        recallMsg.setSendNickName(operator.getShowNickName());
        recallMsg.setContent(id.toString());
        recallMsg.setSendTime(new Date());
        mongoMessageService.saveGroupMessage(recallMsg);

        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(recallMsg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);

        log.info("管理员撤回群聊消息，操作者id:{}, 消息发送者id:{}, 群聊id:{}, 消息内容:{}", session.getUserId(), msg.getSendId(), msg.getGroupId(), msg.getContent());
        return msgInfo;
    }

    @Transactional
    @Override
    public GroupMessageVO updateMessage(GroupUpdateMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = mongoMessageService.findGroupMessageById(dto.getId());
        // 1. 校验消息是否存在
        if (msg == null) throw new GlobalException("消息不存在");

        // 2. 校验是否本人发送
        if (!msg.getSendId().equals(session.getUserId())) throw new GlobalException("这条消息不是由您发送,无法修改");

        // 3. 校验时间是否超限
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_Edit_RECALL_SECOND * 1000)
            throw new GlobalException("消息已发送超过60分钟，无法修改");

        // 4. 校验是否仍在群内
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (member == null || Boolean.TRUE.equals(member.getQuit()))
            throw new GlobalException("您已不在群聊中，无法编辑消息");

        // 5. 更新消息内容、类型、引用消息等
        msg.setContent(dto.getContent());
        msg.setType(dto.getType());
        msg.setQuoteMessageId(dto.getQuoteMessageId());
        msg.setAtUserIds(CommaTextUtils.asText(dto.getAtUserIds()));
        msg.setStatus(MessageStatus.EDITED.code()); // 可选：添加已编辑状态

        // 可选：敏感词过滤
        if (MessageType.TEXT.code().equals(dto.getType())) {
            msg.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        }
        mongoMessageService.updateGroupMessageStatus(dto.getId(), MessageStatus.EDITED.code());

        // 6. 构造VO对象并广播编辑结果
        GroupMessageVO msgVO = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        assert msgVO != null;
        msgVO.setType(MessageType.GROUP_MESSAGE_UPDATE.code()); // 标记为编辑通知
        msgVO.setAtUserIds(dto.getAtUserIds());

        // 引用消息
        if (dto.getQuoteMessageId() != null) {
            GroupMessage quoteMessage = mongoMessageService.findGroupMessageById(dto.getQuoteMessageId());
            msgVO.setQuoteMessage(BeanUtils.copyProperties(quoteMessage, QuoteMessageVO.class));
        }

        // 群成员
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());

        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgVO);

        imClient.sendGroupMessage(sendMessage);
        log.info("编辑群聊消息：msgId={}, groupId={}, content={}", msg.getId(), msg.getGroupId(), msg.getContent());

        return msgVO;
    }

    @OnlineCheck
    @Override
    public void pullOfflineMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        // 查询用户加入的群组
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        Map<Long, GroupMember> groupMemberMap = CollStreamUtil.toIdentityMap(members, GroupMember::getGroupId);
        Set<Long> groupIds = groupMemberMap.keySet();
        if (CollectionUtil.isEmpty(groupIds)) {
            // 关闭加载中标志
            this.sendLoadingMessage(false, session);
            return;
        }
        // 只能拉取最近3个月的,移动端只拉一个月
        int months = session.getTerminal().equals(IMTerminalType.APP.code()) ? 1 : 3;
        Date minDate = DateUtils.addMonths(new Date(), -months);
        List<GroupMessage> messages = mongoMessageService.findGroupMessages(minId, minDate, groupIds, MessageStatus.RECALL.code());
        // 通过群聊对消息进行分组
        Map<Long, List<GroupMessage>> messageGroupMap =
                messages.stream().collect(Collectors.groupingBy(GroupMessage::getGroupId));
        // 退群前的消息
        List<GroupMember> quitMembers = groupMemberService.findQuitInMonth(session.getUserId());
        for (GroupMember quitMember : quitMembers) {
            List<GroupMessage> groupMessages = mongoMessageService.findQuitGroupMessages(
                    quitMember.getGroupId(), minDate, quitMember.getQuitTime(), minId);
            messageGroupMap.put(quitMember.getGroupId(), groupMessages);
            groupMemberMap.put(quitMember.getGroupId(), quitMember);
        }
        // 异步推送消息
        EXECUTOR.execute(() -> {
            Long time = System.currentTimeMillis();
            // 开启加载中标志
            AtomicInteger sendCount = new AtomicInteger();
            try {
                this.sendLoadingMessage(true, session);
                messageGroupMap.forEach((groupId, groupMessages) -> {
                    // 引用消息
                    Map<Long, QuoteMessageVO> quoteMessageMap = batchLoadQuoteMessage(groupMessages);
                    // 填充消息状态
                    String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
                    Object o = redisTemplate.opsForHash().get(key, session.getUserId().toString());
                    long readedMaxId = Objects.isNull(o) ? -1 : Long.parseLong(o.toString());
                    Map<Object, Object> maxIdMap = null;
                    // 第一次拉取时,一个群最多推送1w条消息，防止前端接收能力溢出导致卡顿
                    List<GroupMessage> sendMessages = groupMessages;
                    if (minId <= 0 && groupMessages.size() > 10000) {
                        sendMessages = groupMessages.subList(groupMessages.size() - 10000, groupMessages.size());
                    }
                    for (GroupMessage m : sendMessages) {
                        // 排除加群之前的消息
                        GroupMember member = groupMemberMap.get(m.getGroupId());
                        if (DateUtil.compare(member.getCreatedTime(), m.getSendTime()) > 0) {
                            continue;
                        }
                        // 排除不需要接收的消息
                        List<String> recvIds = CommaTextUtils.asList(m.getRecvIds());
                        if (!recvIds.isEmpty() && !recvIds.contains(session.getUserId().toString())) {
                            continue;
                        }
                        // 组装vo
                        GroupMessageVO vo = BeanUtils.copyProperties(m, GroupMessageVO.class);
                        // 引用消息
                        assert vo != null;
                        vo.setQuoteMessage(quoteMessageMap.get(m.getQuoteMessageId()));
                        // 被@用户列表
                        List<String> atIds = CommaTextUtils.asList(m.getAtUserIds());
                        vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
                        // 填充状态
                        vo.setStatus(readedMaxId >= m.getId() ? MessageStatus.READED.code() : MessageStatus.UNSEND.code());
                        // 针对回执消息填充已读人数
                        if (Boolean.TRUE.equals(m.getReceipt())) {
                            if (Objects.isNull(maxIdMap)) {
                                maxIdMap = redisTemplate.opsForHash().entries(key);
                            }
                            int count = getReadedUserIds(maxIdMap, m.getId(), m.getSendId()).size();
                            vo.setReadedCount(count);
                        }
                        // 推送
                        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                        sendMessage.setSender(new IMUserInfo(m.getSendId(), IMTerminalType.WEB.code()));
                        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
                        sendMessage.setSendResult(false);
                        sendMessage.setSendToSelf(false);
                        sendMessage.setData(vo);
                        imClient.sendGroupMessage(sendMessage);
                        sendCount.getAndIncrement();
                    }
                });
                log.info("耗时：{}", System.currentTimeMillis() - time);
                // 关闭加载中标志
            } catch (Exception e) {
                log.error("拉取离线群聊消息异常，userId={}", session.getUserId(), e);
            } finally {
                this.sendLoadingMessage(false, session);
                log.info("拉取离线群聊消息,用户id:{},数量:{},耗时:{}", session.getUserId(), sendCount.get(),
                        System.currentTimeMillis() - time);
            }
        });
    }


    @Override
    public void readedMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        GroupMessage message = mongoMessageService.findLastGroupMessage(groupId);
        if (Objects.isNull(message)) {
            return;
        }
        // 推送消息给自己的其他终端,同步清空会话列表中的未读数量
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 原来的已读消息位置
        Object maxReadedId = redisTemplate.opsForHash().get(key, session.getUserId().toString());
        // 记录已读消息位置
        redisTemplate.opsForHash().put(key, session.getUserId().toString(), message.getId());
        // 推送消息回执，刷新已读人数显示
        Long startId = Objects.isNull(maxReadedId) ? -1L : Long.parseLong(maxReadedId.toString());
        List<GroupMessage> receiptMessages = mongoMessageService.findReceiptMessages(groupId, startId, message.getId(), MessageStatus.RECALL.code());
        if (CollectionUtil.isNotEmpty(receiptMessages)) {
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
            for (GroupMessage receiptMessage : receiptMessages) {
                int readedCount = getReadedUserIds(maxIdMap, receiptMessage.getId(), receiptMessage.getSendId()).size();
                // 如果所有人都已读，记录回执消息完成标记
                if (readedCount >= userIds.size() - 1) {
                    receiptMessage.setReceiptOk(true);
                    mongoMessageService.updateGroupMessageReceiptOk(receiptMessage.getId(), true);
                }
                msgInfo = new GroupMessageVO();
                msgInfo.setId(receiptMessage.getId());
                msgInfo.setGroupId(groupId);
                msgInfo.setReadedCount(readedCount);
                msgInfo.setReceiptOk(receiptMessage.getReceiptOk());
                msgInfo.setType(MessageType.RECEIPT.code());
                sendMessage = new IMGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                sendMessage.setRecvIds(userIds);
                sendMessage.setData(msgInfo);
                sendMessage.setSendToSelf(false);
                sendMessage.setSendResult(false);
                imClient.sendGroupMessage(sendMessage);
            }
        }
    }

    @Override
    public List<Long> findReadedUsers(Long groupId, Long messageId) {
        UserSession session = SessionContext.getSession();
        GroupMessage message = mongoMessageService.findGroupMessageById(messageId);
        if (Objects.isNull(message)) {
            throw new GlobalException("消息不存在");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您已不在群聊里面");
        }
        // 已读位置key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 一次获取所有用户的已读位置
        Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
        // 返回已读用户的id集合
        return getReadedUserIds(maxIdMap, message.getId(), message.getSendId());
    }

    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        // 群聊成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userId);
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        List<GroupMessage> messages = mongoMessageService.findGroupHistory(
                groupId, member.getCreatedTime(), stIdx, size, MessageStatus.RECALL.code());
        List<GroupMessageVO> messageInfos =
                messages.stream().map(m -> BeanUtils.copyProperties(m, GroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
        return messageInfos;
    }

    @Override
    public void save(GroupMessage msg) {
        mongoMessageService.saveGroupMessage(msg);
    }

    private List<Long> getReadedUserIds(Map<Object, Object> maxIdMap, Long messageId, Long sendId) {
        List<Long> userIds = new LinkedList<>();
        maxIdMap.forEach((k, v) -> {
            Long userId = Long.valueOf(k.toString());
            long maxId = Long.parseLong(v.toString());
            // 发送者不计入已读人数
            if (!sendId.equals(userId) && maxId >= messageId) {
                userIds.add(userId);
            }
        });
        return userIds;
    }

    private void sendLoadingMessage(Boolean isLoading, UserSession session) {
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoading.toString());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private Map<Long, QuoteMessageVO> batchLoadQuoteMessage(List<GroupMessage> messages) {
        // 提取列表中所有引用消息
        List<Long> ids =
                messages.stream().filter(m -> !Objects.isNull(m.getQuoteMessageId())).map(GroupMessage::getQuoteMessageId)
                        .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(ids)) {
            return new HashMap<>();
        }
        List<GroupMessage> quoteMessages = mongoMessageService.findGroupMessagesByIds(ids);
        // 转为vo
        return quoteMessages.stream()
                .collect(Collectors.toMap(m -> m.getId(), m -> BeanUtils.copyProperties(m, QuoteMessageVO.class)));
    }

}
