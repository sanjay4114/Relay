package com.relay.modules.messaging.service;

import com.relay.common.exception.RelayException;
import com.relay.common.util.SlugUtils;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.directory.domain.WorkspaceRole;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.messaging.api.dto.ChannelDto;
import com.relay.modules.messaging.api.dto.ChannelMemberDto;
import com.relay.modules.messaging.api.dto.CreateChannelRequest;
import com.relay.modules.messaging.api.dto.UpdateChannelRequest;
import com.relay.modules.messaging.domain.Channel;
import com.relay.modules.messaging.domain.ChannelMember;
import com.relay.modules.messaging.domain.ChannelRole;
import com.relay.modules.messaging.domain.ChannelVisibility;
import com.relay.modules.messaging.repository.ChannelMemberRepository;
import com.relay.modules.messaging.repository.ChannelRepository;
import com.relay.modules.notification.domain.NotificationType;
import com.relay.modules.notification.service.NotificationService;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ChannelDto> listWorkspaceChannels(String workspacePublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        
        return channelRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .filter(ch -> !ch.isDeleted())
                .filter(ch -> ch.getVisibility() == ChannelVisibility.PUBLIC || 
                        channelMemberRepository.existsByChannelIdAndUserId(ch.getId(), userId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChannelDto getChannelDetails(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        return mapToDto(channel);
    }

    @Transactional
    public ChannelDto createChannel(String workspacePublicId, CreateChannelRequest request, AuthenticatedUser userDetails) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userDetails.userId());
        
        var workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), userDetails.userId())
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));

        if (workspaceMember.getWorkspaceRole() != WorkspaceRole.OWNER && workspaceMember.getWorkspaceRole() != WorkspaceRole.ADMIN) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only Admins and Owners can create channels.");
        }

        if (channelRepository.existsByNameAndWorkspaceId(request.name(), workspace.getId())) {
            throw new RelayException(HttpStatus.CONFLICT, "CONFLICT", "Channel name already exists in this workspace.");
        }

        String slug = request.slug() != null && !request.slug().isBlank() 
                ? SlugUtils.slugify(request.slug()) 
                : SlugUtils.slugify(request.name());

        if (channelRepository.existsBySlugAndWorkspaceId(slug, workspace.getId())) {
            slug = slug + "-" + SlugUtils.uniqueSuffix();
        }

        User user = userRepository.getReferenceById(userDetails.userId());
        Channel channel = Channel.create(workspace, request.name(), slug, request.visibility(), user);
        channel.setDescription(request.description());
        channel = channelRepository.save(channel);

        ChannelMember member = ChannelMember.create(channel, user, ChannelRole.OWNER);
        channelMemberRepository.save(member);

        return mapToDto(channel);
    }

    @Transactional
    public ChannelDto updateChannel(String workspacePublicId, String channelPublicId, UpdateChannelRequest request, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        
        validateChannelAdmin(channel, userId, workspace.getId());

        if (request.name() != null && !request.name().equals(channel.getName())) {
            if (channelRepository.existsByNameAndWorkspaceId(request.name(), workspace.getId())) {
                throw new RelayException(HttpStatus.CONFLICT, "CONFLICT", "Channel name already exists.");
            }
            channel.setName(request.name());
        }

        if (request.slug() != null && !request.slug().equals(channel.getSlug())) {
            String slug = SlugUtils.slugify(request.slug());
            if (channelRepository.existsBySlugAndWorkspaceId(slug, workspace.getId())) {
                throw new RelayException(HttpStatus.CONFLICT, "CONFLICT", "Channel slug already exists.");
            }
            channel.setSlug(slug);
        }

        if (request.description() != null) channel.setDescription(request.description());
        if (request.visibility() != null) channel.setVisibility(request.visibility());

        channelRepository.save(channel);
        return mapToDto(channel);
    }

    @Transactional
    public void archiveChannel(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        validateChannelAdmin(channel, userId, workspace.getId());
        channel.setArchived(true);
        channelRepository.save(channel);
    }

    @Transactional
    public void restoreChannel(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        validateChannelAdmin(channel, userId, workspace.getId());
        channel.setArchived(false);
        channelRepository.save(channel);
    }

    @Transactional
    public void deleteChannel(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        
        var workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), userId).get();
        if (workspaceMember.getWorkspaceRole() != WorkspaceRole.OWNER) {
             throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only workspace owners can delete channels.");
        }

        channel.setDeletedAt(Instant.now());
        channel.setDeletedBy(userRepository.getReferenceById(userId));
        channelRepository.save(channel);
    }

    @Transactional
    public void joinChannel(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));

        if (channel.isDeleted()) {
            throw new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found");
        }

        if (channel.getVisibility() == ChannelVisibility.PRIVATE) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Cannot join private channel directly. You must be invited.");
        }

        if (channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), userId)) {
            return; // Already joined
        }

        User user = userRepository.getReferenceById(userId);
        ChannelMember member = ChannelMember.create(channel, user, ChannelRole.MEMBER);
        channelMemberRepository.save(member);
    }

    @Transactional
    public void leaveChannel(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));
        
        ChannelMember member = channelMemberRepository.findByChannelIdAndUserId(channel.getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "You are not a member of this channel"));
        
        if (member.getRole() == ChannelRole.OWNER) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Channel owners cannot leave. Transfer ownership first.");
        }
        
        channelMemberRepository.delete(member);
    }
    
    @Transactional(readOnly = true)
    public List<ChannelMemberDto> listChannelMembers(String workspacePublicId, String channelPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        
        return channelMemberRepository.findAllByChannelIdWithUser(channel.getId()).stream()
                .map(this::mapMemberToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void inviteUser(String workspacePublicId, String channelPublicId, String targetUserPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        validateChannelAdmin(channel, userId, workspace.getId());

        User targetUser = userRepository.findByPublicIdAndDeletedAtIsNull(targetUserPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), targetUser.getId())) {
             throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Target user is not in the workspace");
        }

        if (channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), targetUser.getId())) {
            return; // Already a member
        }

        ChannelMember member = ChannelMember.create(channel, targetUser, ChannelRole.MEMBER);
        channelMemberRepository.save(member);
        
        notificationService.createNotification(
            targetUser.getId(),
            userId,
            NotificationType.CHANNEL_INVITE,
            "Channel Invitation",
            "You have been added to #" + channel.getName(),
            "CHANNEL",
            channel.getPublicId()
        );
    }

    @Transactional
    public void removeUser(String workspacePublicId, String channelPublicId, String targetUserPublicId, Long userId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspacePublicId, userId);
        Channel channel = getChannelAndValidateAccess(channelPublicId, workspace.getId(), userId);
        validateChannelAdmin(channel, userId, workspace.getId());

        User targetUser = userRepository.findByPublicIdAndDeletedAtIsNull(targetUserPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        ChannelMember targetMember = channelMemberRepository.findByChannelIdAndUserId(channel.getId(), targetUser.getId())
                .orElseThrow(() -> new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "User is not in the channel"));

        if (targetMember.getRole() == ChannelRole.OWNER) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Cannot remove channel owner");
        }

        channelMemberRepository.delete(targetMember);
    }

    private Workspace getWorkspaceAndValidateAccess(String workspacePublicId, Long userId) {
        Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found"));
        if (workspace.isDeleted()) {
            throw new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found");
        }
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied to workspace");
        }
        return workspace;
    }

    private Channel getChannelAndValidateAccess(String channelPublicId, Long workspaceId, Long userId) {
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));
        
        if (channel.isDeleted() || !channel.getWorkspace().getId().equals(workspaceId)) {
            throw new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found");
        }
        
        if (channel.getVisibility() == ChannelVisibility.PRIVATE) {
            if (!channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), userId)) {
                throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied to private channel");
            }
        }
        return channel;
    }

    private void validateChannelAdmin(Channel channel, Long userId, Long workspaceId) {
        var workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId).get();
        if (workspaceMember.getWorkspaceRole() == WorkspaceRole.OWNER || workspaceMember.getWorkspaceRole() == WorkspaceRole.ADMIN) {
            return;
        }
        
        ChannelMember cm = channelMemberRepository.findByChannelIdAndUserId(channel.getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not a member of this channel"));
        if (cm.getRole() != ChannelRole.OWNER && cm.getRole() != ChannelRole.MODERATOR) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Requires channel moderator or owner role");
        }
    }

    private ChannelDto mapToDto(Channel ch) {
        return new ChannelDto(
                ch.getPublicId(),
                ch.getName(),
                ch.getSlug(),
                ch.getDescription(),
                ch.getVisibility(),
                ch.isArchived(),
                ch.getCreatedAt(),
                ch.getUpdatedAt()
        );
    }
    
    private ChannelMemberDto mapMemberToDto(ChannelMember cm) {
        return new ChannelMemberDto(
                cm.getUser().getPublicId(),
                cm.getUser().getEmail(),
                cm.getUser().getDisplayName(),
                cm.getUser().getAvatarUrl(),
                cm.getRole(),
                cm.getJoinedAt(),
                cm.getLastReadAt()
        );
    }
}
