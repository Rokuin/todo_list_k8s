package com.example.studybuddy.exception;

/**
 * InvalidRequestException
 *
 * CUSTOM EXCEPTION for "Invalid request" scenarios
 *
 * Examples:
 * - Business rule violation: "Cannot delete subject with existing plan items"
 * - Invalid state transition: "Cannot mark plan item as done when it's already done"
 * - Invalid reference: "Subject ID 999 does not belong to profile 1"
 *
 * Maps to: 400 BAD REQUEST HTTP status
 *
 * WHEN TO USE:
 * ============
 * - Bean Validation handles SYNTAX errors (@NotBlank, @Email)
 * - This exception handles SEMANTIC errors (business rules)
 *
 * Example:
 * Bean Validation: "Email must be valid" (syntax)
 * This exception: "Email already registered" (business rule)
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
