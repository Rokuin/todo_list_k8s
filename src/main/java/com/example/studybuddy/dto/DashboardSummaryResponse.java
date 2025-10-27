package com.example.studybuddy.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardSummaryResponse DTO
 *
 * AGGREGATED DATA DTO:
 * ====================
 * This DTO doesn't map to a single entity
 * Instead, it aggregates data from multiple sources:
 * - PlanItems (count by status)
 * - SessionLogs (total study time)
 * - Subjects (study time breakdown)
 *
 * Use case: Dashboard page showing overview
 *
 * Example response:
 * {
 *   "totalStudyMinutes": 1200,
 *   "totalStudyHours": 20.0,
 *   "totalSessions": 15,
 *   "openPlanItemsCount": 5,
 *   "donePlanItemsCount": 12,
 *   "subjectStats": [
 *     {"subjectId": 1, "subjectName": "Math", "totalMinutes": 600},
 *     {"subjectId": 2, "subjectName": "English", "totalMinutes": 600}
 *   ]
 * }
 */
public class DashboardSummaryResponse {

    /**
     * Total study time in minutes
     */
    private Long totalStudyMinutes;

    /**
     * Total study time in hours (calculated field)
     * Convenience for UI display
     */
    private Double totalStudyHours;

    /**
     * Total number of study sessions
     */
    private Long totalSessions;

    /**
     * Number of open (incomplete) plan items
     */
    private Long openPlanItemsCount;

    /**
     * Number of completed plan items
     */
    private Long donePlanItemsCount;

    /**
     * Subject-wise breakdown
     *
     * Why List<SubjectStatsDTO>?
     * - One entry per subject
     * - Shows how time is distributed across subjects
     * - Useful for charts/graphs
     */
    private List<SubjectStatsDTO> subjectStats = new ArrayList<>();

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public DashboardSummaryResponse() {
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getTotalStudyMinutes() {
        return totalStudyMinutes;
    }

    public void setTotalStudyMinutes(Long totalStudyMinutes) {
        this.totalStudyMinutes = totalStudyMinutes;
        // Auto-calculate hours when minutes are set
        if (totalStudyMinutes != null) {
            this.totalStudyHours = totalStudyMinutes / 60.0;
        }
    }

    public Double getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(Double totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public Long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public Long getOpenPlanItemsCount() {
        return openPlanItemsCount;
    }

    public void setOpenPlanItemsCount(Long openPlanItemsCount) {
        this.openPlanItemsCount = openPlanItemsCount;
    }

    public Long getDonePlanItemsCount() {
        return donePlanItemsCount;
    }

    public void setDonePlanItemsCount(Long donePlanItemsCount) {
        this.donePlanItemsCount = donePlanItemsCount;
    }

    public List<SubjectStatsDTO> getSubjectStats() {
        return subjectStats;
    }

    public void setSubjectStats(List<SubjectStatsDTO> subjectStats) {
        this.subjectStats = subjectStats;
    }
}
