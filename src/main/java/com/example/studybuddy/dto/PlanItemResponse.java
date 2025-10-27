package com.example.studybuddy.dto;

import com.example.studybuddy.entity.PlanStatus;

import java.time.Instant;

/**
 * PlanItemResponse DTO
 *
 * Response when fetching plan items
 */
public class PlanItemResponse {

    private Long id;

    private Long profileId;

    private Long subjectId;

    /**
     * Include subject name for convenience
     *
     * Alternative approaches:
     * 1. Return only subjectId (client makes another request)
     * 2. Return full SubjectResponse object (nested DTO)
     * 3. Return just name (our choice - simple and useful)
     *
     * This avoids client needing to fetch subject details
     * just to display "Math: Complete Chapter 5"
     */
    private String subjectName;

    private String title;

    private Instant deadline;

    private Integer targetMinutes;

    /**
     * Status as enum
     *
     * JSON will serialize as: "status": "OPEN" or "DONE"
     * Jackson handles enum serialization automatically
     */
    private PlanStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public PlanItemResponse() {
    }

    public PlanItemResponse(Long id, Long profileId, Long subjectId, String subjectName,
                           String title, Instant deadline, Integer targetMinutes,
                           PlanStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.profileId = profileId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.title = title;
        this.deadline = deadline;
        this.targetMinutes = targetMinutes;
        this.status = status;
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

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
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
