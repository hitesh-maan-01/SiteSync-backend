package com.sitesync.auth.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.auth.dto.AuthRequests.*;
import com.sitesync.auth.dto.AuthSessionResponse;
import com.sitesync.auth.dto.RefreshResponse;
import com.sitesync.auth.dto.UserResponse;
import com.sitesync.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Every mapping here corresponds exactly to a constant in
 * api_endpoints.dart - path, HTTP method, and request/response shape all
 * checked directly against auth_repository.dart before writing this.
 * Do not rename a path here without updating that file (and vice versa).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/login/password")
    public ResponseEntity<ApiResponse<AuthSessionResponse>> loginWithPassword(
            @Valid @RequestBody LoginPasswordRequest request
    ) {
        AuthSessionResponse session = authService.loginWithPassword(request.identifier, request.password);
        return ResponseEntity.ok(ApiResponse.ok(session, "Logged in."));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<Void>> requestOtp(
            @Valid @RequestBody OtpRequestRequest request
    ) {
        authService.requestLoginOtp(request.identifier);
        return ResponseEntity.ok(ApiResponse.ok(null, "Code sent."));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthSessionResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request
    ) {
        AuthSessionResponse session = authService.verifyLoginOtp(request.identifier, request.otp);
        return ResponseEntity.ok(ApiResponse.ok(session, "Logged in."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request.name, request.identifier, request.password, request.companyName);
        return ResponseEntity.ok(ApiResponse.ok(null, "Verification code sent."));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthSessionResponse>> verifyRegistration(
            @Valid @RequestBody RegisterVerifyRequest request
    ) {
        AuthSessionResponse session = authService.verifyRegistrationOtp(request.identifier, request.otp);
        return ResponseEntity.ok(ApiResponse.ok(session, "Account created."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        RefreshResponse response = authService.refreshAccessToken(request.refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> currentUser(Authentication authentication) {
        UserResponse user = authService.getCurrentUser(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out."));
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            Authentication authentication,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        authService.registerFcmToken(currentUserId(authentication), request.token);
        return ResponseEntity.ok(ApiResponse.ok(null, "Token registered."));
    }
}