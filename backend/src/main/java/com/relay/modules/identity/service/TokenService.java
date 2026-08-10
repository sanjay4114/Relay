package com.relay.modules.identity.service;

import com.relay.config.AuthProperties;
import com.relay.config.JwtProperties;
import com.relay.modules.identity.domain.PasswordResetToken;
import com.relay.modules.identity.domain.RefreshToken;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.PasswordResetTokenRepository;
import com.relay.modules.identity.repository.RefreshTokenRepository;
import com.relay.common.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;

    @Transactional
    public String issueRefreshToken(User user, String userAgent, String ipAddress) {
        String rawToken = TokenHasher.generateRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(TokenHasher.hashToken(rawToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()));
        refreshToken.setUserAgent(userAgent);
        refreshToken.setIpAddress(ipAddress);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {
        return refreshTokenRepository.findActiveByTokenHash(TokenHasher.hashToken(rawToken))
                .filter(RefreshToken::isActive)
                .orElse(null);
    }

    @Transactional
    public void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String rotateRefreshToken(RefreshToken existing, String userAgent, String ipAddress) {
        revokeRefreshToken(existing);
        return issueRefreshToken(existing.getUser(), userAgent, ipAddress);
    }

    @Transactional
    public String issuePasswordResetToken(User user) {
        passwordResetTokenRepository.invalidateAllActiveForUser(user.getId(), Instant.now());

        String rawToken = TokenHasher.generateRawToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(TokenHasher.hashToken(rawToken));
        resetToken.setExpiresAt(Instant.now().plusMillis(authProperties.passwordResetExpirationMs()));
        passwordResetTokenRepository.save(resetToken);
        return rawToken;
    }

    @Transactional
    public PasswordResetToken validatePasswordResetToken(String rawToken) {
        return passwordResetTokenRepository.findActiveByTokenHash(TokenHasher.hashToken(rawToken))
                .filter(PasswordResetToken::isValid)
                .orElse(null);
    }

    @Transactional
    public void consumePasswordResetToken(PasswordResetToken token) {
        token.markUsed();
        passwordResetTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllRefreshTokensForUser(Long userId) {
        refreshTokenRepository.revokeAllActiveForUser(userId, Instant.now());
    }
}
