package com.relay.modules.messaging.api.dto.events;

public record WorkspaceEvent(
    String type, // JOINED, LEFT
    String workspacePublicId,
    String userPublicId
) {}
