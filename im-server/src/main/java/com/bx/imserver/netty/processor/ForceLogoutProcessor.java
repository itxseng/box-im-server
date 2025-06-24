package com.bx.imserver.netty.processor;

import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMEventType;
import com.bx.imcommon.model.*;
import com.bx.imcommon.mq.RedisMQTemplate;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForceLogoutProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisMQTemplate redisMQTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo receiver = recvInfo.getReceivers().get(0);
        Long userId = receiver.getId();
        Integer terminal = receiver.getTerminal();
        String deviceId = receiver.getDeviceId();
        ChannelHandlerContext ctx = UserChannelCtxMap.getChannelCtx(userId, terminal, deviceId);
        if (ctx != null) {
            IMSendInfo<Object> sendInfo = new IMSendInfo<>();
            sendInfo.setCmd(IMCmdType.FORCE_LOGUT.code());
            sendInfo.setData(recvInfo.getData());
            ctx.channel().writeAndFlush(sendInfo);
            ctx.channel().close();
        }
        UserChannelCtxMap.removeChannelCtx(userId, terminal, deviceId);
        try {
            String deviceKey = String.join(":", IMRedisKey.IM_USER_DEVICE_ID, userId.toString(), terminal.toString());
            redisMQTemplate.opsForSet().remove(deviceKey, deviceId);
            Long size = redisMQTemplate.opsForSet().size(deviceKey);
            if (size == null || size == 0) {
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
                redisMQTemplate.delete(key);
                IMUserEvent event = new IMUserEvent();
                event.setEventType(IMEventType.OFFLINE.code());
                event.setUserInfo(new IMUserInfo(userId, terminal));
                event.setExtra(true);
                redisMQTemplate.opsForList().rightPush(IMRedisKey.IM_USER_EVENT_QUEUE, event);
            }
        } catch (Exception e) {
            log.error("forceLogout redis error,userId={},terminal={},deviceId={}", userId, terminal, deviceId, e);
        }
    }
}
