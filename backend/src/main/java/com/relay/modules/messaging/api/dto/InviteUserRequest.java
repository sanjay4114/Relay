package com.relay.modules.messaging.api.dto;

import jakarta.validation.constraints.NotBlank;

public record InviteUserRequest(
    @NotBlank(message = "User public ID is required")
    String userPublicId
) {}
