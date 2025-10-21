package com.example.studybuddy.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * SessionLog Entity - Represents an actual study session
 *
 * Example: "Studied Math for 90 minutes on 2024-01-15"
 * This is the ACTUAL record of what you studied (vs PlanItem which is the PLAN)
 *
 * Think of it like:
 * - PlanItem = Your calendar appointment
 * - SessionLog = What you actually did
 */
@Entity
@Table(name = "session_logs")
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MANY-TO-ONE: Many sessions belong to ONE profile
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessionlog_profile"))
    private Profile profile;

    /**
     * MANY-TO-ONE: Many sessions belong to ONE subject
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessionlog_subject"))
    private Subject subject;

    /**
     * OPTIONAL MANY-TO-ONE: A session MAY be linked to a plan item
     * nullable = true - Not all study sessions need to be linked to a plan
     *
     * Use case:
     * - Planned study: Link to PlanItem
     * - Spontaneous study: No link (just track time spent)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_item_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_sessionlog_planitem"))
    private PlanItem planItem;

    /**
     * When did this study session happen?
     * Important for:
     * - Daily/weekly/monthly statistics
     * - Timezone: Stored in UTC (from our config)
     */
    @Column(name = "studied_at", nullable = false)
    private Instant studiedAt;

    /**
     * Duration in minutes
     * Example: 90 minutes = 1.5 hours
     *
     * Why minutes? Avoids decimal precision issues
     * Why Integer? Study sessions won't exceed Integer.MAX_VALUE minutes (4 million years!)
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Optional notes about the session
     * Example: "Finished Chapter 5, struggled with problem 12"
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Timestamps - When was this record created/updated?
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Lifecycle callbacks
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        // Default studied_at to now if not set
        if (studiedAt == null) {
            studiedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SessionLog() {
    }

    public SessionLog(Profile profile, Subject subject, Integer durationMinutes) {
        this.profile = profile;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.studiedAt = Instant.now();
    }

    public SessionLog(Profile profile, Subject subject, PlanItem planItem, Integer durationMinutes) {
        this.profile = profile;
        this.subject = subject;
        this.planItem = planItem;
        this.durationMinutes = durationMinutes;
        this.studiedAt = Instant.now();
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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public PlanItem getPlanItem() {
        return planItem;
    }

    public void setPlanItem(PlanItem planItem) {
        this.planItem = planItem;
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

    // ============================================
    // BUSINESS LOGIC HELPERS
    // ============================================

    /**
     * Get duration in hours (as double)
     * Useful for display: "Studied for 1.5 hours"
     */
    public double getDurationHours() {
        return durationMinutes / 60.0;
    }
}
