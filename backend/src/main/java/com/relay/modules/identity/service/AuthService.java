package com.relay.modules.identity.service;

import com.relay.config.security.JwtTokenProvider;
import com.relay.modules.identity.api.dto.AuthResponse;
import com.relay.modules.identity.api.dto.ForgotPasswordRequest;
import com.relay.modules.identity.api.dto.LoginRequest;
import com.relay.modules.identity.api.dto.MessageResponse;
import com.relay.modules.identity.api.dto.RegisterRequest;
import com.relay.modules.identity.api.dto.ResetPasswordRequest;
import com.relay.modules.identity.domain.PasswordResetToken;
import com.relay.modules.identity.domain.RefreshToken;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.api.dto.UserDto;
import com.relay.modules.identity.exception.AuthErrors;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.service.WorkspaceBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final WorkspaceBootstrapService workspaceBootstrapService;
    private final AuthMailService authMailService;

    @Transactional
    public AuthResult register(RegisterRequest request, String userAgent, String ipAddress) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email().toLowerCase().trim())) {
            throw AuthErrors.emailAlreadyExists();
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.displayName()
        );
        userRepository.save(user);

        Workspace workspace = workspaceBootstrapService.bootstrapForNewUser(user);
        return buildAuthResult(user, workspace, userAgent, ipAddress);
    }

    @Transactional
    public AuthResult login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email().toLowerCase().trim())
                .orElseThrow(AuthErrors::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw AuthErrors.invalidCredentials();
        }

        Workspace workspace = workspaceBootstrapService.findDefaultWorkspaceForUser(user)
                .orElseThrow(() -> new IllegalStateException("User has no workspace"));

        return buildAuthResult(user, workspace, userAgent, ipAddress);
    }

    @Transactional
    public RefreshResult refreshSession(String rawRefreshToken, String userAgent, String ipAddress) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(rawRefreshToken);
        if (refreshToken == null) {
            throw AuthErrors.invalidRefreshToken();
        }

        User user = refreshToken.getUser();
        String newRefreshToken = tokenService.rotateRefreshToken(refreshToken, userAgent, ipAddress);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getPublicId(), user.getEmail());
        return new RefreshResult(accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        RefreshToken token = tokenService.validateRefreshToken(rawRefreshToken);
        if (token != null) {
            tokenService.revokeRefreshToken(token);
        }
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailAndDeletedAtIsNull(request.email().toLowerCase().trim())
                .ifPresent(user -> {
                    String rawToken = tokenService.issuePasswordResetToken(user);
                    authMailService.sendPasswordResetEmail(user.getEmail(), rawToken);
                });

        return new MessageResponse("If an account exists for that email, a reset link has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenService.validatePasswordResetToken(request.token());
        if (resetToken == null) {
            throw AuthErrors.invalidResetToken();
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenService.consumePasswordResetToken(resetToken);
        tokenService.revokeAllRefreshTokensForUser(user.getId());

        return new MessageResponse("Password has been reset. Please sign in with your new password.");
    }

    @Transactional(readOnly = true)
    public UserDto getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return AuthMapper.toUserDto(user);
    }

    private AuthResult buildAuthResult(User user, Workspace workspace, String userAgent, String ipAddress) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getPublicId(), user.getEmail());
        String refreshToken = tokenService.issueRefreshToken(user, userAgent, ipAddress);

        AuthResponse response = new AuthResponse(
                accessToken,
                AuthMapper.toUserDto(user),
                AuthMapper.toWorkspaceDto(workspace)
        );
        return new AuthResult(response, refreshToken);
    }

    public record AuthResult(AuthResponse response, String refreshToken) {
    }

    public record RefreshResult(String accessToken, String refreshToken) {
    }
}
