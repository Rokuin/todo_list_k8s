package com.example.studybuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * CreatePlanItemRequest DTO
 *
 * For creating a study plan/task
 */
public class CreatePlanItemRequest {

    @NotNull(message = "Profile ID is required")
    private Long profileId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    /**
     * Optional deadline
     *
     * Why Instant instead of LocalDateTime?
     * - Instant = Point in time (UTC timezone)
     * - LocalDateTime = Date+time without timezone (ambiguous!)
     *
     * Example problem with LocalDateTime:
     * "2024-01-15T14:00" - Is this Tokyo time? New York time?
     *
     * Instant solves this:
     * "2024-01-15T14:00:00Z" - Z = UTC timezone (unambiguous!)
     *
     * Client sends ISO-8601 format:
     * "2024-01-15T14:00:00Z"
     * Jackson automatically converts to Instant
     */
    private Instant deadline;

    /**
     * Optional target study time in MINUTES
     *
     * @Min(1):
     * - Minimum value validation
     * - Only validates if value is present (null is OK)
     * - If user sends 0 or negative, validation fails
     *
     * Why Integer instead of int?
     * - Integer can be null (optional)
     * - int cannot be null (would default to 0)
     *
     * Why minutes instead of hours?
     * - Precision: 90 minutes exact, 1.5 hours has decimal issues
     * - Storage: Integer (exact) vs Float/Double (imprecise)
     */
    @Min(value = 1, message = "Target minutes must be at least 1")
    private Integer targetMinutes;

    // Note: status is NOT in request
    // Reason: New plan items are always OPEN (set by service layer)
    // Client shouldn't control initial status

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public CreatePlanItemRequest() {
    }

    public CreatePlanItemRequest(Long profileId, Long subjectId, String title) {
        this.profileId = profileId;
        this.subjectId = subjectId;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public Integer getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(Integer targetMinutes) {
        this.targetMinutes = targetMinutes;
    }
}
