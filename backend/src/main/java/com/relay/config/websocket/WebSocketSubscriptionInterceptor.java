package com.relay.config.websocket;

import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.messaging.domain.Channel;
import com.relay.modules.messaging.domain.ChannelVisibility;
import com.relay.modules.messaging.repository.ChannelMemberRepository;
import com.relay.modules.messaging.repository.ChannelRepository;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.WorkspaceRepository;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionInterceptor implements ChannelInterceptor {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;

    private static final Pattern WORKSPACE_TOPIC_PATTERN = Pattern.compile("/topic/workspaces\\.([^.]+)\\..*");
    private static final Pattern CHANNEL_TOPIC_PATTERN = Pattern.compile("/topic/channels\\.([^.]+)\\..*");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination == null) return message;

            AuthenticatedUser user = getAuthenticatedUser(accessor);
            if (user == null) {
                throw new IllegalArgumentException("No authentication found for subscription");
            }

            Matcher workspaceMatcher = WORKSPACE_TOPIC_PATTERN.matcher(destination);
            if (workspaceMatcher.matches()) {
                String workspacePublicId = workspaceMatcher.group(1);
                Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                        .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
                
                if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.userId())) {
                    throw new IllegalArgumentException("Not a member of this workspace");
                }
            }

            Matcher channelMatcher = CHANNEL_TOPIC_PATTERN.matcher(destination);
            if (channelMatcher.matches()) {
                String channelPublicId = channelMatcher.group(1);
                Channel ch = channelRepository.findByPublicId(channelPublicId)
                        .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
                
                if (ch.getVisibility() == ChannelVisibility.PRIVATE) {
                    if (!channelMemberRepository.existsByChannelIdAndUserId(ch.getId(), user.userId())) {
                        throw new IllegalArgumentException("Not a member of this private channel");
                    }
                } else {
                    // For public channels, ensure they are at least in the workspace
                    if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(ch.getWorkspace().getId(), user.userId())) {
                        throw new IllegalArgumentException("Not a member of this workspace");
                    }
                }
            }
        }
        return message;
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
