package com.sitesync.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for auth-domain exceptions. Each carries the HTTP status
 * GlobalExceptionHandler should respond with - this matters because
 * Flutter's ApiClient.mapErrorToFailure branches specifically on status
 * code: 401/403 -> AuthFailure, anything else -> ServerFailure. Getting
 * the status wrong here silently changes which Failure type Flutter sees,
 * which changes how the Bloc/UI reacts (AuthFailure vs ServerFailure are
 * NOT interchangeable from Flutter's perspective).
 */
public abstract class AuthException extends RuntimeException {
    private final HttpStatus status;

    protected AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}