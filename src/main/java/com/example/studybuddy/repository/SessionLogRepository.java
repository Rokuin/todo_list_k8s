package com.example.studybuddy.repository;

import com.example.studybuddy.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * SessionLogRepository - Data access layer for SessionLog entity
 *
 * This repository demonstrates:
 * - Date range queries (for statistics)
 * - Aggregation queries (SUM, COUNT)
 * - GROUP BY queries (for subject-wise statistics)
 * - Complex JOINs for optimized loading
 */
@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

    /**
     * Find all session logs for a profile
     */
    List<SessionLog> findByProfileId(Long profileId);

    /**
     * Find session log by ID with ownership check
     */
    Optional<SessionLog> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Find all session logs for a profile within a date range
     *
     * DATE RANGE QUERY:
     * "findByProfileIdAndStudiedAtBetween" translates to:
     * SELECT s FROM SessionLog s
     * WHERE s.profile.id = ?1
     *   AND s.studied_at BETWEEN ?2 AND ?3
     *
     * CRITICAL for statistics:
     * - Last 7 days
     * - This month
     * - This year
     *
     * Usage (get last 7 days):
     * Instant now = Instant.now();
     * Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
     * List<SessionLog> recentSessions = sessionLogRepository
     *     .findByProfileIdAndStudiedAtBetween(profileId, weekAgo, now);
     */
    List<SessionLog> findByProfileIdAndStudiedAtBetween(
        Long profileId,
        Instant startDate,
        Instant endDate
    );

    /**
     * Find all session logs for a subject
     */
    List<SessionLog> findBySubjectId(Long subjectId);

    /**
     * Find all session logs for a specific plan item
     */
    List<SessionLog> findByPlanItemId(Long planItemId);

    /**
     * Find sessions for a profile and subject within date range
     *
     * Multi-condition query for subject-specific statistics
     */
    List<SessionLog> findByProfileIdAndSubjectIdAndStudiedAtBetween(
        Long profileId,
        Long subjectId,
        Instant startDate,
        Instant endDate
    );

    /**
     * AGGREGATION: Calculate total study time (in minutes) for a profile
     *
     * Returns total time spent studying
     *
     * Usage:
     * Long totalMinutes = sessionLogRepository.sumDurationMinutesByProfileId(profileId);
     * double totalHours = totalMinutes / 60.0;
     * System.out.println("Total study time: " + totalHours + " hours");
     */
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM SessionLog s " +
           "WHERE s.profile.id = :profileId")
    Long sumDurationMinutesByProfileId(@Param("profileId") Long profileId);

    /**
     * Calculate total study time within a date range
     *
     * For weekly/monthly/yearly statistics
     *
     * Usage (total time this week):
     * Instant weekStart = getStartOfWeek();
     * Instant now = Instant.now();
     * Long weeklyMinutes = sessionLogRepository.sumDurationMinutesByProfileIdAndDateRange(
     *     profileId, weekStart, now
     * );
     */
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM SessionLog s " +
           "WHERE s.profile.id = :profileId " +
           "AND s.studiedAt BETWEEN :startDate AND :endDate")
    Long sumDurationMinutesByProfileIdAndDateRange(
        @Param("profileId") Long profileId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Calculate total study time for a specific subject
     *
     * Usage:
     * Long mathMinutes = sessionLogRepository.sumDurationMinutesBySubjectId(mathSubjectId);
     */
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM SessionLog s " +
           "WHERE s.subject.id = :subjectId")
    Long sumDurationMinutesBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * ADVANCED: Get subject-wise study time statistics
     *
     * GROUP BY QUERY:
     * - Returns list of Object arrays: [subjectId, subjectName, totalMinutes]
     * - Each row represents one subject's statistics
     *
     * Object[] structure:
     * - result[0] = subjectId (Long)
     * - result[1] = subjectName (String)
     * - result[2] = totalMinutes (Long)
     *
     * WHY Object[]?
     * - We're selecting multiple columns, not an entity
     * - Alternative: Create a DTO class and use constructor expression
     *
     * Usage:
     * List<Object[]> stats = sessionLogRepository.getSubjectStatsByProfileId(profileId);
     * for (Object[] row : stats) {
     *     Long subjectId = (Long) row[0];
     *     String subjectName = (String) row[1];
     *     Long totalMinutes = (Long) row[2];
     *     System.out.println(subjectName + ": " + (totalMinutes / 60.0) + " hours");
     * }
     *
     * Better approach (we'll use in DTOs):
     * @Query("SELECT new com.example.studybuddy.dto.SubjectStatsDTO(s.subject.id, s.subject.name, SUM(s.durationMinutes)) ...")
     */
    @Query("SELECT s.subject.id, s.subject.name, COALESCE(SUM(s.durationMinutes), 0) " +
           "FROM SessionLog s " +
           "WHERE s.profile.id = :profileId " +
           "GROUP BY s.subject.id, s.subject.name " +
           "ORDER BY SUM(s.durationMinutes) DESC")
    List<Object[]> getSubjectStatsByProfileId(@Param("profileId") Long profileId);

    /**
     * Count total study sessions for a profile
     *
     * Simple count, useful for "You've completed 42 study sessions!" messages
     */
    long countByProfileId(Long profileId);

    /**
     * Count study sessions within date range
     */
    long countByProfileIdAndStudiedAtBetween(
        Long profileId,
        Instant startDate,
        Instant endDate
    );

    /**
     * Find recent session logs, ordered by date (newest first)
     *
     * ORDER BY DESC = Descending (newest to oldest)
     *
     * Usage (get last 10 sessions):
     * List<SessionLog> recent = sessionLogRepository
     *     .findByProfileIdOrderByStudiedAtDesc(profileId)
     *     .stream()
     *     .limit(10)
     *     .collect(Collectors.toList());
     *
     * Or use pagination (better for large datasets - we'll learn later)
     */
    List<SessionLog> findByProfileIdOrderByStudiedAtDesc(Long profileId);

    /**
     * OPTIMIZED: Find sessions with JOIN FETCH
     *
     * Loads SessionLog + Subject + Profile in ONE query
     * Use when displaying session list with subject/profile names
     *
     * Without JOIN FETCH:
     * 1. SELECT * FROM session_logs (1 query)
     * 2. For each session: SELECT * FROM subjects (N queries)
     * 3. For each session: SELECT * FROM profiles (N queries)
     * Total: 1 + N + N = 1 + 2N queries (BAD!)
     *
     * With JOIN FETCH:
     * SELECT s, sub, p FROM session_logs s
     * JOIN subjects sub ON ...
     * JOIN profiles p ON ...
     * Total: 1 query (GOOD!)
     */
    @Query("SELECT s FROM SessionLog s " +
           "JOIN FETCH s.subject " +
           "JOIN FETCH s.profile " +
           "WHERE s.profile.id = :profileId " +
           "ORDER BY s.studiedAt DESC")
    List<SessionLog> findByProfileIdWithSubjectAndProfile(@Param("profileId") Long profileId);
}
