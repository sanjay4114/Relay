package com.relay.modules.messaging.api.dto;

import com.relay.modules.messaging.domain.ChannelVisibility;

import java.time.Instant;

public record ChannelDto(
        String publicId,
        String name,
        String slug,
        String description,
        ChannelVisibility visibility,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
