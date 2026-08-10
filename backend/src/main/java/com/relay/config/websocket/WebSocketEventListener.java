package com.relay.config.websocket;

import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.messaging.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        AuthenticatedUser user = getAuthenticatedUser(headerAccessor);
        if (user != null) {
            String sessionId = headerAccessor.getSessionId();
            presenceService.handleConnect(user.publicId(), sessionId);
            log.info("User connected: {} (Session: {})", user.publicId(), sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        AuthenticatedUser user = getAuthenticatedUser(headerAccessor);
        if (user != null) {
            String sessionId = headerAccessor.getSessionId();
            presenceService.handleDisconnect(user.publicId(), sessionId);
            log.info("User disconnected: {} (Session: {})", user.publicId(), sessionId);
        }
    }

    private AuthenticatedUser getAuthenticatedUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof AuthenticatedUser user) {
                return user;
            }
        }
        return null;
    }
}
