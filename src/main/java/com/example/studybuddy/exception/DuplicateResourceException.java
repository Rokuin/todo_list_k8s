package com.example.studybuddy.exception;

/**
 * DuplicateResourceException
 *
 * CUSTOM EXCEPTION for "Resource already exists" scenarios
 *
 * Examples:
 * - POST /profiles with email that already exists
 * - Unique constraint violation
 *
 * Maps to: 409 CONFLICT HTTP status
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
