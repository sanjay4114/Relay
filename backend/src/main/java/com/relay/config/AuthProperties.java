package com.relay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "relay.auth")
public record AuthProperties(
        String frontendUrl,
        long passwordResetExpirationMs,
        String refreshCookieName,
        boolean refreshCookieSecure
) {
}
