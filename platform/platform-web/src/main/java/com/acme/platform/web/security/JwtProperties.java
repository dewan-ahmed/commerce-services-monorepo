package com.acme.platform.web.security;

import java.net.URI;

public record JwtProperties(URI issuerUri, URI jwkSetUri, String audience, String requiredScope) {
    public JwtProperties {
        requireHttpsUri(issuerUri, "issuer-uri");
        requireHttpsUri(jwkSetUri, "jwk-set-uri");
        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("platform.security.jwt.audience must not be blank");
        }
        if (requiredScope == null || !requiredScope.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalArgumentException("platform.security.jwt.required-scope must be a single scope");
        }
    }

    private static void requireHttpsUri(URI uri, String propertyName) {
        if (uri == null
                || !uri.isAbsolute()
                || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException(
                    "platform.security.jwt." + propertyName + " must be an HTTPS URI without user info");
        }
    }
}
