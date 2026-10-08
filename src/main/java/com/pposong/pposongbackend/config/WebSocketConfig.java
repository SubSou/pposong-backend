
package com.pposong.pposongbackend.config;

import com.pposong.pposongbackend.websocket.OnlineUserWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OnlineUserWebSocketHandler onlineUserWebSocketHandler;

    public WebSocketConfig(
            OnlineUserWebSocketHandler onlineUserWebSocketHandler
    ) {
        this.onlineUserWebSocketHandler =
                onlineUserWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry
    ) {
        registry
                .addHandler(
                        onlineUserWebSocketHandler,
                        "/ws/online"
                )
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "https://pposong-frontend.vercel.app"
                );
    }
}
