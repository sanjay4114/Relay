package com.relay.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        StorageProperties.class,
        AuthProperties.class
})
public class AppPropertiesConfig {
}
