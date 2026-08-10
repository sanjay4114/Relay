package com.relay.modules.notification.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.notification.api.dto.NotificationDto;
import com.relay.modules.notification.domain.Notification;
import com.relay.modules.notification.domain.NotificationType;
import com.relay.modules.notification.repository.NotificationRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(Long recipientId, Long actorId, NotificationType type, String title, String body, String entityType, String entityPublicId) {
        if (recipientId.equals(actorId)) {
            return; // Don't notify self
        }

        Notification notification = new Notification();
        notification.setRecipient(userRepository.getReferenceById(recipientId));
        
        if (actorId != null) {
            notification.setActor(userRepository.getReferenceById(actorId));
        }
        
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setEntityType(entityType);
        notification.setEntityPublicId(entityPublicId);
        
        notification = notificationRepository.save(notification);
        
        // Broadcast
        NotificationDto dto = mapToDto(notification);
        messagingTemplate.convertAndSendToUser(
                notification.getRecipient().getPublicId(),
                "/queue/notifications",
                Map.of(
                        "type", "NOTIFICATION_CREATED",
                        "notification", dto,
                        "unreadCount", getUnreadCount(recipientId)
                )
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(Long userId, int limit) {
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.min(limit, 50)));
        return page.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long userId, String publicId) {
        Notification notification = notificationRepository.findByPublicIdAndRecipientId(publicId, userId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Notification not found"));
                
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
            
            // Broadcast read state
            messagingTemplate.convertAndSendToUser(
                    notification.getRecipient().getPublicId(),
                    "/queue/notifications",
                    Map.of(
                            "type", "NOTIFICATION_READ",
                            "publicId", publicId,
                            "unreadCount", getUnreadCount(userId)
                    )
            );
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
        
        // Broadcast
        User user = userRepository.findById(userId).orElseThrow();
        messagingTemplate.convertAndSendToUser(
                user.getPublicId(),
                "/queue/notifications",
                Map.of(
                        "type", "NOTIFICATIONS_ALL_READ",
                        "unreadCount", 0
                )
        );
    }

    @Transactional
    public void deleteNotification(Long userId, String publicId) {
        Notification notification = notificationRepository.findByPublicIdAndRecipientId(publicId, userId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Notification not found"));
                
        notificationRepository.delete(notification);
        
        messagingTemplate.convertAndSendToUser(
                notification.getRecipient().getPublicId(),
                "/queue/notifications",
                Map.of(
                        "type", "NOTIFICATION_DELETED",
                        "publicId", publicId,
                        "unreadCount", getUnreadCount(userId)
                )
        );
    }

    private NotificationDto mapToDto(Notification notification) {
        NotificationDto.ActorDto actorDto = null;
        if (notification.getActor() != null) {
            actorDto = new NotificationDto.ActorDto(
                    notification.getActor().getPublicId(),
                    notification.getActor().getDisplayName(),
                    notification.getActor().getAvatarUrl()
            );
        }
        
        return new NotificationDto(
                notification.getPublicId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getBody(),
                notification.getEntityType(),
                notification.getEntityPublicId(),
                notification.isRead(),
                notification.getCreatedAt(),
                actorDto
        );
    }
}
