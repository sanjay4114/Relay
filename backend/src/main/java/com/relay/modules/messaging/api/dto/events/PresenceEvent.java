package com.relay.modules.messaging.api.dto.events;

import java.time.Instant;

public record PresenceEvent(
    String userPublicId,
    String status, // ONLINE, OFFLINE
    Instant lastSeenAt
) {}
