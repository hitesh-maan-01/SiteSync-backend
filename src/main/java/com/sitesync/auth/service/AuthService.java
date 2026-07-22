package com.sitesync.auth.service;

import com.sitesync.auth.dto.UserResponse;
import com.sitesync.auth.dto.AuthSessionResponse;
import com.sitesync.auth.dto.RefreshResponse;
import com.sitesync.auth.entity.OtpCode;
import com.sitesync.auth.entity.User;
import com.sitesync.auth.entity.UserRole;
import com.sitesync.auth.exception.AccountAlreadyExistsException;
import com.sitesync.auth.exception.InvalidCredentialsException;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.auth.security.JwtService;
import com.sitesync.auth.security.OtpService;
import com.sitesync.auth.security.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Orchestrates every auth flow AuthController exposes. Each public method
 * here corresponds 1:1 to a method on Flutter's AuthRepository - see that
 * file's doc comments for the exact request/response contract each of
 * these must satisfy.
 */
@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            OtpService otpService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.otpService = otpService;
    }

    private boolean isEmail(String identifier) {
        return EMAIL_PATTERN.matcher(identifier).matches();
    }

    private User findByIdentifier(String identifier) {
        return isEmail(identifier)
                ? userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."))
                : userRepository.findByPhoneNumber(identifier)
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid phone number or password."));
    }

    // -----------------------------------------------------------------
    // Password login - POST /api/auth/login/password
    // -----------------------------------------------------------------
    public AuthSessionResponse loginWithPassword(String identifier, String password) {
        User user = findByIdentifier(identifier);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        return issueSession(user);
    }

    // -----------------------------------------------------------------
    // Login OTP - POST /api/auth/otp/request, /api/auth/otp/verify
    // -----------------------------------------------------------------
    public void requestLoginOtp(String identifier) {
        if (!isEmail(identifier)) {
            throw new InvalidCredentialsException("OTP login currently supports email only.");
        }

        userRepository.findByEmail(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("No account found for this email."));

        otpService.generateAndSend(identifier, OtpCode.OtpPurpose.login);
    }

    public AuthSessionResponse verifyLoginOtp(String identifier, String otp) {
        otpService.verify(identifier, otp, OtpCode.OtpPurpose.login);
        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("No account found for this email."));
        return issueSession(user);
    }

    // -----------------------------------------------------------------
    // Signup - POST /api/auth/register, /api/auth/register/verify
    // -----------------------------------------------------------------
    /**
     * Creates the User row immediately with emailVerified = false, and
     * verifyRegistrationOtp() flips that flag on success - rather than
     * delaying account creation until verification, since
     * auth_repository.dart's verifyOtp() only sends identifier + otp
     * (not the password again), so there's nowhere to stash a pending
     * registration without its own table. This means an unverified row
     * can persist if someone never completes the OTP step - a scheduled
     * cleanup job for unverified accounts past their OTP expiry is a
     * reasonable addition later, not built here.
     */
    @Transactional
    public void register(String name, String identifier, String password, String companyName) {
        if (!isEmail(identifier)) {
            throw new InvalidCredentialsException("Signup currently supports email only.");
        }

        if (userRepository.existsByEmail(identifier)) {
            throw new AccountAlreadyExistsException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(identifier);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.CONTRACTOR);
        user.setCompanyName(companyName);
        user.setEmailVerified(false);
        userRepository.save(user);

        otpService.generateAndSend(identifier, OtpCode.OtpPurpose.signup);
    }

    public AuthSessionResponse verifyRegistrationOtp(String identifier, String otp) {
        otpService.verify(identifier, otp, OtpCode.OtpPurpose.signup);

        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("No pending signup found for this email."));
        user.setEmailVerified(true);
        userRepository.save(user);

        return issueSession(user);
    }

    // -----------------------------------------------------------------
    // Session management
    // -----------------------------------------------------------------
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Session expired. Please log in again."));
        return UserResponse.fromEntity(user);
    }

    public RefreshResponse refreshAccessToken(String rawRefreshToken) {
        UUID userId = refreshTokenService.validateAndGetUserId(rawRefreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Session expired. Please log in again."));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole().toWireValue());
        return new RefreshResponse(newAccessToken);
    }

    public void logout(UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    public void registerFcmToken(UUID userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Session expired. Please log in again."));
        user.setFcmToken(token);
        userRepository.save(user);
    }

    private AuthSessionResponse issueSession(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().toWireValue());
        String refreshToken = refreshTokenService.issue(user.getId());
        return new AuthSessionResponse(accessToken, refreshToken, UserResponse.fromEntity(user));
    }
}