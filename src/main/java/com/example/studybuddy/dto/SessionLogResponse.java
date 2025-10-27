package com.example.studybuddy.dto;

import java.time.Instant;

/**
 * SessionLogResponse DTO
 *
 * Response when fetching session logs
 */
public class SessionLogResponse {

    private Long id;

    private Long profileId;

    private Long subjectId;

    /**
     * Include subject name for display
     */
    private String subjectName;

    /**
     * Optional plan item reference
     */
    private Long planItemId;

    /**
     * Optional plan item title for display
     */
    private String planItemTitle;

    private Instant studiedAt;

    private Integer durationMinutes;

    private String notes;

    private Instant createdAt;

    private Instant updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SessionLogResponse() {
    }

    public SessionLogResponse(Long id, Long profileId, Long subjectId, String subjectName,
                             Long planItemId, String planItemTitle, Instant studiedAt,
                             Integer durationMinutes, String notes,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.profileId = profileId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.planItemId = planItemId;
        this.planItemTitle = planItemTitle;
        this.studiedAt = studiedAt;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
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

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(Long planItemId) {
        this.planItemId = planItemId;
    }

    public String getPlanItemTitle() {
        return planItemTitle;
    }

    public void setPlanItemTitle(String planItemTitle) {
        this.planItemTitle = planItemTitle;
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
