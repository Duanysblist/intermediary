package com.dduany.intermediary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;
import java.util.List;

/**
 * Deployment settings, all overridable through environment variables (see application.yaml).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Auth auth, Cors cors, Ai ai, String timezone) {

    public ZoneId zone() {
        return ZoneId.of(timezone == null || timezone.isBlank() ? "UTC" : timezone);
    }

    public record Auth(String username, String password, String jwtSecret, int tokenTtlHours) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Ai(String apiKey, String model) {
        public boolean enabled() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
