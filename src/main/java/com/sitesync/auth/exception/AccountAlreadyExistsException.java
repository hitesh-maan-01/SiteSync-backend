package com.sitesync.auth.exception;

import org.springframework.http.HttpStatus;

/** Email/phone already registered -> 409 -> ServerFailure(message) in Flutter, message shown to user. */
public class AccountAlreadyExistsException extends AuthException {
    public AccountAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}