package com.example.studybuddy.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Profile Entity - Represents a learner/student
 *
 * JPA Annotations Explained:
 * @Entity - Marks this class as a JPA entity (maps to database table)
 * @Table - Specifies the table name (optional, defaults to class name)
 * @Id - Marks the primary key field
 * @GeneratedValue - Auto-generate the ID value (database does it for you!)
 * @Column - Maps field to a table column with constraints
 * @OneToMany - Defines one-to-many relationship
 */
@Entity
@Table(name = "profiles")
public class Profile {

    /**
     * Primary Key
     * @Id - This field is the primary key
     * @GeneratedValue(strategy = IDENTITY) - Database auto-increments this value
     *   - IDENTITY: Uses database's auto-increment (PostgreSQL SERIAL)
     *   - SEQUENCE: Uses database sequence (more flexible, can pre-allocate IDs)
     *   - AUTO: JPA chooses best strategy
     *
     * Answer to Q2: B is correct! Database generates IDs automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name field
     * @Column(nullable = false) - This field is required (NOT NULL in database)
     * Answer to Q3: A is correct! Makes the field required.
     *
     * length = 100 - Maximum length (VARCHAR(100) in database)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Email field
     * unique = true - No two profiles can have same email (unique constraint)
     * Why unique? Email is often used as username/identifier
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Timestamps - When was this profile created/updated?
     *
     * updatable = false - Once set, cannot be changed (creation time is immutable)
     * Instant = Java 8+ date/time type (better than old Date class)
     * Stored as TIMESTAMP in database (UTC timezone from our config!)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * ONE-TO-MANY RELATIONSHIPS
     *
     * One Profile HAS MANY Subjects
     * @OneToMany - Defines the relationship
     * mappedBy = "profile" - The "profile" field in Subject entity owns this relationship
     * cascade = CascadeType.ALL - Operations on Profile cascade to Subjects
     *   - If you delete a Profile, all its Subjects are also deleted
     * orphanRemoval = true - If a Subject is removed from this list, delete it from database
     *
     * Why List? Because one profile can have multiple subjects
     * Why ArrayList? Good default implementation (indexed access)
     */
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanItem> planItems = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionLog> sessionLogs = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReminderRule> reminderRules = new ArrayList<>();

    /**
     * JPA LIFECYCLE CALLBACKS
     * These methods are automatically called by JPA at certain times
     *
     * @PrePersist - Called before entity is saved for the first time (INSERT)
     * @PreUpdate - Called before entity is updated (UPDATE)
     *
     * Why? To automatically set timestamps without manual code!
     */
    @PrePersist
    protected void onCreate() {
        // Set both timestamps to current time when creating
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        // Update the updated_at timestamp when modifying
        updatedAt = Instant.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * No-args constructor - REQUIRED by JPA
     * JPA needs this to create instances via reflection
     */
    public Profile() {
    }

    /**
     * Constructor with required fields
     * Convenience constructor for creating new profiles
     */
    public Profile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================
    // Standard Java bean pattern
    // JPA uses these to read/write field values

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
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

    public List<ReminderRule> getReminderRules() {
        return reminderRules;
    }

    public void setReminderRules(List<ReminderRule> reminderRules) {
        this.reminderRules = reminderRules;
    }

    // ============================================
    // HELPER METHODS
    // ============================================
    // These methods help maintain bidirectional relationships

    /**
     * Add a subject to this profile
     * Also sets the profile reference in the subject (bidirectional relationship)
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
        subject.setProfile(this);
    }

    /**
     * Remove a subject from this profile
     * Also clears the profile reference in the subject
     */
    public void removeSubject(Subject subject) {
        subjects.remove(subject);
        subject.setProfile(null);
    }
}
