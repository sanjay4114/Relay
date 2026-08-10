package com.relay.modules.notification.api.dto;

import java.time.Instant;

public record NotificationDto(
        String publicId,
        String type,
        String title,
        String body,
        String entityType,
        String entityPublicId,
        boolean isRead,
        Instant createdAt,
        ActorDto actor
) {
    public record ActorDto(
            String publicId,
            String displayName,
            String avatarUrl
    ) {}
}
