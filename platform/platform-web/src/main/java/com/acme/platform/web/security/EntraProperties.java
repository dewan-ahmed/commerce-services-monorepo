package com.acme.platform.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.UUID;

@ConfigurationProperties(prefix = "platform.security.entra")
public record EntraProperties(UUID tenantId, String audience, String requiredScope) {
    public EntraProperties {
        if (tenantId == null) {
            throw new IllegalArgumentException("ENTRA_TENANT_ID must be a tenant UUID");
        }
        if (audience == null || audience.isBlank() || audience.contains("${")) {
            throw new IllegalArgumentException("ENTRA_AUDIENCE must be configured");
        }
    }

    public JwtProperties jwtProperties() {
        String tenantBase = "https://login.microsoftonline.com/" + tenantId;
        return new JwtProperties(
                URI.create(tenantBase + "/v2.0"),
                URI.create(tenantBase + "/discovery/v2.0/keys"),
                audience,
                requiredScope);
    }
}
