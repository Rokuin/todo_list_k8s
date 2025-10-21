package com.example.studybuddy.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * ReminderRule Entity - Foundation for reminder functionality
 *
 * Future feature: Send notifications/reminders based on rules
 * Example rules:
 * - "Remind me daily at 9 AM"
 * - "Remind me 1 day before deadline"
 * - "Remind me if I haven't studied in 3 days"
 *
 * For now, this is just the database structure.
 * Implementation would require:
 * - Kubernetes CronJob to check rules periodically
 * - Notification service (email, SMS, push notification)
 */
@Entity
@Table(name = "reminder_rules")
public class ReminderRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MANY-TO-ONE: Many reminder rules belong to ONE profile
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reminderrule_profile"))
    private Profile profile;

    /**
     * Rule description
     * Example: "Daily reminder at 9 AM"
     */
    @Column(nullable = false, length = 200)
    private String description;

    /**
     * Cron expression for scheduling
     * Example: "0 9 * * *" = Every day at 9:00 AM
     *
     * Cron format: second minute hour day month weekday
     * Learn more: https://crontab.guru/
     *
     * For now, just store as string. Later, Kubernetes CronJob would parse this.
     */
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    /**
     * Is this rule currently active?
     * Allows users to temporarily disable reminders without deleting them
     */
    @Column(nullable = false)
    private Boolean active;

    /**
     * Timestamps
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
        // Default to active if not set
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public ReminderRule() {
    }

    public ReminderRule(Profile profile, String description) {
        this.profile = profile;
        this.description = description;
        this.active = true;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
