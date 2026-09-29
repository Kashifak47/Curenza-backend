package com.curenza.trading_engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the endpoint React will use to establish the initial connection
        // We enable SockJS as a fallback for browsers that drop raw WebSockets
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic" -> Where Spring Boot broadcasts live prices TO React
        config.enableSimpleBroker("/topic");
        // "/app" -> Where React can send messages TO Spring Boot (if needed)
        config.setApplicationDestinationPrefixes("/app");
    }
}
