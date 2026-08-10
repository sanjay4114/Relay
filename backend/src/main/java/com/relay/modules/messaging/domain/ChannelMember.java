package com.relay.modules.messaging.domain;

import com.relay.modules.identity.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "channel_members")
@Getter
@Setter
@NoArgsConstructor
public class ChannelMember {

    @EmbeddedId
    private ChannelMemberId id = new ChannelMemberId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("channelId")
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelRole role = ChannelRole.MEMBER;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private Message lastReadMessage;

    public static ChannelMember create(Channel channel, User user, ChannelRole role) {
        ChannelMember member = new ChannelMember();
        member.channel = channel;
        member.user = user;
        member.role = role != null ? role : ChannelRole.MEMBER;
        return member;
    }
}
