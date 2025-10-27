package com.example.studybuddy.exception;

/**
 * ResourceNotFoundException
 *
 * CUSTOM EXCEPTION for "Resource not found" scenarios
 *
 * Examples:
 * - GET /profiles/999 → Profile with ID 999 doesn't exist
 * - GET /subjects/123?profileId=1 → Subject 123 not found or doesn't belong to profile 1
 *
 * WHY CUSTOM EXCEPTION?
 * =====================
 * 1. Semantic: Clearly indicates "not found" scenario
 * 2. HTTP Status: Can map to 404 NOT FOUND
 * 3. Handling: @ControllerAdvice can catch and format error response
 *
 * EXTENDS RuntimeException:
 * - RuntimeException = Unchecked exception (no need for try-catch)
 * - Spring will automatically rollback transaction on unchecked exceptions
 * - Clean code: throw new ResourceNotFoundException(...) without throws declaration
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with message only
     *
     * Usage:
     * throw new ResourceNotFoundException("Profile not found with id: " + id);
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * Usage (wrap another exception):
     * catch (SomeException e) {
     *     throw new ResourceNotFoundException("Profile not found", e);
     * }
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
