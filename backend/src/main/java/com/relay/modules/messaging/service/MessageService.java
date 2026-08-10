package com.relay.modules.messaging.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.directory.domain.WorkspaceRole;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.messaging.api.dto.EditMessageRequest;
import com.relay.modules.messaging.api.dto.MessageDto;
import com.relay.modules.messaging.api.dto.MessageEventDto;
import com.relay.modules.messaging.api.dto.SendMessageRequest;
import com.relay.modules.messaging.domain.Message;
import com.relay.modules.messaging.domain.MessageReaction;
import com.relay.modules.messaging.domain.MessageRead;
import com.relay.modules.messaging.domain.MessageReadId;
import com.relay.modules.messaging.domain.MessageType;
import com.relay.modules.messaging.domain.ChannelMember;
import com.relay.modules.messaging.domain.ChannelMemberId;
import com.relay.modules.messaging.repository.ChannelMemberRepository;
import com.relay.modules.notification.domain.NotificationType;
import com.relay.modules.notification.service.NotificationService;
import com.relay.modules.messaging.repository.ChannelRepository;
import com.relay.modules.messaging.repository.MessageRepository;
import com.relay.modules.messaging.repository.MessageReactionRepository;
import com.relay.modules.messaging.repository.SavedMessageRepository;
import com.relay.modules.messaging.repository.MessageMentionRepository;
import com.relay.modules.messaging.domain.Channel;
import com.relay.modules.messaging.domain.SavedMessage;
import com.relay.modules.messaging.domain.SavedMessageId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageReactionRepository messageReactionRepository;
    private final SavedMessageRepository savedMessageRepository;
    private final MessageMentionRepository messageMentionRepository;
    private final com.relay.modules.messaging.repository.MessageReadRepository messageReadRepository;
    private final com.relay.modules.file.repository.FileAttachmentRepository fileAttachmentRepository;
    private final NotificationService notificationService;
    
    private static final Pattern MENTION_PATTERN = Pattern.compile("<@([a-zA-Z0-9-]+)>");

    @Transactional
    public MessageDto sendMessage(String channelPublicId, SendMessageRequest request, Long senderId) {
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));

        if (!channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), senderId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Must be a member of the channel to send messages");
        }

        User sender = userRepository.getReferenceById(senderId);
        Message message = Message.create(channel, sender, request.content());
        
        if (request.parentMessageId() != null) {
            Message parent = messageRepository.findByPublicId(request.parentMessageId())
                    .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parent message not found"));
            
            if (!parent.getChannel().getId().equals(channel.getId())) {
                throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Parent message must be in the same channel");
            }
            if (parent.getParentMessage() != null) {
                throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Cannot reply to a reply");
            }
            
            message.setParentMessage(parent);
            
            // Update parent stats
            parent.setReplyCount(parent.getReplyCount() + 1);
            parent.setLastReplyAt(Instant.now());
            messageRepository.save(parent);
        }

        if (request.attachmentIds() != null && !request.attachmentIds().isEmpty()) {
            for (String attId : request.attachmentIds()) {
                fileAttachmentRepository.findByPublicId(attId).ifPresent(att -> {
                    att.setMessage(message);
                    message.getAttachments().add(att);
                });
            }
        }

        final Message finalMessage = messageRepository.save(message);
        
        parseMentions(finalMessage);
        
        // Notification for Thread Reply
        if (finalMessage.getParentMessage() != null) {
            User parentSender = finalMessage.getParentMessage().getSender();
            if (!parentSender.getId().equals(senderId)) {
                notificationService.createNotification(
                    parentSender.getId(),
                    senderId,
                    NotificationType.THREAD_REPLY,
                    "New reply from " + sender.getDisplayName(),
                    finalMessage.getContent(),
                    "MESSAGE",
                    finalMessage.getPublicId()
                );
            }
        } else if (!finalMessage.getAttachments().isEmpty()) {
            // Notification for File Upload (simulated for demonstration, notifying channel members would require iterating over all members)
            // Let's just generate one for demonstration purposes, maybe to the workspace owner or something,
            // or we skip broadcasting a specific file upload notification to everyone to avoid spam.
            // The prompt requires File Uploads notification. We could notify all channel members except the sender.
            channelMemberRepository.findAllByChannelIdWithUser(finalMessage.getChannel().getId())
                .forEach(member -> {
                    if (!member.getUser().getId().equals(senderId)) {
                        notificationService.createNotification(
                            member.getUser().getId(),
                            senderId,
                            NotificationType.FILE_UPLOAD,
                            sender.getDisplayName() + " shared a file in #" + channel.getName(),
                            finalMessage.getAttachments().get(0).getOriginalName(),
                            "MESSAGE",
                            finalMessage.getPublicId()
                        );
                    }
                });
        }

        MessageDto dto = mapToDto(finalMessage);
        broadcastEvent(channelPublicId, "MESSAGE_CREATED", dto);
        
        if (finalMessage.getParentMessage() != null) {
            MessageDto parentDto = mapToDto(finalMessage.getParentMessage());
            broadcastEvent(channelPublicId, "THREAD_REPLY_CREATED", parentDto);
        }
        
        return dto;
    }

    @Transactional
    public MessageDto editMessage(String channelPublicId, String messagePublicId, EditMessageRequest request, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);

        if (!message.getSender().getId().equals(userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the sender can edit this message");
        }
        
        if (message.isDeleted()) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Cannot edit a deleted message");
        }

        message.setContent(request.content());
        message.setEditedAt(Instant.now());
        message.setEditedBy(userRepository.getReferenceById(userId));
        messageRepository.save(message);
        
        parseMentions(message);

        MessageDto dto = mapToDto(message);
        broadcastEvent(channelPublicId, "MESSAGE_UPDATED", dto);
        return dto;
    }

    @Transactional
    public void deleteMessage(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);

        if (!message.getSender().getId().equals(userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the sender can delete this message");
        }
        
        if (message.isDeleted()) {
            return;
        }

        message.setDeletedAt(Instant.now());
        message.setDeletedBy(userRepository.getReferenceById(userId));
        messageRepository.save(message);

        MessageDto dto = mapToDto(message);
        broadcastEvent(channelPublicId, "MESSAGE_DELETED", dto);
    }

    @Transactional
    public void restoreMessage(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);

        var workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(message.getChannel().getWorkspace().getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));

        if (workspaceMember.getWorkspaceRole() != WorkspaceRole.OWNER && workspaceMember.getWorkspaceRole() != WorkspaceRole.ADMIN) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only workspace admins or owners can restore messages");
        }

        if (!message.isDeleted()) {
            return;
        }

        message.setDeletedAt(null);
        message.setDeletedBy(null);
        messageRepository.save(message);

        MessageDto dto = mapToDto(message);
        broadcastEvent(channelPublicId, "MESSAGE_RESTORED", dto);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getChannelMessages(String channelPublicId, Instant cursor, int limit, Long userId) {
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));

        if (!channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Must be a member of the channel to read messages");
        }

        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 50));
        Page<Message> messagePage;

        if (cursor != null) {
            messagePage = messageRepository.findByChannelIdAndParentMessageIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(channel.getId(), cursor, pageRequest);
        } else {
            messagePage = messageRepository.findByChannelIdAndParentMessageIsNullOrderByCreatedAtDesc(channel.getId(), pageRequest);
        }

        return messagePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getThreadMessages(String channelPublicId, String parentMessagePublicId, Instant cursor, int limit, Long userId) {
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));

        if (!channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Must be a member of the channel to read messages");
        }

        Message parentMessage = messageRepository.findByPublicId(parentMessagePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parent message not found"));

        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 50));
        Page<Message> messagePage;

        if (cursor != null) {
            messagePage = messageRepository.findByParentMessageIdAndCreatedAtBeforeOrderByCreatedAtDesc(parentMessage.getId(), cursor, pageRequest);
        } else {
            messagePage = messageRepository.findByParentMessageIdOrderByCreatedAtDesc(parentMessage.getId(), pageRequest);
        }

        return messagePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addReaction(String channelPublicId, String messagePublicId, String emoji, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        
        if (messageReactionRepository.findByMessageIdAndUserIdAndEmoji(message.getId(), userId, emoji).isPresent()) {
            return; // Already reacted
        }

        MessageReaction reaction = new MessageReaction();
        reaction.setMessage(message);
        reaction.setUser(userRepository.getReferenceById(userId));
        reaction.setEmoji(emoji);
        messageReactionRepository.save(reaction);

        // Fetch latest state to broadcast
        messageReactionRepository.save(reaction);
        
        // Notify sender of the message
        if (!message.getSender().getId().equals(userId)) {
            User actor = userRepository.getReferenceById(userId);
            notificationService.createNotification(
                message.getSender().getId(),
                userId,
                NotificationType.MESSAGE_REACTION,
                actor.getDisplayName() + " reacted with " + emoji,
                "They reacted to your message: " + message.getContent(),
                "MESSAGE",
                message.getPublicId()
            );
        }

        broadcastEvent(message.getChannel().getPublicId(), "REACTION_ADDED", mapToDto(message));
    }

    @Transactional
    public void removeReaction(String channelPublicId, String messagePublicId, String emoji, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        
        messageReactionRepository.findByMessageIdAndUserIdAndEmoji(message.getId(), userId, emoji)
                .ifPresent(reaction -> {
                    messageReactionRepository.delete(reaction);
                    message.getReactions().remove(reaction);
                    broadcastEvent(channelPublicId, "REACTION_REMOVED", mapToDto(message));
                });
    }

    @Transactional
    public void pinMessage(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        
        var workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(message.getChannel().getWorkspace().getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));

        if (workspaceMember.getWorkspaceRole() != WorkspaceRole.OWNER && workspaceMember.getWorkspaceRole() != WorkspaceRole.ADMIN) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only admins or owners can pin messages");
        }

        if (message.getPinnedAt() == null) {
            message.setPinnedAt(Instant.now());
            message.setPinnedBy(userRepository.getReferenceById(userId));
            messageRepository.save(message);
            broadcastEvent(channelPublicId, "MESSAGE_PINNED", mapToDto(message));
        }
    }

    @Transactional
    public void unpinMessage(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        
        var workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(message.getChannel().getWorkspace().getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));

        if (workspaceMember.getWorkspaceRole() != WorkspaceRole.OWNER && workspaceMember.getWorkspaceRole() != WorkspaceRole.ADMIN) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only admins or owners can unpin messages");
        }

        if (message.getPinnedAt() != null) {
            message.setPinnedAt(null);
            message.setPinnedBy(null);
            messageRepository.save(message);
            broadcastEvent(channelPublicId, "MESSAGE_UNPINNED", mapToDto(message));
        }
    }

    @Transactional
    public void saveMessage(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        SavedMessageId id = new SavedMessageId(userId, message.getId());
        if (!savedMessageRepository.existsById(id)) {
            SavedMessage saved = new SavedMessage();
            saved.setId(id);
            saved.setUser(userRepository.getReferenceById(userId));
            saved.setMessage(message);
            savedMessageRepository.save(saved);
        }
    }

    @Transactional
    public void unsaveMessage(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        SavedMessageId id = new SavedMessageId(userId, message.getId());
        savedMessageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getPinnedMessages(String channelPublicId, Long userId) {
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));

        if (!channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Must be a member of the channel to read messages");
        }

        return messageRepository.findByChannelIdAndPinnedAtIsNotNullOrderByPinnedAtDesc(channel.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getSavedMessages(Long userId, Instant cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 50));
        Page<SavedMessage> savedPage;

        if (cursor != null) {
            savedPage = savedMessageRepository.findByUserIdAndSavedAtBeforeOrderBySavedAtDesc(userId, cursor, pageRequest);
        } else {
            savedPage = savedMessageRepository.findByUserIdOrderBySavedAtDesc(userId, pageRequest);
        }

        return savedPage.getContent().stream()
                .map(sm -> mapToDto(sm.getMessage()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessageDelivered(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        MessageReadId id = new MessageReadId(message.getId(), userId);
        
        MessageRead read = messageReadRepository.findById(id).orElseGet(() -> {
            MessageRead newRead = new MessageRead();
            newRead.setId(id);
            newRead.setMessage(message);
            newRead.setUser(userRepository.getReferenceById(userId));
            return newRead;
        });

        if (read.getDeliveredAt() == null) {
            read.setDeliveredAt(Instant.now());
            messageReadRepository.save(read);
            
            // Broadcast receipt
            messagingTemplate.convertAndSend(
                "/topic/channels." + channelPublicId + ".messages.receipts",
                Map.of(
                    "type", "MESSAGE_DELIVERED",
                    "messageId", messagePublicId,
                    "userId", userRepository.getReferenceById(userId).getPublicId(),
                    "deliveredAt", read.getDeliveredAt()
                )
            );
        }
    }

    @Transactional
    public void markMessageRead(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        MessageReadId id = new MessageReadId(message.getId(), userId);
        
        MessageRead read = messageReadRepository.findById(id).orElseGet(() -> {
            MessageRead newRead = new MessageRead();
            newRead.setId(id);
            newRead.setMessage(message);
            newRead.setUser(userRepository.getReferenceById(userId));
            newRead.setDeliveredAt(Instant.now());
            return newRead;
        });

        if (read.getReadAt() == null) {
            read.setReadAt(Instant.now());
            messageReadRepository.save(read);
            
            // Broadcast receipt
            messagingTemplate.convertAndSend(
                "/topic/channels." + channelPublicId + ".messages.receipts",
                Map.of(
                    "type", "MESSAGE_READ",
                    "messageId", messagePublicId,
                    "userId", userRepository.getReferenceById(userId).getPublicId(),
                    "readAt", read.getReadAt()
                )
            );
        }
        
        // Also update channel member watermark
        markChannelRead(channelPublicId, messagePublicId, userId);
    }

    @Transactional
    public void markChannelRead(String channelPublicId, String messagePublicId, Long userId) {
        Message message = getMessageAndValidateChannel(channelPublicId, messagePublicId);
        
        ChannelMember member = channelMemberRepository.findById(new ChannelMemberId(message.getChannel().getId(), userId))
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Must be a member of the channel"));
                
        // Only update if this message is newer than the previous last read
        if (member.getLastReadAt() == null || message.getCreatedAt().isAfter(member.getLastReadAt())) {
            member.setLastReadAt(message.getCreatedAt());
            member.setLastReadMessage(message);
            channelMemberRepository.save(member);
            
            messagingTemplate.convertAndSendToUser(
                userRepository.getReferenceById(userId).getPublicId(),
                "/queue/channels." + channelPublicId + ".unread",
                Map.of(
                    "type", "UNREAD_COUNT_UPDATED",
                    "channelId", channelPublicId,
                    "unreadCount", 0
                )
            );
        }
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(String channelPublicId, Long userId) {
        Channel channel = channelRepository.findByPublicId(channelPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Channel not found"));
                
        ChannelMember member = channelMemberRepository.findById(new ChannelMemberId(channel.getId(), userId))
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Must be a member of the channel"));
                
        if (member.getLastReadAt() == null) {
            // Alternatively, count all messages, but let's bound it for performance or just use 0 if they never read any
            // Usually, joining a channel sets lastReadAt = joinedAt
            return messageRepository.countByChannelIdAndCreatedAtAfter(channel.getId(), member.getJoinedAt());
        }
        
        return messageRepository.countByChannelIdAndCreatedAtAfter(channel.getId(), member.getLastReadAt());
    }

    private Message getMessageAndValidateChannel(String channelPublicId, String messagePublicId) {
        Message message = messageRepository.findByPublicId(messagePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Message not found"));

        if (!message.getChannel().getPublicId().equals(channelPublicId)) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Message does not belong to this channel");
        }
        
        return message;
    }

    private void parseMentions(Message message) {
        Matcher matcher = MENTION_PATTERN.matcher(message.getContent());
        message.getMentions().clear(); // For edits, we could just clear and re-parse
        // Currently JPA handles orphanRemoval, but since we are manually saving below it's better to clear manually in DB if editing.
        // For simplicity, we just delete all existing mentions for this message and re-insert.
        // Actually, just clearing the set doesn't trigger DB delete unless we save the message again, 
        // but `MessageMention` is its own repository. Let's do a direct repository manipulation.
        // Wait, since we are in a transaction and `mentions` is cascade ALL with orphanRemoval=true, 
        // just clearing the list and adding new ones and saving `message` again works.
        message.getMentions().clear();
        
        while (matcher.find()) {
            String userPublicId = matcher.group(1);
            userRepository.findByPublicIdAndDeletedAtIsNull(userPublicId).ifPresent(mentionedUser -> {
                // Ensure the user is in the channel or workspace
                if (channelMemberRepository.existsByChannelIdAndUserId(message.getChannel().getId(), mentionedUser.getId())) {
                    com.relay.modules.messaging.domain.MessageMention mention = new com.relay.modules.messaging.domain.MessageMention();
                    mention.setMessage(message);
                    mention.setUser(mentionedUser);
                    message.getMentions().add(mention);
                    
                    broadcastEvent(message.getChannel().getPublicId(), "MENTION_CREATED", mapToDto(message));
                    
                    // Notify mentioned user
                    notificationService.createNotification(
                        mentionedUser.getId(),
                        message.getSender().getId(),
                        NotificationType.MENTION,
                        "You were mentioned in #" + message.getChannel().getName(),
                        message.getContent(),
                        "MESSAGE",
                        message.getPublicId()
                    );
                }
            });
        }
        messageRepository.save(message);
    }

    private void broadcastEvent(String channelPublicId, String type, MessageDto dto) {
        MessageEventDto event = new MessageEventDto(type, dto);
        messagingTemplate.convertAndSend("/topic/channels." + channelPublicId + ".messages", event);
    }

    private MessageDto mapToDto(Message message) {
        return new MessageDto(
                message.getPublicId(),
                message.getChannel().getPublicId(),
                message.getContent(),
                message.getMessageType().name(),
                new MessageDto.SenderDto(
                        message.getSender().getPublicId(),
                        message.getSender().getDisplayName(),
                        message.getSender().getAvatarUrl()
                ),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getEditedAt(),
                message.getDeletedAt(),
                message.getParentMessage() != null ? message.getParentMessage().getPublicId() : null,
                message.getReplyCount(),
                message.getLastReplyAt(),
                message.getPinnedAt() != null,
                message.getReactions().stream()
                        .collect(Collectors.groupingBy(
                                MessageReaction::getEmoji,
                                Collectors.mapping(r -> r.getUser().getPublicId(), Collectors.toList())
                        )),
                message.getAttachments().stream().map(a -> new com.relay.modules.file.api.dto.FileAttachmentDto(
                        a.getPublicId(),
                        a.getOriginalName(),
                        a.getMimeType(),
                        a.getExtension(),
                        a.getFileSize(),
                        "/api/v1/files/" + a.getPublicId() + "/download",
                        a.getThumbnailPath() != null ? "/api/v1/files/" + a.getPublicId() + "/thumbnail" : null
                )).collect(Collectors.toList())
        );
    }
}
