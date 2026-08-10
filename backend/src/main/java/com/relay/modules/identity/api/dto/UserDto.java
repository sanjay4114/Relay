package com.relay.modules.identity.api.dto;

public record UserDto(
        String publicId,
        String email,
        String displayName,
        String avatarUrl,
        String statusMessage,
        String timezone
) {
}
