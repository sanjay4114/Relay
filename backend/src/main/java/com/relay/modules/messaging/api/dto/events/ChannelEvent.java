package com.relay.modules.messaging.api.dto.events;

public record ChannelEvent(
    String type, // JOINED, LEFT
    String channelPublicId,
    String userPublicId
) {}
