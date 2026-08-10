package com.relay.modules.identity.domain;

import com.relay.common.jpa.JpaColumnDefinitions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = JpaColumnDefinitions.UUID_CHAR_36)
    private String publicId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "status_message", length = 200)
    private String statusMessage;

    @Column(nullable = false, length = 64)
    private String timezone = "UTC";

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    public static User create(String email, String passwordHash, String displayName) {
        User user = new User();
        user.publicId = UUID.randomUUID().toString();
        user.email = email.toLowerCase().trim();
        user.passwordHash = passwordHash;
        user.displayName = displayName.trim();
        return user;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
