package com.sitesync.auth.dto;

import java.util.Map;

/**
 * Matches Flutter's ApiResponseModel.fromJson exactly:
 * { "success": bool, "data": {...} | null, "message": string, "errors": {...} | null }
 *
 * Every controller endpoint returns this shape, whether it succeeded or
 * failed - Flutter's repositories check success/data explicitly rather
 * than relying on HTTP status codes alone for business-logic failures
 * (though 401/403/etc. status codes are still set correctly - see
 * GlobalExceptionHandler - since ApiClient's Dio interceptor also branches
 * on status code for auth-failure detection).
 */
public class ApiResponse<T> {
    public boolean success;
    public T data;
    public String message;
    public Map<String, String> errors;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = "OK";
        return response;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> response = ok(data);
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> fail(String message, Map<String, String> errors) {
        ApiResponse<T> response = fail(message);
        response.errors = errors;
        return response;
    }
}