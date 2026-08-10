package com.relay.modules.identity.api;

import com.relay.common.dto.ApiResponse;
import com.relay.modules.identity.api.dto.AccessTokenResponse;
import com.relay.modules.identity.api.dto.AuthResponse;
import com.relay.modules.identity.api.dto.ForgotPasswordRequest;
import com.relay.modules.identity.api.dto.LoginRequest;
import com.relay.modules.identity.api.dto.MessageResponse;
import com.relay.modules.identity.api.dto.RegisterRequest;
import com.relay.modules.identity.api.dto.ResetPasswordRequest;
import com.relay.modules.identity.api.dto.UserDto;
import com.relay.modules.identity.service.AuthService;
import com.relay.modules.identity.service.RefreshTokenCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.relay.config.security.AuthenticatedUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthService.AuthResult result = authService.register(
                request,
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );
        refreshTokenCookieService.setRefreshTokenCookie(httpResponse, result.refreshToken());
        return ApiResponse.ok(result.response());
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthService.AuthResult result = authService.login(
                request,
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );
        refreshTokenCookieService.setRefreshTokenCookie(httpResponse, result.refreshToken());
        return ApiResponse.ok(result.response());
    }

    @PostMapping("/refresh")
    public ApiResponse<AccessTokenResponse> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String rawRefreshToken = refreshTokenCookieService.extractRefreshToken(httpRequest);
        AuthService.RefreshResult result = authService.refreshSession(
                rawRefreshToken,
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );
        refreshTokenCookieService.setRefreshTokenCookie(httpResponse, result.refreshToken());
        return ApiResponse.ok(new AccessTokenResponse(result.accessToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(refreshTokenCookieService.extractRefreshToken(httpRequest));
        refreshTokenCookieService.clearRefreshTokenCookie(httpResponse);
        return ApiResponse.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.ok(authService.resetPassword(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> getMe(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(authService.getMe(user.userId()));
    }
}
