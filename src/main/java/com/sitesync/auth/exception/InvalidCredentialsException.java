package com.sitesync.auth.exception;

import org.springframework.http.HttpStatus;

/** Wrong password, invalid/expired OTP, expired refresh token, etc. -> 401 -> AuthFailure in Flutter. */
public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}