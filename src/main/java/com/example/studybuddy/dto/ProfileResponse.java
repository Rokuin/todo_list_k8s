package com.example.studybuddy.dto;

import java.time.Instant;

/**
 * ProfileResponse DTO
 *
 * RESPONSE DTO = Data going TO the client
 *
 * WHY SEPARATE FROM ENTITY?
 * ==========================
 * 1. CLEAN: Don't expose internal relationships (subjects, planItems, etc.)
 * 2. CONTROL: Choose exactly which fields to return
 * 3. PERFORMANCE: Avoid lazy-loading issues and circular references
 * 4. VERSIONING: Can change entity without breaking API
 *
 * NO VALIDATION ANNOTATIONS:
 * ==========================
 * Response DTOs don't need validation because:
 * - Data comes from our database (already validated)
 * - We control what goes into response
 *
 * But they should be IMMUTABLE (read-only) for safety
 * - Use final fields
 * - No setters
 * - Constructor to set all values
 */
public class ProfileResponse {

    /**
     * Profile ID
     *
     * Why include ID in response?
     * - Client needs it to make subsequent requests
     * - Example: POST /profiles → returns ID → client uses it for POST /subjects
     */
    private Long id;

    private String name;

    private String email;

    /**
     * Timestamps
     *
     * Why return timestamps?
     * - Useful for UI: "Created 2 days ago"
     * - Debugging: When was this created?
     * - Sync: Has data changed since last fetch?
     *
     * Instant serializes to ISO-8601 format:
     * "2024-01-15T10:30:00Z"
     */
    private Instant createdAt;

    private Instant updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public ProfileResponse() {
    }

    /**
     * All-args constructor
     *
     * Used in service layer to convert Entity → DTO:
     * return new ProfileResponse(
     *     profile.getId(),
     *     profile.getName(),
     *     profile.getEmail(),
     *     profile.getCreatedAt(),
     *     profile.getUpdatedAt()
     * );
     */
    public ProfileResponse(Long id, String name, String email, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============================================
    // GETTERS ONLY (read-only DTO)
    // ============================================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters included for Jackson, but ideally use constructor
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
