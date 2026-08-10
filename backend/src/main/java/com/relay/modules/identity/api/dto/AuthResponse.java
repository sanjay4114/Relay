package com.relay.modules.identity.api.dto;

public record AuthResponse(
        String accessToken,
        UserDto user,
        WorkspaceSummaryDto workspace
) {
}
