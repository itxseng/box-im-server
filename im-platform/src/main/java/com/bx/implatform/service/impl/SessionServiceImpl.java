package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.entity.Session;
import com.bx.implatform.mapper.SessionMapper;
import com.bx.implatform.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

        private final StringRedisTemplate redisTemplate;

        private static final String SESSION_LIST_KEY = "IM_SESSION_LIST:";
        private static final String SESSION_DETAIL_KEY = "IM_SESSION_DETAIL:";

        public void updateSession(Long userId, String sessionId, Map<String, String> sessionData, long lastTime) {
            String listKey = SESSION_LIST_KEY + userId;
            String detailKey = SESSION_DETAIL_KEY + userId + ":" + sessionId;

            redisTemplate.opsForZSet().add(listKey, sessionId, lastTime);
            redisTemplate.opsForHash().putAll(detailKey, sessionData);

            redisTemplate.expire(listKey, Duration.ofDays(90));
            redisTemplate.expire(detailKey, Duration.ofDays(90));
        }

        public void incrementUnread(Long userId, String sessionId) {
            String detailKey = SESSION_DETAIL_KEY + userId + ":" + sessionId;
            redisTemplate.opsForHash().increment(detailKey, "unreadCount", 1);
        }

        public void clearUnread(Long userId, String sessionId) {
            String detailKey = SESSION_DETAIL_KEY + userId + ":" + sessionId;
            redisTemplate.opsForHash().put(detailKey, "unreadCount", "0");
        }

        public List<Map<String, String>> getSessionList(Long userId, int limit) {
            String listKey = SESSION_LIST_KEY + userId;
            Set<String> sessionIds = redisTemplate.opsForZSet().reverseRange(listKey, 0, limit - 1);

            if (sessionIds == null || sessionIds.isEmpty()) return Collections.emptyList();

            List<Map<String, String>> result = new ArrayList<>();
            for (String sessionId : sessionIds) {
                String detailKey = SESSION_DETAIL_KEY + userId + ":" + sessionId;
                Map<Object, Object> raw = redisTemplate.opsForHash().entries(detailKey);
                if (!raw.isEmpty()) {
                    Map<String, String> map = raw.entrySet().stream()
                            .collect(Collectors.toMap(
                                    e -> e.getKey().toString(),
                                    e -> e.getValue().toString()
                            ));
                    map.put("sessionId", sessionId);
                    result.add(map);
                }
            }
            return result;
        }
    }
