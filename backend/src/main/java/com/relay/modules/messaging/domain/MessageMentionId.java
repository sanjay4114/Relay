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
public class MessageMentionId implements Serializable {
    private Long messageId;
    private Long userId;

    public MessageMentionId() {}

    public MessageMentionId(Long messageId, Long userId) {
        this.messageId = messageId;
        this.userId = userId;
    }
}
