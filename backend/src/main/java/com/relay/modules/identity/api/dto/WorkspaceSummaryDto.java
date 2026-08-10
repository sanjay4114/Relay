package com.relay.modules.identity.api.dto;

public record WorkspaceSummaryDto(
        String publicId,
        String name,
        String slug
) {
}
