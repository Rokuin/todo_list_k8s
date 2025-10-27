package com.example.studybuddy.dto;

/**
 * SubjectStatsDTO
 *
 * NESTED DTO:
 * ===========
 * Used within DashboardSummaryResponse
 * Represents study statistics for ONE subject
 *
 * Example:
 * {
 *   "subjectId": 1,
 *   "subjectName": "Math",
 *   "totalMinutes": 600,
 *   "totalHours": 10.0,
 *   "sessionCount": 8
 * }
 */
public class SubjectStatsDTO {

    private Long subjectId;

    private String subjectName;

    /**
     * Total study time for this subject (in minutes)
     */
    private Long totalMinutes;

    /**
     * Total study time for this subject (in hours)
     * Calculated field for convenience
     */
    private Double totalHours;

    /**
     * Number of study sessions for this subject
     */
    private Long sessionCount;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public SubjectStatsDTO() {
    }

    /**
     * Constructor used in JPQL query
     *
     * In SessionLogRepository, we could write:
     * @Query("SELECT new com.example.studybuddy.dto.SubjectStatsDTO(s.subject.id, s.subject.name, SUM(s.durationMinutes), COUNT(s)) ...")
     *
     * JPA would call this constructor with query results!
     */
    public SubjectStatsDTO(Long subjectId, String subjectName, Long totalMinutes, Long sessionCount) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.totalMinutes = totalMinutes;
        this.totalHours = totalMinutes != null ? totalMinutes / 60.0 : 0.0;
        this.sessionCount = sessionCount;
    }

    // Convenience constructor without sessionCount
    public SubjectStatsDTO(Long subjectId, String subjectName, Long totalMinutes) {
        this(subjectId, subjectName, totalMinutes, 0L);
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Long totalMinutes) {
        this.totalMinutes = totalMinutes;
        // Auto-calculate hours
        this.totalHours = totalMinutes != null ? totalMinutes / 60.0 : 0.0;
    }

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public Long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Long sessionCount) {
        this.sessionCount = sessionCount;
    }
}
