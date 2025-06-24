package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMSystemMessage;
import com.bx.implatform.annotation.OnlineCheck;
import com.bx.imcommon.contant.RedisKey;
import com.bx.implatform.entity.SmPushTask;
import com.bx.implatform.entity.SystemMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.imcommon.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.SystemMessageMapper;
import com.bx.implatform.service.SmPushTaskService;
import com.bx.implatform.service.SystemMessageService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.SystemMessageContentVO;
import com.bx.implatform.vo.SystemMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Blue
 * @date: 2024-09-07
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMessageServiceImpl extends ServiceImpl<SystemMessageMapper, SystemMessage>
    implements SystemMessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SmPushTaskService smPushTaskService;
    private final IMClient imClient;

    @Override
    public SystemMessageContentVO getMessageContent(Long id) {
        SystemMessage message = this.getById(id);
        if (Objects.isNull(message)) {
            throw new GlobalException("系统消息不存在");
        }
        SystemMessageContentVO vo = new SystemMessageContentVO();
        vo.setId(message.getId());
        vo.setContentType(message.getContentType());
        vo.setRichText(message.getRichText());
        vo.setExternLink(message.getExternLink());
        return vo;
    }

    @OnlineCheck
    @Override
    public void pullOfflineMessage(Long minSeqNo) {
        UserSession session = SessionContext.getSession();
        List<SmPushTask> tasks = smPushTaskService.findSendedTaskInMonth(minSeqNo);
        if (CollectionUtil.isEmpty(tasks)) {
            this.sendLoadingMessage(false);
            return;
        }
        // 开启加载中标志
        this.sendLoadingMessage(true);
        // 查询出管理的系统消息
        Set<Long> ids = tasks.stream().map(SmPushTask::getMessageId).collect(Collectors.toSet());
        LambdaQueryWrapper<SystemMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SystemMessage::getId, ids);
        wrapper.select(SystemMessage::getId, SystemMessage::getCoverUrl, SystemMessage::getIntro,
            SystemMessage::getTitle);
        List<SystemMessage> messages = this.list(wrapper);
        Map<Long, SystemMessage> messageMap = messages.stream().collect(Collectors.toMap(SystemMessage::getId, o -> o));
        // 已读位置
        String key = StrUtil.join(":", RedisKey.IM_SYSTEM_READED_POSITION, session.getUserId());
        Object v = redisTemplate.opsForValue().get(key);
        long maxSeqNo = Objects.isNull(v) ? 0 : Long.parseLong(v.toString());
        // 逐条推送
        tasks.forEach(task -> {
            SystemMessage message = messageMap.get(task.getMessageId());
            if(Objects.isNull(message)){
                return;
            }
            SystemMessageVO msgInfo = new SystemMessageVO();
            msgInfo.setId(task.getMessageId());
            msgInfo.setSeqNo(task.getSeqNo());
            msgInfo.setType(MessageType.SYSTEM_MESSAGE.code());
            msgInfo.setTitle(message.getTitle());
            msgInfo.setCoverUrl(message.getCoverUrl());
            msgInfo.setIntro(message.getIntro());
            msgInfo.setStatus(MessageStatus.SENDED.code());
            msgInfo.setSendTime(task.getSendTime());
            // 小于等于已读位置，状态为已读
            if (task.getSeqNo() <= maxSeqNo) {
                msgInfo.setStatus(MessageStatus.READED.code());
            }
            IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
            sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
            sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
            sendMessage.setData(msgInfo);
            sendMessage.setSendResult(false);
            imClient.sendSystemMessage(sendMessage);
        });
        // 关闭加载中标志
        this.sendLoadingMessage(false);
        log.info("拉取系统消息，用户id:{},数量:{}", session.getUserId(), tasks.size());
    }

    @Override
    public void readedMessage(Long maxSeqNo) {
        UserSession session = SessionContext.getSession();
        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_SYSTEM_READED_POSITION, session.getUserId());
        // 记录已读消息位置
        redisTemplate.opsForValue().set(key, maxSeqNo);
        // 推送消息给自己，清空会话列表上的已读数量
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.READED.code());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setData(msgInfo);
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);
    }

    private void sendLoadingMessage(Boolean isLoading) {
        UserSession session = SessionContext.getSession();
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoading.toString());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(List.of(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);
    }
}
