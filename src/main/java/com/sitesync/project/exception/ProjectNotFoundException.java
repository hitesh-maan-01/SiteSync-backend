package com.sitesync.project.exception;

import org.springframework.http.HttpStatus;

/** No project with that id -> 404. NOT 401/403, so Flutter's ApiClient maps it to ServerFailure, not AuthFailure. */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}