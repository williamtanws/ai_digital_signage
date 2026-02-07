package io.jeecloud.aidigitalsignage.common.infrastructure.rest.common;

import java.time.Instant;
import java.util.Map;

/**
 * Standard API response wrapper for consistent response format.
 */
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp,
    Map<String, Object> metadata
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now(), null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now(), null);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, Instant.now(), null);
    }
    
    public static <T> ApiResponse<T> error(String message, Map<String, Object> metadata) {
        return new ApiResponse<>(false, null, message, Instant.now(), metadata);
    }
}
