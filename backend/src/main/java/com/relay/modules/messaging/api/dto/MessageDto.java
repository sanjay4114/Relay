package com.relay.modules.messaging.api.dto;

import java.time.Instant;

public record MessageDto(
        String publicId,
        String channelPublicId,
        String content,
        String messageType,
        SenderDto sender,
        Instant createdAt,
        Instant updatedAt,
        Instant editedAt,
        Instant deletedAt,
        String parentMessageId,
        Integer replyCount,
        Instant lastReplyAt,
        boolean isPinned,
        java.util.Map<String, java.util.List<String>> reactions,
        java.util.List<com.relay.modules.file.api.dto.FileAttachmentDto> attachments
) {
    public record SenderDto(
            String publicId,
            String displayName,
            String avatarUrl
    ) {}
}
