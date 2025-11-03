package com.example.studybuddy.controller;

import com.example.studybuddy.dto.DashboardSummaryResponse;
import com.example.studybuddy.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DashboardController - REST API endpoints for Dashboard/Summary
 *
 * DASHBOARD PATTERN:
 * ==================
 * Single endpoint that aggregates data from multiple sources
 * - Reduces number of HTTP requests (1 instead of 5+)
 * - Provides complete overview for dashboard page
 * - Optimized for common use case
 *
 * Alternative: Client makes multiple requests
 * - GET /session-logs/total-minutes?profileId=1
 * - GET /plan-items?profileId=1&status=OPEN
 * - GET /plan-items?profileId=1&status=DONE
 * - GET /session-logs/subject-stats?profileId=1
 * - etc.
 * Problem: 5+ HTTP requests, slower, more complex client code
 *
 * Solution: Single dashboard endpoint
 * - GET /dashboard/summary?profileId=1
 * - Returns everything in one response
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * GET DASHBOARD SUMMARY
     * =====================
     * Endpoint: GET /api/v1/dashboard/summary?profileId=1
     *
     * Returns comprehensive dashboard data:
     * {
     *   "totalStudyMinutes": 1200,
     *   "totalStudyHours": 20.0,
     *   "totalSessions": 15,
     *   "openPlanItemsCount": 5,
     *   "donePlanItemsCount": 12,
     *   "subjectStats": [
     *     {
     *       "subjectId": 1,
     *       "subjectName": "Math",
     *       "totalMinutes": 600,
     *       "totalHours": 10.0,
     *       "sessionCount": 8
     *     },
     *     {
     *       "subjectId": 2,
     *       "subjectName": "English",
     *       "totalMinutes": 600,
     *       "totalHours": 10.0,
     *       "sessionCount": 7
     *     }
     *   ]
     * }
     *
     * AGGREGATED DTO:
     * ===============
     * DashboardSummaryResponse combines data from:
     * - SessionLogs (total time, session count)
     * - PlanItems (open/done counts)
     * - Subjects (breakdown by subject)
     *
     * PERFORMANCE CONSIDERATIONS:
     * ===========================
     * Multiple database queries:
     * 1. SUM(duration_minutes) FROM session_logs
     * 2. COUNT(*) FROM session_logs
     * 3. COUNT(*) FROM plan_items WHERE status = 'OPEN'
     * 4. COUNT(*) FROM plan_items WHERE status = 'DONE'
     * 5. GROUP BY subject_id FROM session_logs
     *
     * Each query is efficient (uses indexes), but still 5 queries
     *
     * Optimization strategies:
     * 1. Caching: @Cacheable on service method
     *    - Cache for 5 minutes
     *    - Invalidate on data change
     * 2. Single complex query: JOIN everything
     *    - More complex SQL
     *    - Harder to maintain
     * 3. Pre-computed materialized view
     *    - Update on data change
     *    - Instant read
     *
     * For this project: Simple approach is fine
     * For high traffic: Add caching
     *
     * USE CASE:
     * =========
     * Dashboard page that shows:
     * - "Total study time: 20 hours"
     * - "Sessions completed: 15"
     * - "Open tasks: 5"
     * - "Completed tasks: 12"
     * - Pie chart: Study time by subject
     * - Progress bar: Open vs done tasks
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
        @RequestParam Long profileId
    ) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(profileId);
        return ResponseEntity.ok(summary);
    }

    /**
     * FUTURE DASHBOARD ENDPOINTS:
     * ===========================
     *
     * GET /dashboard/streak?profileId=1
     * - Current study streak (consecutive days studied)
     * - Longest streak
     * Response: {"current": 7, "longest": 30}
     *
     * GET /dashboard/weekly?profileId=1
     * - Study time by day of week
     * - For bar chart visualization
     * Response: [
     *   {"day": "Monday", "minutes": 120},
     *   {"day": "Tuesday", "minutes": 90},
     *   ...
     * ]
     *
     * GET /dashboard/monthly?profileId=1&year=2024
     * - Study time by month
     * Response: [
     *   {"month": "January", "minutes": 2400},
     *   {"month": "February", "minutes": 2100},
     *   ...
     * ]
     *
     * GET /dashboard/progress?profileId=1
     * - Progress towards goals
     * - Completion percentage
     * Response: {
     *   "targetMinutes": 3000,
     *   "completedMinutes": 1500,
     *   "percentage": 50.0
     * }
     *
     * GET /dashboard/leaderboard?profileId=1
     * - Compare with friends (social feature)
     * - Requires friend relationships
     *
     * DESIGN PRINCIPLE:
     * =================
     * Dashboard endpoints should be:
     * 1. Read-only (GET only)
     * 2. Aggregated (combine multiple data sources)
     * 3. Optimized (consider caching)
     * 4. Focused (specific use case, not general-purpose)
     */
}
