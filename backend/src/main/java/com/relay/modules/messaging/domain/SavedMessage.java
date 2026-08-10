package com.relay.modules.messaging.domain;

import com.relay.modules.identity.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "saved_messages")
@Getter
@Setter
public class SavedMessage {

    @EmbeddedId
    private SavedMessageId id = new SavedMessageId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt = Instant.now();
}
