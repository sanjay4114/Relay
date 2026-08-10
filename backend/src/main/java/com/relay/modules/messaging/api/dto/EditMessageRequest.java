package com.relay.modules.messaging.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditMessageRequest(
        @NotBlank(message = "Message content is required")
        @Size(max = 4000, message = "Message content must be less than 4000 characters")
        String content
) {
}
