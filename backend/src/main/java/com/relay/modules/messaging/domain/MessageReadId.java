package com.relay.modules.messaging.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class MessageReadId implements Serializable {
    private Long messageId;
    private Long userId;

    public MessageReadId() {}

    public MessageReadId(Long messageId, Long userId) {
        this.messageId = messageId;
        this.userId = userId;
    }
}
