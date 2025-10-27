package com.example.studybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * CreateSubjectRequest DTO
 *
 * For creating a new subject (Math, English, etc.)
 */
public class CreateSubjectRequest {

    /**
     * Profile ID - WHO owns this subject?
     *
     * @NotNull (not @NotBlank):
     * - @NotNull: For any object (Long, Integer, Boolean, etc.)
     * - @NotBlank: Only for Strings
     *
     * Why include profileId here?
     * - Every subject MUST belong to a profile (foreign key not null)
     * - Alternative: Get from authentication token (when we add JWT auth later)
     *
     * Current approach (no auth):
     * POST /subjects
     * {
     *   "profileId": 123,
     *   "name": "Math"
     * }
     *
     * Future approach (with auth):
     * POST /subjects
     * {
     *   "name": "Math"
     * }
     * // profileId extracted from JWT token
     */
    @NotNull(message = "Profile ID is required")
    private Long profileId;

    /**
     * Subject name
     */
    @NotBlank(message = "Subject name is required")
    @Size(min = 1, max = 100, message = "Subject name must be between 1 and 100 characters")
    private String name;

    /**
     * Optional description
     *
     * NO validation annotations = Optional field
     * - Client can omit this field
     * - Client can send null
     * - Client can send empty string
     *
     * If you want to allow null but NOT empty string:
     * @Size(min = 1, max = 500, message = "...")
     * // Size allows null, but if present, must be 1-500 chars
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public CreateSubjectRequest() {
    }

    public CreateSubjectRequest(Long profileId, String name) {
        this.profileId = profileId;
        this.name = name;
    }

    public CreateSubjectRequest(Long profileId, String name, String description) {
        this.profileId = profileId;
        this.name = name;
        this.description = description;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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
}
