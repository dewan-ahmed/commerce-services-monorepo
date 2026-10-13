package com.acme.platform.web.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntraPropertiesTest {
    private static final UUID TENANT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void derivesTenantSpecificEndpoints() {
        JwtProperties jwt = new EntraProperties(TENANT, "catalog-api", "catalog.read").jwtProperties();
        assertEquals("https://login.microsoftonline.com/" + TENANT + "/v2.0", jwt.issuerUri().toString());
        assertEquals("https://login.microsoftonline.com/" + TENANT + "/discovery/v2.0/keys",
                jwt.jwkSetUri().toString());
    }

    @Test
    void rejectsMissingTenant() {
        assertThrows(IllegalArgumentException.class, () -> new EntraProperties(null, "catalog-api", "catalog.read"));
    }

    @Test
    void rejectsMissingAudience() {
        assertThrows(IllegalArgumentException.class, () -> new EntraProperties(TENANT, "", "catalog.read"));
    }
}
