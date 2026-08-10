package com.relay.modules.directory.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TransferOwnershipRequest(
        @NotBlank(message = "New owner ID is required")
        String newOwnerPublicId
) {
}
