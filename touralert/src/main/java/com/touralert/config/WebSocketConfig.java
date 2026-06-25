package com.touralert.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker; // Fixed import
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Fixed annotation name
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint URL the frontend connects to initially
        registry.addEndpoint("/ws-alerts")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enables a built-in memory message broker to carry alerts out to users
        registry.enableSimpleBroker("/topic");
        // Prefix used for filtering client-to-server messages
        registry.setApplicationDestinationPrefixes("/app");
    }
}