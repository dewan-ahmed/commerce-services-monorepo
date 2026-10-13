package com.acme.platform.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformSecurityConfigurationTest {
    private static final JwtProperties PROPERTIES = new JwtProperties(
            URI.create("https://identity.example.test/issuer"),
            URI.create("https://identity.example.test/jwks"),
            "catalog-api",
            "catalog.read");

    private final OAuth2TokenValidator<Jwt> validator =
            PlatformSecurityConfiguration.jwtValidator(PROPERTIES);

    @Test
    void acceptsJwtWithExpectedIssuerAudienceAndLifetime() {
        assertFalse(validator.validate(jwt(
                "https://identity.example.test/issuer",
                List.of("catalog-api"),
                Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void rejectsJwtWithUnexpectedIssuer() {
        assertTrue(validator.validate(jwt(
                "https://attacker.example.test/",
                List.of("catalog-api"),
                Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void rejectsJwtWithUnexpectedAudience() {
        assertTrue(validator.validate(jwt(
                "https://identity.example.test/issuer",
                List.of("other-api"),
                Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void rejectsExpiredJwt() {
        assertTrue(validator.validate(jwt(
                "https://identity.example.test/issuer",
                List.of("catalog-api"),
                Instant.now().minusSeconds(120))).hasErrors());
    }

    @Test
    void rejectsJwtWithoutExpiry() {
        Jwt token = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuer(PROPERTIES.issuerUri().toString())
                .audience(List.of(PROPERTIES.audience()))
                .build();
        assertTrue(validator.validate(token).hasErrors());
    }

    private Jwt jwt(String issuer, List<String> audience, Instant expiresAt) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuer(issuer)
                .audience(audience)
                .issuedAt(expiresAt.minusSeconds(3600))
                .expiresAt(expiresAt)
                .build();
    }
}
