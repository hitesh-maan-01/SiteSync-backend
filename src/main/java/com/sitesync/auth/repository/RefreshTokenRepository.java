package com.sitesync.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sitesync.auth.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUserId(UUID userId);
}