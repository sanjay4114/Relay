package com.relay.modules.identity.exception;

import com.relay.common.exception.RelayException;
import org.springframework.http.HttpStatus;

public final class AuthErrors {

    private AuthErrors() {
    }

    public static RelayException emailAlreadyExists() {
        return new RelayException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "An account with this email already exists");
    }

    public static RelayException invalidCredentials() {
        return new RelayException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
    }

    public static RelayException invalidRefreshToken() {
        return new RelayException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
    }

    public static RelayException invalidResetToken() {
        return new RelayException(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN", "Password reset link is invalid or expired");
    }
}
