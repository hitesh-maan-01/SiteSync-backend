package com.sitesync.auth.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sitesync.auth.entity.RefreshToken;
import com.sitesync.auth.exception.InvalidCredentialsException;
import com.sitesync.auth.repository.RefreshTokenRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

/**
 * Refresh tokens are opaque random strings (not JWTs) - the raw value is
 * only ever seen once, at issuance, and returned to the client. Only a
 * SHA-256 hash of it is stored, same reasoning as password storage: if the
 * database is ever read by someone unauthorized, they can't reconstruct
 * usable tokens from it.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiryDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-token-expiry-days:30}") long refreshTokenExpiryDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiryDays = refreshTokenExpiryDays;
    }

    /** Generates, hashes, stores, and returns the RAW token (store the hash, return the raw value once). */
    public String issue(UUID userId) {
        String rawToken = generateRawToken();

        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plus(refreshTokenExpiryDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(entity);

        return rawToken;
    }

    /**
     * Validates a raw refresh token and returns the associated userId.
     * Throws InvalidCredentialsException (401 -> AuthFailure in Flutter)
     * if the token is missing, expired, or revoked - matching
     * AuthRepository.refreshAccessToken()'s expectation that a failed
     * refresh surfaces as AuthFailure so the session gets cleared.
     */
    public UUID validateAndGetUserId(String rawToken) {
        RefreshToken entity = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidCredentialsException("Session expired. Please log in again."));

        if (entity.isRevoked() || entity.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Session expired. Please log in again.");
        }

        return entity.getUserId();
    }

    /** Invalidates every refresh token for a user - called on logout. */
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}