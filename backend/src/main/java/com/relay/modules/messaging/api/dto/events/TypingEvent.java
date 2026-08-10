package com.relay.modules.messaging.api.dto.events;

public record TypingEvent(
    String userPublicId,
    String channelPublicId,
    boolean isTyping
) {}
