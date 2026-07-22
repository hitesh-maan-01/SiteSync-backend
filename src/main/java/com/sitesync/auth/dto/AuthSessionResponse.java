package com.sitesync.auth.dto;

/**
 * Matches auth_repository.dart's private _AuthPayload.fromJson exactly:
 * { "accessToken": string, "refreshToken": string, "user": {...} }
 * Returned by login (password/OTP) and signup-verify - same shape both
 * times, since signup auto-logs-in on success.
 */
public class AuthSessionResponse {
    public String accessToken;
    public String refreshToken;
    public UserResponse user;

    public AuthSessionResponse(String accessToken, String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}