package com.bx.imserver.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserChannelCtxMap {

    /**
     *  维护userId和ctx的关联关系，格式:Map<userId,map<terminal，ctx>>
     */
    private static Map<Long, Map<Integer, Map<String, ChannelHandlerContext>>> channelMap = new ConcurrentHashMap();

    public static void addChannelCtx(Long userId, Integer terminal, String deviceId, ChannelHandlerContext ctx) {
        channelMap
                .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(terminal, t -> new ConcurrentHashMap<>())
                .put(deviceId, ctx);
    }

    public static void removeChannelCtx(Long userId, Integer terminal, String deviceId) {
        if (userId != null && terminal != null && deviceId != null) {
            Map<Integer, Map<String, ChannelHandlerContext>> terminalMap = channelMap.get(userId);
            if (terminalMap != null) {
                Map<String, ChannelHandlerContext> deviceMap = terminalMap.get(terminal);
                if (deviceMap != null) {
                    deviceMap.remove(deviceId);
                    if (deviceMap.isEmpty()) {
                        terminalMap.remove(terminal);
                    }
                    if (terminalMap.isEmpty()) {
                        channelMap.remove(userId);
                    }
                }
            }
        }
    }

    public static ChannelHandlerContext getChannelCtx(Long userId, Integer terminal, String deviceId) {
        return Optional.ofNullable(channelMap.get(userId))
                .map(termMap -> termMap.get(terminal))
                .map(devMap -> devMap.get(deviceId))
                .orElse(null);
    }

    public static List<ChannelHandlerContext> getAllCtxForUser(Long userId) {
        List<ChannelHandlerContext> list = new ArrayList<>();
        Map<Integer, Map<String, ChannelHandlerContext>> terminalMap = channelMap.get(userId);
        if (terminalMap != null) {
            for (Map<String, ChannelHandlerContext> deviceMap : terminalMap.values()) {
                list.addAll(deviceMap.values());
            }
        }
        return list;
    }

}
