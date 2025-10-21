package com.example.studybuddy.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Subject Entity - Represents a study subject/course
 *
 * Example: Math, English, Programming, etc.
 * Each subject belongs to ONE profile
 */
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MANY-TO-ONE RELATIONSHIP
     *
     * Many Subjects belong to ONE Profile
     * @ManyToOne - Defines the relationship
     * @JoinColumn - Specifies the foreign key column
     *   - name = "profile_id" - Column name in subjects table
     *   - nullable = false - Every subject MUST belong to a profile
     *   - foreignKey = @ForeignKey - Names the foreign key constraint in database
     *
     * KEY CONCEPT: The "Many" side owns the relationship
     * Subject has profile_id column, not the other way around
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subject_profile"))
    private Profile profile;

    /**
     * Subject name
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Optional description
     * nullable = true (default) - This field is optional
     */
    @Column(length = 500)
    private String description;

    /**
     * Timestamps
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * ONE-TO-MANY: One Subject has many PlanItems
     */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanItem> planItems = new ArrayList<>();

    /**
     * ONE-TO-MANY: One Subject has many SessionLogs
     */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionLog> sessionLogs = new ArrayList<>();

    /**
     * Lifecycle callbacks - auto-set timestamps
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Subject() {
    }

    public Subject(Profile profile, String name) {
        this.profile = profile;
        this.name = name;
    }

    public Subject(Profile profile, String name, String description) {
        this.profile = profile;
        this.name = name;
        this.description = description;
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

    public List<PlanItem> getPlanItems() {
        return planItems;
    }

    public void setPlanItems(List<PlanItem> planItems) {
        this.planItems = planItems;
    }

    public List<SessionLog> getSessionLogs() {
        return sessionLogs;
    }

    public void setSessionLogs(List<SessionLog> sessionLogs) {
        this.sessionLogs = sessionLogs;
    }
}
