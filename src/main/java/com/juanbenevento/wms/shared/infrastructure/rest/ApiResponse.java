package com.juanbenevento.wms.shared.infrastructure.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for consistent API responses across all endpoints.
 * 
 * Benefits:
 * - Consistent response structure for all clients
 * - Easy to parse metadata (timestamp, status)
 * - Support for pagination, metadata, and error handling
 * - Self-documenting with type information
 * 
 * Usage:
 * ```java
 * return ResponseEntity.ok(ApiResponse.success(data));
 * return ResponseEntity.ok(ApiResponse.success(data, "User created successfully"));
 * return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Order confirmed"));
 * return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
 * ```
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    T data,
    String message,
    boolean success,
    String code,
    LocalDateTime timestamp,
    Meta meta
) {
    
    /**
     * Create a successful response with data only.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .data(data)
            .success(true)
            .timestamp(now())
            .build();
    }
    
    /**
     * Create a successful response with data and message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .data(data)
            .message(message)
            .success(true)
            .timestamp(now())
            .build();
    }
    
    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .message(message)
            .success(false)
            .timestamp(now())
            .build();
    }
    
    /**
     * Create an error response with code.
     */
    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
            .message(message)
            .code(code)
            .success(false)
            .timestamp(now())
            .build();
    }
    
    /**
     * Create an error response with data (for validation errors).
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
            .message(message)
            .data(data)
            .success(false)
            .timestamp(now())
            .build();
    }
    
    /**
     * Create a successful response with pagination metadata.
     */
    public static <T> ApiResponse<T> successPaginated(T data, PaginationMeta pagination) {
        return ApiResponse.<T>builder()
            .data(data)
            .success(true)
            .timestamp(now())
            .meta(pagination)
            .build();
    }
    
    private static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    // ==================== Nested Types ====================
    
    /**
     * Metadata for paginated responses.
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Meta(
        PaginationMeta pagination,
        String requestId,
        Integer totalCount
    ) {}
    
    /**
     * Pagination information.
     */
    @Builder
    public record PaginationMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
    ) {
        public static PaginationMeta of(int page, int size, long totalElements) {
            int totalPages = (int) Math.ceil((double) totalElements / size);
            return PaginationMeta.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
        }
    }
    
    // ==================== Factory Methods for Common Responses ====================
    
    /**
     * Response for created resources (201).
     */
    public static <T> ApiResponse<T> created(T data) {
        return success(data, "Resource created successfully");
    }
    
    /**
     * Response for updated resources (200).
     */
    public static <T> ApiResponse<T> updated(T data) {
        return success(data, "Resource updated successfully");
    }
    
    /**
     * Response for deleted resources (200 with no content equivalent).
     */
    public static <T> ApiResponse<T> deleted() {
        return success(null, "Resource deleted successfully");
    }
    
    /**
     * Response for not found (404).
     */
    public static <T> ApiResponse<T> notFound(String resource) {
        return error(resource + " not found", "NOT_FOUND");
    }
    
    /**
     * Response for validation errors (400).
     */
    public static <T> ApiResponse<T> validationError(String message, T errors) {
        return error(message, errors);
    }
    
    /**
     * Response for authentication errors (401).
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(message, "UNAUTHORIZED");
    }
    
    /**
     * Response for forbidden access (403).
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(message, "FORBIDDEN");
    }
    
    /**
     * Response for conflict (409).
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return error(message, "CONFLICT");
    }
    
    /**
     * Response for internal server errors (500).
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(message, "INTERNAL_ERROR");
    }
}