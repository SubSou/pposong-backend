package com.pposong.pposongbackend.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserManager {

    // userId별로 연결된 WebSocket sessionId들을 저장
    private final Map<Long, Set<String>> onlineUsers =
            new ConcurrentHashMap<>();

    public void connect(
            Long userId,
            String sessionId
    ) {
        onlineUsers
                .computeIfAbsent(
                        userId,
                        key -> ConcurrentHashMap.newKeySet()
                )
                .add(sessionId);

        System.out.println(
                "Online user connected: userId="
                        + userId
                        + ", onlineCount="
                        + onlineUsers.size()
        );
    }

    public void disconnect(
            Long userId,
            String sessionId
    ) {
        Set<String> sessions =
                onlineUsers.get(userId);

        if (sessions == null) {
            return;
        }

        sessions.remove(sessionId);

        // 해당 사용자의 WebSocket 연결이 하나도 없으면
        // 온라인 목록에서 제거
        if (sessions.isEmpty()) {
            onlineUsers.remove(userId);
        }

        System.out.println(
                "Online user disconnected: userId="
                        + userId
                        + ", onlineCount="
                        + onlineUsers.size()
        );
    }

    public Set<Long> getOnlineUserIds() {
        return Set.copyOf(onlineUsers.keySet());
    }
}