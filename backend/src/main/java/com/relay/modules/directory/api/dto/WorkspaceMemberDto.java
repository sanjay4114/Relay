package com.relay.modules.directory.api.dto;

import java.time.Instant;

public record WorkspaceMemberDto(
        String publicId,
        String email,
        String displayName,
        String avatarUrl,
        String role,
        Instant joinedAt
) {
}
