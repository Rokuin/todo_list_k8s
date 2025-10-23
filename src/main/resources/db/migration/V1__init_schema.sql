-- ============================================
-- Flyway Migration V1: Initialize Schema
-- ============================================
-- This creates all tables for the StudyBuddy application
--
-- Flyway will:
-- 1. Check if this migration already ran (looks in flyway_schema_history table)
-- 2. If not, execute this SQL
-- 3. Record that V1 was executed
-- 4. Never run this again (migrations are immutable!)
--
-- IMPORTANT: Once this runs in production, NEVER modify it!
-- For changes, create V2__description.sql

-- ============================================
-- TABLE: profiles
-- ============================================
-- Stores learner/student information
-- Maps to: Profile entity

CREATE TABLE profiles (
    -- Primary Key
    -- BIGSERIAL = PostgreSQL type for auto-incrementing big integer
    --   Equivalent to: BIGINT + AUTO_INCREMENT in MySQL
    --   Equivalent to: @GeneratedValue(strategy = IDENTITY) in JPA
    id BIGSERIAL PRIMARY KEY,

    -- Name field
    -- VARCHAR(n) = Variable-length string with max n characters
    -- NOT NULL = This field is required (like @Column(nullable = false))
    name VARCHAR(100) NOT NULL,

    -- Email field
    -- UNIQUE = No two rows can have the same email
    --   Database enforces this at INSERT/UPDATE time
    email VARCHAR(255) NOT NULL UNIQUE,

    -- Timestamps
    -- TIMESTAMP = Date + Time (without timezone)
    -- Our app config stores in UTC (hibernate.jdbc.time_zone: UTC)
    -- NOT NULL = Always required
    -- DEFAULT CURRENT_TIMESTAMP = Auto-set on INSERT (backup if app doesn't set it)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
-- Why? Users might search/login by email frequently
-- Index = Like book index, helps find data quickly without scanning entire table
-- B-tree index (default) = Good for equality (=) and range (<, >) queries
CREATE INDEX idx_profiles_email ON profiles(email);

-- ============================================
-- TABLE: subjects
-- ============================================
-- Stores study subjects (Math, English, etc.)
-- Maps to: Subject entity

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign Key to profiles table
    -- BIGINT = Must match the type of profiles.id
    -- NOT NULL = Every subject must belong to a profile
    profile_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,

    -- Optional description
    -- No NOT NULL constraint = This field is optional (nullable = true)
    description VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraint
    -- CONSTRAINT = Names the constraint (helpful for error messages and debugging)
    -- FOREIGN KEY (column) REFERENCES other_table(column)
    --   - Ensures profile_id always points to a valid profile
    --   - Cannot insert a subject with profile_id = 999 if profile 999 doesn't exist
    -- ON DELETE CASCADE = If profile is deleted, delete all its subjects too
    --   - Matches JPA: cascade = CascadeType.ALL, orphanRemoval = true
    -- ON UPDATE CASCADE = If profile.id changes, update all subject.profile_id
    --   (Rare since IDs rarely change, but good practice)
    CONSTRAINT fk_subject_profile
        FOREIGN KEY (profile_id)
        REFERENCES profiles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Index on foreign key for faster joins and lookups
-- Query: "Get all subjects for profile 123" becomes much faster
-- Foreign key columns should almost always be indexed!
CREATE INDEX idx_subjects_profile_id ON subjects(profile_id);

-- ============================================
-- TABLE: plan_items
-- ============================================
-- Stores study tasks/plans
-- Maps to: PlanItem entity

CREATE TABLE plan_items (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign keys
    profile_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,

    -- Optional deadline
    -- NULL allowed = Some tasks might not have deadlines
    deadline TIMESTAMP,

    -- Target study time in MINUTES
    -- INTEGER = 4-byte signed integer (-2 billion to +2 billion)
    -- Why minutes? Avoids floating-point precision issues
    --   90 minutes (exact) vs 1.5 hours (0.5 might become 0.499999...)
    target_minutes INTEGER,

    -- Status enum
    -- VARCHAR(20) = Store as string ("OPEN" or "DONE")
    -- NOT NULL = Status is always required
    -- CHECK constraint = Validates allowed values at database level
    --   - Extra safety beyond application validation
    --   - Protects against direct SQL manipulation
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'DONE')),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_planitem_profile
        FOREIGN KEY (profile_id)
        REFERENCES profiles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_planitem_subject
        FOREIGN KEY (subject_id)
        REFERENCES subjects(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Composite index on (profile_id, status)
-- Why? Common query: "Get all OPEN tasks for profile 123"
-- This index can answer that query very efficiently
-- Order matters: profile_id first (more selective), then status
CREATE INDEX idx_plan_items_profile_status ON plan_items(profile_id, status);

-- Index on subject for queries like "Get all plan items for subject X"
CREATE INDEX idx_plan_items_subject_id ON plan_items(subject_id);

-- Index on deadline for queries like "Get all tasks due soon"
-- PARTIAL INDEX (WHERE clause) = Only index non-null deadlines
--   Saves space: No need to index rows where deadline is NULL
CREATE INDEX idx_plan_items_deadline ON plan_items(deadline) WHERE deadline IS NOT NULL;

-- ============================================
-- TABLE: session_logs
-- ============================================
-- Stores actual study session records
-- Maps to: SessionLog entity

CREATE TABLE session_logs (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign keys
    profile_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,

    -- Optional link to plan item
    -- NULL allowed = Not all study sessions are linked to a plan
    plan_item_id BIGINT,

    -- When did the study session happen?
    studied_at TIMESTAMP NOT NULL,

    -- Duration in minutes
    duration_minutes INTEGER NOT NULL,

    -- Optional notes
    -- TEXT = Variable-length string with no practical limit
    --   Use TEXT instead of VARCHAR when length is unpredictable
    notes TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_sessionlog_profile
        FOREIGN KEY (profile_id)
        REFERENCES profiles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_sessionlog_subject
        FOREIGN KEY (subject_id)
        REFERENCES subjects(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_sessionlog_planitem
        FOREIGN KEY (plan_item_id)
        REFERENCES plan_items(id)
        ON DELETE SET NULL  -- If plan item deleted, keep session log but clear link
        ON UPDATE CASCADE
);

-- Index for time-based queries
-- "Get all sessions in date range" or "Get sessions for last 7 days"
-- studied_at DESC = Optimized for recent-first queries (most common pattern)
CREATE INDEX idx_session_logs_studied_at ON session_logs(studied_at DESC);

-- Composite index for profile-based queries with time
-- "Get all sessions for profile 123 in last month"
CREATE INDEX idx_session_logs_profile_studied ON session_logs(profile_id, studied_at DESC);

-- Index on subject for aggregations like "Total time spent on Math"
CREATE INDEX idx_session_logs_subject_id ON session_logs(subject_id);

-- Index on plan_item for queries like "Get all sessions for this plan"
CREATE INDEX idx_session_logs_plan_item_id ON session_logs(plan_item_id) WHERE plan_item_id IS NOT NULL;

-- ============================================
-- TABLE: reminder_rules
-- ============================================
-- Stores reminder rules (foundation for future feature)
-- Maps to: ReminderRule entity

CREATE TABLE reminder_rules (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign key
    profile_id BIGINT NOT NULL,

    description VARCHAR(200) NOT NULL,

    -- Cron expression for scheduling
    -- Example: "0 9 * * *" = Every day at 9:00 AM
    -- NULL allowed = Some reminders might not be cron-based
    cron_expression VARCHAR(100),

    -- Is this rule active?
    -- BOOLEAN = true/false
    -- DEFAULT true = New rules are active by default
    active BOOLEAN NOT NULL DEFAULT true,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint
    CONSTRAINT fk_reminderrule_profile
        FOREIGN KEY (profile_id)
        REFERENCES profiles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Index on profile for "Get all reminders for profile X"
CREATE INDEX idx_reminder_rules_profile_id ON reminder_rules(profile_id);

-- Partial index for active reminders only
-- Query: "Get all active reminders" (for cron job processing)
-- Only indexes rows where active = true (smaller, faster)
CREATE INDEX idx_reminder_rules_active ON reminder_rules(profile_id, active) WHERE active = true;

-- ============================================
-- DATABASE COMMENTS (PostgreSQL feature)
-- ============================================
-- Comments stored in database, visible in psql and GUI tools
-- Helpful for documentation and maintenance

COMMENT ON TABLE profiles IS 'Stores learner/student profiles';
COMMENT ON TABLE subjects IS 'Stores study subjects (Math, English, etc.)';
COMMENT ON TABLE plan_items IS 'Stores study tasks/plans with deadlines and status';
COMMENT ON TABLE session_logs IS 'Stores actual study session records';
COMMENT ON TABLE reminder_rules IS 'Stores reminder rules for notifications';

-- ============================================
-- VERIFICATION QUERIES (for learning)
-- ============================================
-- After this migration runs, you can verify with:
--
-- List all tables:
--   \dt
--
-- Describe a table:
--   \d profiles
--
-- View indexes:
--   \di
--
-- View foreign keys:
--   SELECT constraint_name, table_name, constraint_type
--   FROM information_schema.table_constraints
--   WHERE constraint_type = 'FOREIGN KEY';
