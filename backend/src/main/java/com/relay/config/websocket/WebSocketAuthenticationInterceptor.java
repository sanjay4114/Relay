package com.relay.config.websocket;

import com.relay.config.security.AuthenticatedUser;
import com.relay.config.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.getFirst();
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                
                if (jwtTokenProvider.isTokenValid(token)) {
                    try {
                        Claims claims = jwtTokenProvider.parseToken(token);
                        Long userId = claims.get("userId", Long.class);
                        String email = claims.get("email", String.class);
                        String publicId = claims.getSubject();
                        
                        AuthenticatedUser user = AuthenticatedUser.of(userId, publicId, email);
                        UsernamePasswordAuthenticationToken auth = 
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                
                        accessor.setUser(auth);
                    } catch (Exception e) {
                        log.warn("Invalid JWT token during WebSocket connection: {}", e.getMessage());
                    }
                }
            }
        }
        return message;
    }
}
