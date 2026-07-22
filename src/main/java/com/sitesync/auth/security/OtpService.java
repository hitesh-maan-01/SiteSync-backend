package com.sitesync.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sitesync.auth.entity.OtpCode;
import com.sitesync.auth.exception.InvalidCredentialsException;
import com.sitesync.auth.repository.OtpCodeRepository;
import com.sitesync.auth.service.EmailService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

/**
 * Handles both login-OTP and signup-OTP - same underlying mechanism
 * (6-digit code, hashed at rest, expires after a few minutes), only the
 * purpose differs so a login code can't be replayed to verify a signup
 * or vice versa.
 *
 * Email-only for now per product decision - identifier passed in here is
 * assumed to already be validated as an email by the caller
 * (AuthService); this class doesn't itself distinguish email vs phone.
 */
@Service
public class OtpService {

    private static final int CODE_LENGTH = 6;
    private static final int MAX_VERIFY_ATTEMPTS_CONSIDERED = 5;

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private final int expiryMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            OtpCodeRepository otpCodeRepository,
            EmailService emailService,
            @Value("${app.otp.expiry-minutes:10}") int expiryMinutes
    ) {
        this.otpCodeRepository = otpCodeRepository;
        this.emailService = emailService;
        this.expiryMinutes = expiryMinutes;
    }

    public void generateAndSend(String email, OtpCode.OtpPurpose purpose) {
        String code = generateCode();

        OtpCode entity = new OtpCode();
        entity.setIdentifier(email);
        entity.setCodeHash(hash(code));
        entity.setPurpose(purpose);
        entity.setExpiresAt(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES));
        otpCodeRepository.save(entity);

        emailService.sendOtpEmail(email, code, purpose);
    }

    /**
     * Verifies a code against the most recent unconsumed codes for this
     * identifier+purpose. Throws InvalidCredentialsException (401 ->
     * AuthFailure) on any failure - wrong code, expired, or already used -
     * without distinguishing which, so as not to help someone brute-force
     * which failure mode they hit.
     */
    public void verify(String email, String code, OtpCode.OtpPurpose purpose) {
        List<OtpCode> candidates = otpCodeRepository
                .findByIdentifierAndPurposeAndConsumedFalseOrderByCreatedAtDesc(email, purpose);

        String hashedInput = hash(code);
        Instant now = Instant.now();

        OtpCode match = candidates.stream()
                .limit(MAX_VERIFY_ATTEMPTS_CONSIDERED)
                .filter(c -> c.getCodeHash().equals(hashedInput) && c.getExpiresAt().isAfter(now))
                .findFirst()
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired code."));

        match.setConsumed(true);
        otpCodeRepository.save(match);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", code);
    }

    private String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(code.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}