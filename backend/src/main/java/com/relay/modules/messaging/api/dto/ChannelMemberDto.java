package com.relay.modules.messaging.api.dto;

import com.relay.modules.messaging.domain.ChannelRole;

import java.time.Instant;

public record ChannelMemberDto(
        String userPublicId,
        String email,
        String displayName,
        String avatarUrl,
        ChannelRole role,
        Instant joinedAt,
        Instant lastReadAt
) {
}
