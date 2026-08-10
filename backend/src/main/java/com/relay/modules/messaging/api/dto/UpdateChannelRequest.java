package com.relay.modules.messaging.api.dto;

import com.relay.modules.messaging.domain.ChannelVisibility;
import jakarta.validation.constraints.Size;

public record UpdateChannelRequest(
        @Size(max = 80, message = "Name must be less than 80 characters")
        String name,

        @Size(max = 100, message = "Slug must be less than 100 characters")
        String slug,

        String description,

        ChannelVisibility visibility
) {
}
