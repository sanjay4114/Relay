package com.relay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "relay.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
