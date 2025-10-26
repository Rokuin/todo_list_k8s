package com.example.studybuddy.repository;

import com.example.studybuddy.entity.PlanItem;
import com.example.studybuddy.entity.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * PlanItemRepository - Data access layer for PlanItem entity
 *
 * This repository demonstrates:
 * - Querying with enums (PlanStatus)
 * - Date/time comparisons
 * - Complex multi-condition queries
 * - Aggregation queries (SUM)
 */
@Repository
public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {

    /**
     * Find all plan items for a profile
     */
    List<PlanItem> findByProfileId(Long profileId);

    /**
     * Find plan item by ID with ownership check
     * SECURITY: Ensures user can only access their own plan items
     */
    Optional<PlanItem> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Find all plan items for a profile filtered by status
     *
     * METHOD NAME WITH ENUM:
     * "findByProfileIdAndStatus" translates to:
     * SELECT p FROM PlanItem p WHERE p.profile.id = ?1 AND p.status = ?2
     *
     * Spring Data JPA automatically handles enum comparison!
     * You pass PlanStatus.OPEN, it compares correctly in database
     *
     * Usage:
     * List<PlanItem> openTasks = planItemRepository.findByProfileIdAndStatus(
     *     profileId,
     *     PlanStatus.OPEN
     * );
     */
    List<PlanItem> findByProfileIdAndStatus(Long profileId, PlanStatus status);

    /**
     * Find all plan items for a subject
     */
    List<PlanItem> findBySubjectId(Long subjectId);

    /**
     * Find plan items by profile and subject
     *
     * Multi-condition query:
     * WHERE p.profile.id = ?1 AND p.subject.id = ?2
     */
    List<PlanItem> findByProfileIdAndSubjectId(Long profileId, Long subjectId);

    /**
     * Find overdue plan items for a profile
     *
     * COMPLEX CONDITIONS:
     * "findByProfileIdAndStatusAndDeadlineBefore" translates to:
     * SELECT p FROM PlanItem p
     * WHERE p.profile.id = ?1
     *   AND p.status = ?2
     *   AND p.deadline < ?3
     *
     * KEYWORD MEANINGS:
     * - Before = Less than (<)
     * - After = Greater than (>)
     * - Between = Between two values
     * - IsNull = IS NULL
     * - IsNotNull = IS NOT NULL
     *
     * Other useful keywords:
     * - LessThan, GreaterThan, LessThanEqual, GreaterThanEqual
     * - Like, NotLike, StartingWith, EndingWith, Containing
     * - In, NotIn
     * - True, False (for booleans)
     *
     * Usage (find overdue OPEN tasks):
     * List<PlanItem> overdue = planItemRepository.findByProfileIdAndStatusAndDeadlineBefore(
     *     profileId,
     *     PlanStatus.OPEN,
     *     Instant.now()  // Current time
     * );
     */
    List<PlanItem> findByProfileIdAndStatusAndDeadlineBefore(
        Long profileId,
        PlanStatus status,
        Instant deadline
    );

    /**
     * Find upcoming plan items (deadline in the near future)
     *
     * "DeadlineBetween" translates to:
     * WHERE p.deadline BETWEEN ?1 AND ?2
     *
     * Usage (find tasks due in next 7 days):
     * Instant now = Instant.now();
     * Instant weekFromNow = now.plus(7, ChronoUnit.DAYS);
     * List<PlanItem> upcoming = planItemRepository.findByProfileIdAndDeadlineBetween(
     *     profileId, now, weekFromNow
     * );
     */
    List<PlanItem> findByProfileIdAndDeadlineBetween(
        Long profileId,
        Instant start,
        Instant end
    );

    /**
     * ADVANCED: Calculate total target minutes for a profile
     *
     * AGGREGATION QUERY:
     * - SUM() = Aggregate function
     * - COALESCE(sum, 0) = Return 0 if sum is NULL (no plan items)
     *
     * Why COALESCE?
     * - If profile has no plan items, SUM returns NULL
     * - NULL can cause NullPointerException
     * - COALESCE converts NULL to 0
     *
     * This query returns a single number (total minutes), not a list of entities
     *
     * Usage:
     * Long totalMinutes = planItemRepository.sumTargetMinutesByProfileId(profileId);
     * double totalHours = totalMinutes / 60.0;
     */
    @Query("SELECT COALESCE(SUM(p.targetMinutes), 0) FROM PlanItem p WHERE p.profile.id = :profileId")
    Long sumTargetMinutesByProfileId(@Param("profileId") Long profileId);

    /**
     * Calculate total target minutes for OPEN plan items only
     *
     * Useful for "remaining work" calculations
     */
    @Query("SELECT COALESCE(SUM(p.targetMinutes), 0) FROM PlanItem p " +
           "WHERE p.profile.id = :profileId AND p.status = :status")
    Long sumTargetMinutesByProfileIdAndStatus(
        @Param("profileId") Long profileId,
        @Param("status") PlanStatus status
    );

    /**
     * Count plan items by status
     *
     * Simple way to get statistics:
     * long openCount = planItemRepository.countByProfileIdAndStatus(profileId, PlanStatus.OPEN);
     * long doneCount = planItemRepository.countByProfileIdAndStatus(profileId, PlanStatus.DONE);
     */
    long countByProfileIdAndStatus(Long profileId, PlanStatus status);

    /**
     * ADVANCED: Find plan items with JOIN FETCH (optimized loading)
     *
     * Loads PlanItem + Subject + Profile in ONE query
     * Prevents N+1 query problem
     *
     * Use this when you know you'll need the related entities
     * Example: Displaying list of plan items with subject names
     */
    @Query("SELECT p FROM PlanItem p " +
           "JOIN FETCH p.subject s " +
           "JOIN FETCH p.profile " +
           "WHERE p.profile.id = :profileId")
    List<PlanItem> findByProfileIdWithSubjectAndProfile(@Param("profileId") Long profileId);

    /**
     * Find plan items ordered by deadline (closest first)
     *
     * ORDER BY in method name:
     * - OrderBy + PropertyName + Asc/Desc
     *
     * "findByProfileIdOrderByDeadlineAsc" translates to:
     * SELECT p FROM PlanItem p WHERE p.profile.id = ?1 ORDER BY p.deadline ASC
     *
     * ASC = Ascending (1, 2, 3... or A, B, C... or oldest to newest)
     * DESC = Descending (reverse)
     *
     * Problem: NULL deadlines appear first in ASC order!
     * Solution: Use custom @Query with NULLS LAST
     */
    @Query("SELECT p FROM PlanItem p WHERE p.profile.id = :profileId " +
           "ORDER BY p.deadline ASC NULLS LAST")
    List<PlanItem> findByProfileIdOrderByDeadlineNullsLast(@Param("profileId") Long profileId);
}
