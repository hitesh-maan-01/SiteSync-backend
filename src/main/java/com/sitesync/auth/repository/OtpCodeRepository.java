package com.sitesync.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sitesync.auth.entity.OtpCode;

import java.util.List;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    /** Most recent, unconsumed codes for this identifier+purpose — used to verify. */
    List<OtpCode> findByIdentifierAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            String identifier, OtpCode.OtpPurpose purpose);
}