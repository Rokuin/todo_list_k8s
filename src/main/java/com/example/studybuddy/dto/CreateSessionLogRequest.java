package com.example.studybuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * CreateSessionLogRequest DTO
 *
 * For recording a study session
 */
public class CreateSessionLogRequest {

    @NotNull(message = "Profile ID is required")
    private Long profileId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    /**
     * Optional link to plan item
     *
     * Use cases:
     * - Planned study: Set planItemId (links session to plan)
     * - Spontaneous study: Leave null (just track time)
     */
    private Long planItemId;

    /**
     * When did the study session happen?
     *
     * Optional in request:
     * - If provided: Use client's timestamp (for manual entry of past sessions)
     * - If null: Service sets to current time (most common case)
     *
     * Example (manual entry):
     * "I studied yesterday but forgot to log it"
     * Client sends: studiedAt = "2024-01-14T10:00:00Z"
     */
    private Instant studiedAt;

    /**
     * Duration in minutes
     *
     * @Min(1): Must study at least 1 minute
     * (Prevents accidental 0 or negative values)
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    /**
     * Optional notes
     *
     * Examples:
     * - "Finished Chapter 5"
     * - "Struggled with problem 12, need to review"
     * - "Great progress today!"
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public CreateSessionLogRequest() {
    }

    public CreateSessionLogRequest(Long profileId, Long subjectId, Integer durationMinutes) {
        this.profileId = profileId;
        this.subjectId = subjectId;
        this.durationMinutes = durationMinutes;
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

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(Long planItemId) {
        this.planItemId = planItemId;
    }

    public Instant getStudiedAt() {
        return studiedAt;
    }

    public void setStudiedAt(Instant studiedAt) {
        this.studiedAt = studiedAt;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
