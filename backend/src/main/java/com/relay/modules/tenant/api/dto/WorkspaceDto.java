package com.relay.modules.tenant.api.dto;

import java.time.Instant;

public record WorkspaceDto(
        String publicId,
        String name,
        String slug,
        String description,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
}
