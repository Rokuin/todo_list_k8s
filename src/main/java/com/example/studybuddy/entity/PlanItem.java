package com.example.studybuddy.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * PlanItem Entity - Represents a study task/plan
 *
 * Example: "Complete Chapter 5", "Practice 50 problems", etc.
 * Each plan item has:
 * - A deadline
 * - Target study hours
 * - Status (OPEN or DONE)
 */
@Entity
@Table(name = "plan_items")
public class PlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MANY-TO-ONE: Many plan items belong to ONE profile
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_planitem_profile"))
    private Profile profile;

    /**
     * MANY-TO-ONE: Many plan items belong to ONE subject
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_planitem_subject"))
    private Subject subject;

    /**
     * Task title/description
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Deadline for this task
     * Can be null if no specific deadline
     */
    @Column(name = "deadline")
    private Instant deadline;

    /**
     * Target study hours for this task
     * Example: 5.5 hours
     *
     * Why Integer? We store in MINUTES to avoid decimal precision issues
     * 5.5 hours = 330 minutes
     * When showing to user: minutes / 60.0 = hours
     */
    @Column(name = "target_minutes")
    private Integer targetMinutes;

    /**
     * STATUS ENUM
     * @Enumerated(EnumType.STRING) - Store enum as string in database
     *   - STRING: Stores "OPEN" or "DONE" (readable, safe if you reorder enum)
     *   - ORDINAL: Stores 0 or 1 (smaller, but breaks if you reorder enum!)
     *
     * Best practice: Use STRING for enums
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status;

    /**
     * Timestamps
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * ONE-TO-MANY: One plan item can have many session logs
     * (Optional relationship - not all sessions need to link to a plan item)
     */
    @OneToMany(mappedBy = "planItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionLog> sessionLogs = new ArrayList<>();

    /**
     * Lifecycle callbacks
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        // Default status is OPEN if not set
        if (status == null) {
            status = PlanStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public PlanItem() {
    }

    public PlanItem(Profile profile, Subject subject, String title) {
        this.profile = profile;
        this.subject = subject;
        this.title = title;
        this.status = PlanStatus.OPEN;
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

    public List<SessionLog> getSessionLogs() {
        return sessionLogs;
    }

    public void setSessionLogs(List<SessionLog> sessionLogs) {
        this.sessionLogs = sessionLogs;
    }

    // ============================================
    // BUSINESS LOGIC HELPERS
    // ============================================

    /**
     * Mark this plan item as done
     */
    public void markAsDone() {
        this.status = PlanStatus.DONE;
    }

    /**
     * Check if this plan item is overdue
     */
    public boolean isOverdue() {
        return deadline != null &&
               status == PlanStatus.OPEN &&
               Instant.now().isAfter(deadline);
    }
}
