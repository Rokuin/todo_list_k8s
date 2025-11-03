package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CreateSessionLogRequest;
import com.example.studybuddy.dto.SessionLogResponse;
import com.example.studybuddy.service.SessionLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * SessionLogController - REST API endpoints for Session Log management
 *
 * DEMONSTRATES:
 * - Date range queries with optional parameters
 * - Statistics endpoints
 */
@RestController
@RequestMapping("/api/v1/session-logs")
public class SessionLogController {

    private final SessionLogService sessionLogService;

    public SessionLogController(SessionLogService sessionLogService) {
        this.sessionLogService = sessionLogService;
    }

    /**
     * CREATE SESSION LOG
     * ==================
     * Endpoint: POST /api/v1/session-logs
     *
     * Request body:
     * {
     *   "profileId": 1,
     *   "subjectId": 2,
     *   "planItemId": 3,          // Optional
     *   "studiedAt": "2024-01-15T10:00:00Z",  // Optional (defaults to now)
     *   "durationMinutes": 90,
     *   "notes": "Completed Chapter 5"  // Optional
     * }
     *
     * USE CASES:
     * 1. Real-time logging: POST immediately after study session
     * 2. Manual entry: POST with past studiedAt (forgot to log yesterday)
     */
    @PostMapping
    public ResponseEntity<SessionLogResponse> createSessionLog(
        @Valid @RequestBody CreateSessionLogRequest request
    ) {
        SessionLogResponse response = sessionLogService.createSessionLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET SESSION LOGS
     * ================
     * Endpoint: GET /api/v1/session-logs?profileId=1
     *
     * Optional date range filtering:
     * GET /api/v1/session-logs?profileId=1&startDate=...&endDate=...
     *
     * INSTANT AS REQUEST PARAMETER:
     * =============================
     * Spring automatically parses ISO-8601 format:
     * - ?startDate=2024-01-01T00:00:00Z
     * - Jackson converts String → Instant
     *
     * If format is invalid → 400 BAD REQUEST
     *
     * DATE RANGE FILTERING:
     * - If startDate/endDate provided → filter
     * - If not provided → return all
     */
    @GetMapping
    public ResponseEntity<List<SessionLogResponse>> getSessionLogs(
        @RequestParam Long profileId,
        @RequestParam(required = false) Instant startDate,
        @RequestParam(required = false) Instant endDate
    ) {
        List<SessionLogResponse> sessionLogs;

        if (startDate != null && endDate != null) {
            // Date range filter
            sessionLogs = sessionLogService.getSessionLogsByDateRange(
                profileId,
                startDate,
                endDate
            );
        } else {
            // All sessions
            sessionLogs = sessionLogService.getSessionLogsByProfileId(profileId);
        }

        return ResponseEntity.ok(sessionLogs);
    }

    /**
     * GET SESSION LOGS FOR LAST N DAYS
     * =================================
     * Endpoint: GET /api/v1/session-logs/recent?profileId=1&days=7
     *
     * Convenience endpoint for common use case
     * - Last 7 days (default)
     * - Last 30 days
     * - Last year
     *
     * SIMPLER THAN:
     * GET /api/v1/session-logs?profileId=1&startDate=...&endDate=...
     * (Client doesn't need to calculate dates)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<SessionLogResponse>> getRecentSessionLogs(
        @RequestParam Long profileId,
        @RequestParam(defaultValue = "7") int days
    ) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);

        List<SessionLogResponse> sessionLogs = sessionLogService
            .getSessionLogsByDateRange(profileId, startDate, endDate);

        return ResponseEntity.ok(sessionLogs);
    }

    /**
     * GET SESSION LOG BY ID
     * =====================
     * Endpoint: GET /api/v1/session-logs/{id}?profileId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionLogResponse> getSessionLogById(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        SessionLogResponse response = sessionLogService.getSessionLogById(id, profileId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET TOTAL STUDY TIME
     * ====================
     * Endpoint: GET /api/v1/session-logs/total-minutes?profileId=1
     *
     * Returns: Total study time in minutes (all time)
     *
     * Response: 1200 (JSON number)
     *
     * STATISTICS ENDPOINT:
     * - Simple aggregation
     * - Useful for "lifetime statistics"
     */
    @GetMapping("/total-minutes")
    public ResponseEntity<Long> getTotalStudyMinutes(
        @RequestParam Long profileId
    ) {
        Long totalMinutes = sessionLogService.getTotalStudyMinutes(profileId);
        return ResponseEntity.ok(totalMinutes);
    }

    /**
     * GET TOTAL STUDY TIME IN DATE RANGE
     * ===================================
     * Endpoint: GET /api/v1/session-logs/total-minutes/range
     *            ?profileId=1&startDate=...&endDate=...
     *
     * Returns: Total study time in specified period
     *
     * USE CASES:
     * - "You studied 15 hours this week!"
     * - "This month: 40 hours"
     * - "This year: 500 hours"
     */
    @GetMapping("/total-minutes/range")
    public ResponseEntity<Long> getTotalStudyMinutesInRange(
        @RequestParam Long profileId,
        @RequestParam Instant startDate,
        @RequestParam Instant endDate
    ) {
        Long totalMinutes = sessionLogService.getTotalStudyMinutesInRange(
            profileId,
            startDate,
            endDate
        );
        return ResponseEntity.ok(totalMinutes);
    }

    /**
     * GET TOTAL SESSION COUNT
     * =======================
     * Endpoint: GET /api/v1/session-logs/count?profileId=1
     *
     * Returns: Number of study sessions (all time)
     *
     * USE CASE:
     * - "You've completed 42 study sessions!"
     * - Gamification: Achievement badges
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalSessionCount(
        @RequestParam Long profileId
    ) {
        Long count = sessionLogService.getTotalSessionCount(profileId);
        return ResponseEntity.ok(count);
    }

    /**
     * DELETE SESSION LOG
     * ==================
     * Endpoint: DELETE /api/v1/session-logs/{id}?profileId=1
     *
     * USE CASE:
     * - Mistakenly logged session
     * - Incorrect data entry
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSessionLog(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        sessionLogService.deleteSessionLog(id, profileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ADVANCED PATTERNS (not implemented):
     * ===================================
     *
     * Update session log:
     * PATCH /api/v1/session-logs/{id}
     * - Fix duration, notes, or studiedAt
     *
     * Get session logs by subject:
     * GET /api/v1/session-logs?profileId=1&subjectId=2
     * - Filter by subject
     *
     * Export to CSV:
     * GET /api/v1/session-logs/export?profileId=1&format=csv
     * - Download study history
     * - Content-Type: text/csv
     *
     * Heatmap data (calendar view):
     * GET /api/v1/session-logs/heatmap?profileId=1&year=2024
     * - Returns: [{date: "2024-01-15", minutes: 90}, ...]
     * - For GitHub-style contribution calendar
     */
}
