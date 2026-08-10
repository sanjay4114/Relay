package com.relay.modules.messaging.domain;

import com.relay.common.jpa.JpaColumnDefinitions;
import com.relay.modules.identity.domain.User;
import com.relay.modules.tenant.domain.Workspace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "channels")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = JpaColumnDefinitions.UUID_CHAR_36)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelVisibility visibility = ChannelVisibility.PUBLIC;

    @Column(nullable = false)
    private boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public static Channel create(Workspace workspace, String name, String slug, ChannelVisibility visibility, User createdBy) {
        Channel channel = new Channel();
        channel.publicId = UUID.randomUUID().toString();
        channel.workspace = workspace;
        channel.name = name;
        channel.slug = slug;
        channel.visibility = visibility != null ? visibility : ChannelVisibility.PUBLIC;
        channel.createdBy = createdBy;
        return channel;
    }
}
