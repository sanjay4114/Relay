package com.relay.modules.messaging.api.dto;

public record MessageEventDto(
        String type, // CREATED, UPDATED, DELETED, RESTORED
        MessageDto message
) {
}
