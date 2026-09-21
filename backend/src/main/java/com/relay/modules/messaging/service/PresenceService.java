package com.relay.modules.messaging.service;

import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.messaging.api.dto.events.PresenceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Tracks userPublicId -> Set of Session IDs
    private final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void handleConnect(String userPublicId, String sessionId) {
        Set<String> sessions = userSessions.computeIfAbsent(userPublicId, k -> ConcurrentHashMap.newKeySet());
        boolean wasOffline = sessions.isEmpty();
        sessions.add(sessionId);

        if (wasOffline) {
            Instant now = Instant.now();
            updateLastSeen(userPublicId, now);
            broadcastPresence(userPublicId, "ONLINE", now);
        }
    }

    public void handleDisconnect(String userPublicId, String sessionId) {
        Set<String> sessions = userSessions.get(userPublicId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userPublicId);
                Instant now = Instant.now();
                updateLastSeen(userPublicId, now);
                broadcastPresence(userPublicId, "OFFLINE", now);
            }
        }
    }

    @Transactional
    protected void updateLastSeen(String userPublicId, Instant lastSeenAt) {
        userRepository.findByPublicIdAndDeletedAtIsNull(userPublicId).ifPresent(user -> {
            user.setLastSeenAt(lastSeenAt);
            userRepository.save(user);
        });
    }

    private void broadcastPresence(String userPublicId, String status, Instant lastSeenAt) {
        PresenceEvent event = new PresenceEvent(userPublicId, status, lastSeenAt);
        // We broadcast to a generic presence topic. 
        // In a real app, we'd only broadcast to workspaces the user is a member of.
        // For simplicity, we broadcast globally and the frontend handles it, or we can broadcast to /topic/presence
        messagingTemplate.convertAndSend("/topic/presence", event);
    }
    
    public boolean isUserOnline(String userPublicId) {
        return userSessions.containsKey(userPublicId);
    }
}
