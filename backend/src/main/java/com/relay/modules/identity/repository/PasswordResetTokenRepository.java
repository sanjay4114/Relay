package com.relay.modules.identity.repository;

import com.relay.modules.identity.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("""
            SELECT prt FROM PasswordResetToken prt
            JOIN FETCH prt.user u
            WHERE prt.tokenHash = :tokenHash
              AND prt.usedAt IS NULL
              AND u.deletedAt IS NULL
            """)
    Optional<PasswordResetToken> findActiveByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PasswordResetToken prt
            SET prt.usedAt = :usedAt
            WHERE prt.user.id = :userId AND prt.usedAt IS NULL
            """)
    void invalidateAllActiveForUser(@Param("userId") Long userId, @Param("usedAt") Instant usedAt);
}
