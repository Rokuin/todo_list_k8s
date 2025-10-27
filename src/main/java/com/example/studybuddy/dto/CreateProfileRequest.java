package com.example.studybuddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateProfileRequest DTO
 *
 * REQUEST DTO = Data coming FROM the client
 *
 * WHY SEPARATE FROM ENTITY?
 * ==========================
 * 1. SECURITY: Client can't set id, createdAt, updatedAt
 * 2. VALIDATION: Input is validated before touching the database
 * 3. CLEAN API: Only expose fields needed for creation
 *
 * BEAN VALIDATION ANNOTATIONS:
 * ============================
 * These annotations automatically validate when you use @Valid in controller
 *
 * @NotBlank:
 * - Checks: not null, not empty, not whitespace-only
 * - For Strings only
 * - Example: "   " would FAIL (whitespace-only)
 *
 * @Size:
 * - Checks: length constraints
 * - Works on: String, Collection, Map, Array
 * - min = minimum length (inclusive)
 * - max = maximum length (inclusive)
 *
 * @Email:
 * - Checks: valid email format (RFC 5322)
 * - Examples: "user@example.com" ✓, "invalid" ✗
 * - Note: Allows "user@localhost" (valid by RFC, might want custom validation)
 *
 * message:
 * - Custom error message shown when validation fails
 * - Default messages are generic: "must not be blank"
 * - Custom messages are user-friendly: "Name is required"
 */
public class CreateProfileRequest {

    /**
     * Profile name
     *
     * Validation:
     * - NOT blank (required, not empty/whitespace)
     * - Length: 1-100 characters
     *
     * Why @NotBlank instead of @NotNull?
     * - @NotNull: Allows empty string "" ✗
     * - @NotBlank: Requires actual content ✓
     */
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    /**
     * Email address
     *
     * Validation:
     * - NOT blank (required)
     * - Valid email format
     * - Length: 1-255 characters
     *
     * Note: Uniqueness is NOT checked here!
     * Reason: Validation annotations can't access database
     * Solution: Service layer checks uniqueness before saving
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * No-args constructor - REQUIRED for Jackson (JSON deserialization)
     *
     * When Spring Boot receives JSON:
     * {
     *   "name": "John",
     *   "email": "john@example.com"
     * }
     *
     * Jackson (JSON library):
     * 1. Creates instance: new CreateProfileRequest()
     * 2. Sets fields: setName("John"), setEmail("john@example.com")
     */
    public CreateProfileRequest() {
    }

    /**
     * All-args constructor - Convenience for testing
     */
    public CreateProfileRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================
    // Required for Jackson JSON serialization/deserialization

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ============================================
    // TOSTRING (for logging/debugging)
    // ============================================

    @Override
    public String toString() {
        return "CreateProfileRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
