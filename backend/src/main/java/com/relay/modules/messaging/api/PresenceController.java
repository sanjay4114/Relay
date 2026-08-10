package com.relay.modules.messaging.api;

import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.messaging.api.dto.events.TypingEvent;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class PresenceController {

    private final SimpMessagingTemplate messagingTemplate;

    public PresenceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/channels.{channelPublicId}.typing")
    public void handleTyping(
            @DestinationVariable String channelPublicId,
            Principal principal,
            TypingPayload payload) {
            
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof AuthenticatedUser user) {
                TypingEvent event = new TypingEvent(user.publicId(), channelPublicId, payload.isTyping());
                messagingTemplate.convertAndSend("/topic/channels." + channelPublicId + ".events", event);
            }
        }
    }
    
    public record TypingPayload(boolean isTyping) {}
}
