package com.example.studybuddy.dto;

import java.time.Instant;

/**
 * SubjectResponse DTO
 *
 * Response when fetching subjects
 */
public class SubjectResponse {

    private Long id;

    /**
     * Profile ID instead of entire Profile object
     *
     * Why just ID?
     * - Avoid circular references (Subject → Profile → List<Subject> → ...)
     * - Smaller JSON payload
     * - Client usually already knows the profileId
     *
     * If client needs full profile, they can:
     * 1. Make separate request: GET /profiles/{id}
     * 2. Or we create a "detailed" response DTO with full profile
     */
    private Long profileId;

    private String name;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SubjectResponse() {
    }

    public SubjectResponse(Long id, Long profileId, String name, String description,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.profileId = profileId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
