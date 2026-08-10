package com.relay.modules.messaging.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SendMessageRequest(
        @NotBlank(message = "Message content is required")
        @Size(max = 4000, message = "Message content must be less than 4000 characters")
        String content,
        
        String parentMessageId,
        
        List<String> attachmentIds
) {
}
