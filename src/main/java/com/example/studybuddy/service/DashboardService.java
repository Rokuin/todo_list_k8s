package com.example.studybuddy.service;

import com.example.studybuddy.dto.DashboardSummaryResponse;
import com.example.studybuddy.dto.SubjectStatsDTO;
import com.example.studybuddy.entity.PlanStatus;
import com.example.studybuddy.exception.ResourceNotFoundException;
import com.example.studybuddy.repository.PlanItemRepository;
import com.example.studybuddy.repository.ProfileRepository;
import com.example.studybuddy.repository.SessionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardService - Business logic for Dashboard/Summary
 *
 * DEMONSTRATES:
 * - Aggregating data from multiple sources
 * - Converting Object[] to DTO
 * - Building complex response DTOs
 *
 * DASHBOARD PATTERN:
 * ==================
 * Dashboard = Single endpoint that aggregates multiple statistics
 * - Total study time
 * - Session count
 * - Plan item status breakdown
 * - Subject-wise breakdown
 *
 * WHY SEPARATE SERVICE?
 * - Dashboard logic doesn't fit cleanly into any single entity service
 * - Coordinates multiple repositories
 * - Returns custom aggregated DTO
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final SessionLogRepository sessionLogRepository;
    private final PlanItemRepository planItemRepository;
    private final ProfileRepository profileRepository;

    public DashboardService(SessionLogRepository sessionLogRepository,
                           PlanItemRepository planItemRepository,
                           ProfileRepository profileRepository) {
        this.sessionLogRepository = sessionLogRepository;
        this.planItemRepository = planItemRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Get dashboard summary for a profile
     *
     * AGGREGATION FROM MULTIPLE SOURCES:
     * ===================================
     * 1. SessionLog → total study time, session count
     * 2. PlanItem → open/done counts
     * 3. SessionLog + Subject → subject-wise breakdown
     *
     * PERFORMANCE:
     * - Multiple queries, but each is efficient (uses indexes)
     * - Alternative: Single complex JOIN query (harder to maintain)
     * - For high traffic: Add caching (@Cacheable)
     */
    public DashboardSummaryResponse getDashboardSummary(Long profileId) {
        // Validate profile exists
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        // Create response DTO
        DashboardSummaryResponse response = new DashboardSummaryResponse();

        // QUERY 1: Total study time and session count
        Long totalMinutes = sessionLogRepository.sumDurationMinutesByProfileId(profileId);
        Long totalSessions = sessionLogRepository.countByProfileId(profileId);

        response.setTotalStudyMinutes(totalMinutes);
        // setTotalStudyMinutes auto-calculates totalStudyHours (see DTO)
        response.setTotalSessions(totalSessions);

        // QUERY 2: Plan item counts by status
        Long openCount = planItemRepository.countByProfileIdAndStatus(
            profileId,
            PlanStatus.OPEN
        );
        Long doneCount = planItemRepository.countByProfileIdAndStatus(
            profileId,
            PlanStatus.DONE
        );

        response.setOpenPlanItemsCount(openCount);
        response.setDonePlanItemsCount(doneCount);

        // QUERY 3: Subject-wise statistics
        List<SubjectStatsDTO> subjectStats = getSubjectStatistics(profileId);
        response.setSubjectStats(subjectStats);

        return response;
    }

    /**
     * Get subject-wise study statistics
     *
     * CONVERTING Object[] TO DTO:
     * ============================
     * Repository returns: List<Object[]>
     * Each Object[] = [subjectId, subjectName, totalMinutes]
     *
     * We convert to: List<SubjectStatsDTO>
     *
     * WHY Object[]?
     * - JPQL query returns multiple columns, not an entity
     * - Object[] is generic container for query results
     *
     * BETTER APPROACH (for complex apps):
     * Use constructor expression in JPQL:
     * @Query("SELECT new com.example.SubjectStatsDTO(s.id, s.name, SUM(...)) ...")
     * Returns List<SubjectStatsDTO> directly (no Object[] conversion)
     *
     * For this project, manual conversion is educational
     */
    private List<SubjectStatsDTO> getSubjectStatistics(Long profileId) {
        // Get raw query results
        List<Object[]> rawStats = sessionLogRepository
            .getSubjectStatsByProfileId(profileId);

        // Convert Object[] to SubjectStatsDTO
        List<SubjectStatsDTO> stats = new ArrayList<>();

        for (Object[] row : rawStats) {
            // Extract values from Object[]
            // Database columns: subject_id, subject_name, total_minutes
            Long subjectId = (Long) row[0];           // Cast to Long
            String subjectName = (String) row[1];     // Cast to String
            Long totalMinutes = (Long) row[2];        // Cast to Long

            // Create DTO
            SubjectStatsDTO dto = new SubjectStatsDTO(
                subjectId,
                subjectName,
                totalMinutes
            );

            stats.add(dto);
        }

        return stats;

        // STREAM VERSION (more concise):
        // return rawStats.stream()
        //     .map(row -> new SubjectStatsDTO(
        //         (Long) row[0],
        //         (String) row[1],
        //         (Long) row[2]
        //     ))
        //     .collect(Collectors.toList());
    }

    /**
     * FUTURE ENHANCEMENTS:
     * ====================
     * Additional dashboard metrics could include:
     * - Study streak (consecutive days studied)
     * - Most studied subject
     * - Average session duration
     * - Study time by day of week (chart data)
     * - Progress towards target hours
     * - Overdue task count
     */
}
