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
public class SavedMessageId implements Serializable {
    private Long userId;
    private Long messageId;

    public SavedMessageId() {}

    public SavedMessageId(Long userId, Long messageId) {
        this.userId = userId;
        this.messageId = messageId;
    }
}
