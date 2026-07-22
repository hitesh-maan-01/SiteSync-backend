package com.sitesync.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * All request DTOs in one file for easy cross-reference against
 * auth_repository.dart's request bodies - each inner class's field names
 * must match exactly what Flutter sends.
 */
public class AuthRequests {

    /** POST /api/auth/login/password - body: { identifier, password } */
    public static class LoginPasswordRequest {
        @NotBlank public String identifier;
        @NotBlank public String password;
    }

    /** POST /api/auth/otp/request - body: { identifier } */
    public static class OtpRequestRequest {
        @NotBlank public String identifier;
    }

    /** POST /api/auth/otp/verify - body: { identifier, otp } */
    public static class OtpVerifyRequest {
        @NotBlank public String identifier;
        @NotBlank public String otp;
    }

    /** POST /api/auth/register - body: { name, identifier, password, companyName } */
    public static class RegisterRequest {
        @NotBlank public String name;
        @NotBlank public String identifier;
        @NotBlank public String password;
        @NotBlank public String companyName;
    }

    /** POST /api/auth/register/verify - body: { identifier, otp } */
    public static class RegisterVerifyRequest {
        @NotBlank public String identifier;
        @NotBlank public String otp;
    }

    /** POST /api/auth/refresh - body: { refreshToken } */
    public static class RefreshRequest {
        @NotBlank public String refreshToken;
    }

    /** POST /api/auth/fcm-token - body: { token } */
    public static class FcmTokenRequest {
        @NotBlank public String token;
    }
}