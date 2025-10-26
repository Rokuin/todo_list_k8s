package com.example.studybuddy.repository;

import com.example.studybuddy.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SubjectRepository - Data access layer for Subject entity
 *
 * This repository demonstrates:
 * - Method name query derivation
 * - @Query with JPQL (Java Persistence Query Language)
 * - Named parameters
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Find all subjects for a given profile
     *
     * METHOD NAME PARSING:
     * "findByProfileId" breaks down as:
     * - findBy = Query prefix
     * - Profile = Navigate to "profile" field in Subject entity
     * - Id = Access "id" field of Profile entity
     *
     * Generated query:
     * SELECT s FROM Subject s WHERE s.profile.id = ?1
     *
     * This uses the RELATIONSHIP between Subject and Profile!
     * Subject has @ManyToOne Profile, so we can navigate: subject.profile.id
     *
     * WHY List?
     * - One profile can have MANY subjects (one-to-many relationship)
     * - Returns empty list [] if no subjects found (not null)
     */
    List<Subject> findByProfileId(Long profileId);

    /**
     * Find a subject by ID AND ensure it belongs to the profile
     *
     * OWNERSHIP CHECK:
     * This is CRITICAL for security!
     * - Prevents user A from accessing user B's subjects
     * - Example: User tries to delete subject 999, but it belongs to another user
     *
     * "findByIdAndProfileId" translates to:
     * SELECT s FROM Subject s WHERE s.id = ?1 AND s.profile.id = ?2
     *
     * Returns Optional because:
     * - Subject might not exist (wrong ID)
     * - Subject might belong to different profile (ownership check fails)
     *
     * Usage in service:
     * Subject subject = subjectRepository.findByIdAndProfileId(subjectId, profileId)
     *     .orElseThrow(() -> new NotFoundException("Subject not found or access denied"));
     */
    Optional<Subject> findByIdAndProfileId(Long id, Long profileId);

    /**
     * ADVANCED: Custom JPQL query with JOIN FETCH
     *
     * @Query annotation:
     * - Allows writing custom queries
     * - Uses JPQL (Java Persistence Query Language), NOT SQL
     * - Works with entities and fields, not tables and columns
     *
     * JPQL vs SQL:
     * - JPQL: SELECT s FROM Subject s (entity name)
     * - SQL:  SELECT * FROM subjects s (table name)
     *
     * JOIN FETCH:
     * - Solves the N+1 query problem!
     * - Loads Subject AND Profile in ONE query (instead of N+1 queries)
     *
     * N+1 PROBLEM EXPLAINED:
     * Without JOIN FETCH:
     * 1. SELECT * FROM subjects (1 query)
     * 2. For each subject, SELECT * FROM profiles WHERE id = ? (N queries)
     * Total: 1 + N queries (bad performance!)
     *
     * With JOIN FETCH:
     * SELECT s, p FROM subjects s JOIN profiles p WHERE ... (1 query)
     * Total: 1 query (great performance!)
     *
     * NAMED PARAMETERS:
     * @Param("profileId") maps :profileId in query to method parameter
     * More readable than ?1, ?2 positional parameters
     *
     * WHEN TO USE:
     * - When you need to load related entities immediately
     * - When method name query is too complex
     * - When you want explicit control over the query
     */
    @Query("SELECT s FROM Subject s JOIN FETCH s.profile WHERE s.profile.id = :profileId")
    List<Subject> findByProfileIdWithProfile(@Param("profileId") Long profileId);

    /**
     * Count subjects for a profile
     *
     * "countByProfileId" translates to:
     * SELECT COUNT(s) FROM Subject s WHERE s.profile.id = ?1
     *
     * Returns long (primitive) - always returns a number (never null)
     *
     * Usage:
     * long subjectCount = subjectRepository.countByProfileId(profileId);
     * if (subjectCount > 10) {
     *     throw new LimitExceededException("Maximum 10 subjects allowed");
     * }
     */
    long countByProfileId(Long profileId);

    /**
     * Delete all subjects for a profile
     *
     * "deleteByProfileId" translates to:
     * DELETE FROM Subject s WHERE s.profile.id = ?1
     *
     * Returns long = number of deleted rows
     *
     * NOTE: This is usually not needed because of CASCADE!
     * When you delete a Profile, all its Subjects are automatically deleted
     * due to: cascade = CascadeType.ALL, orphanRemoval = true
     *
     * This method is here for completeness/manual cleanup if needed
     */
    long deleteByProfileId(Long profileId);
}
