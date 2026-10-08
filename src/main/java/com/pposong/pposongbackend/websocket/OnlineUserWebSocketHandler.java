package com.pposong.pposongbackend.websocket;

import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.jwt.JwtProvider;
import com.pposong.pposongbackend.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import tools.jackson.databind.ObjectMapper;
import com.pposong.pposongbackend.websocket.dto.OnlineUserResponse;
import org.springframework.web.socket.TextMessage;

@Component
public class OnlineUserWebSocketHandler
        extends TextWebSocketHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final OnlineUserManager onlineUserManager;

    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper;

    public OnlineUserWebSocketHandler(
            JwtProvider jwtProvider,
            UserRepository userRepository,
            OnlineUserManager onlineUserManager,
            ObjectMapper objectMapper
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.onlineUserManager = onlineUserManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session
    ) throws Exception {

        URI uri = session.getUri();

        if (uri == null || uri.getQuery() == null) {
            System.out.println("WebSocket authentication failed: token not found");
            session.close();
            return;
        }

        String query = uri.getQuery();

        if (!query.startsWith("token=")) {
            System.out.println("WebSocket authentication failed: invalid token parameter");
            session.close();
            return;
        }

        String token = query.substring("token=".length());

        try {
            Long userId = jwtProvider.getUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found"
                            )
                    );

            session.getAttributes().put(
                    "userId",
                    user.getId()
            );

            onlineUserManager.connect(
                    user.getId(),
                    session.getId()
            );

            sessions.add(session);

            broadcastOnlineUsers();

            System.out.println(
                    "WebSocket user connected: "
                            + user.getUsername()
                            + " (userId="
                            + user.getId()
                            + ")"
            );

        } catch (Exception e) {

            System.out.println(
                    "WebSocket authentication failed"
            );

            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) throws Exception {

        Long userId =
                (Long) session.getAttributes().get("userId");

        sessions.remove(session);

        if (userId != null) {
            onlineUserManager.disconnect(
                    userId,
                    session.getId()
            );
        }

        broadcastOnlineUsers();

        System.out.println(
                "WebSocket user disconnected: userId="
                        + userId
        );
    }

    private void broadcastOnlineUsers()
            throws Exception {

        Set<Long> userIds =
                onlineUserManager.getOnlineUserIds();

        var onlineUsers = userIds.stream()
                .map(userId ->
                        userRepository.findById(userId)
                                .map(user ->
                                        new OnlineUserResponse(
                                                user.getId(),
                                                user.getUsername(),
                                                user.getProfileImageUrl()
                                        )
                                )
                                .orElse(null)
                )
                .filter(user -> user != null)
                .toList();

        String json =
                objectMapper.writeValueAsString(onlineUsers);

        TextMessage message =
                new TextMessage(json);

        for (WebSocketSession webSocketSession : sessions) {

            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(message);
            }
        }
    }
}