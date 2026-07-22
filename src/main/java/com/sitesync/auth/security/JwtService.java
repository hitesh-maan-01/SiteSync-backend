package com.sitesync.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Issues short-lived access tokens (validated on every request via
 * JwtAuthenticationFilter) and pairs them with long-lived opaque refresh
 * tokens (see RefreshTokenService - refresh tokens are NOT JWTs here,
 * they're random opaque strings hashed and stored in refresh_tokens,
 * which is simpler to revoke than trying to blacklist JWTs).
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-minutes:15}") long accessTokenExpiryMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiryMillis = accessTokenExpiryMinutes * 60 * 1000;
    }

    public String generateAccessToken(UUID userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseAndValidate(token).getSubject());
    }

    public String extractRole(String token) {
        return parseAndValidate(token).get("role", String.class);
    }
}