package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateSessionLogRequest;
import com.example.studybuddy.dto.SessionLogResponse;
import com.example.studybuddy.entity.PlanItem;
import com.example.studybuddy.entity.Profile;
import com.example.studybuddy.entity.SessionLog;
import com.example.studybuddy.entity.Subject;
import com.example.studybuddy.exception.ResourceNotFoundException;
import com.example.studybuddy.repository.PlanItemRepository;
import com.example.studybuddy.repository.ProfileRepository;
import com.example.studybuddy.repository.SessionLogRepository;
import com.example.studybuddy.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SessionLogService - Business logic for Session Log management
 *
 * DEMONSTRATES:
 * - Optional relationships (planItem can be null)
 * - Date range queries
 * - Aggregation calculations
 */
@Service
@Transactional(readOnly = true)
public class SessionLogService {

    private final SessionLogRepository sessionLogRepository;
    private final ProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;
    private final PlanItemRepository planItemRepository;

    public SessionLogService(SessionLogRepository sessionLogRepository,
                            ProfileRepository profileRepository,
                            SubjectRepository subjectRepository,
                            PlanItemRepository planItemRepository) {
        this.sessionLogRepository = sessionLogRepository;
        this.profileRepository = profileRepository;
        this.subjectRepository = subjectRepository;
        this.planItemRepository = planItemRepository;
    }

    /**
     * Create a new session log
     *
     * OPTIONAL RELATIONSHIPS:
     * =======================
     * planItem is optional:
     * - If provided: Link session to plan (planned study)
     * - If null: Just track study time (spontaneous study)
     *
     * TIMESTAMP HANDLING:
     * - If studiedAt provided: Use it (manual entry of past sessions)
     * - If null: Use current time (most common case)
     */
    @Transactional
    public SessionLogResponse createSessionLog(CreateSessionLogRequest request) {
        // STEP 1: Validate profile
        Profile profile = profileRepository.findById(request.getProfileId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Profile not found with id: " + request.getProfileId()
            ));

        // STEP 2: Validate subject AND ownership
        Subject subject = subjectRepository
            .findByIdAndProfileId(request.getSubjectId(), request.getProfileId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Subject not found with id: " + request.getSubjectId() +
                " for profile: " + request.getProfileId()
            ));

        // STEP 3: Validate plan item if provided (optional!)
        PlanItem planItem = null;
        if (request.getPlanItemId() != null) {
            planItem = planItemRepository
                .findByIdAndProfileId(request.getPlanItemId(), request.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Plan item not found with id: " + request.getPlanItemId() +
                    " for profile: " + request.getProfileId()
                ));
        }

        // STEP 4: Create session log
        SessionLog sessionLog = new SessionLog(
            profile,
            subject,
            planItem,  // Can be null
            request.getDurationMinutes()
        );

        // Set optional fields
        if (request.getStudiedAt() != null) {
            sessionLog.setStudiedAt(request.getStudiedAt());
        }
        // Else: @PrePersist sets to Instant.now()

        if (request.getNotes() != null) {
            sessionLog.setNotes(request.getNotes());
        }

        // STEP 5: Save
        SessionLog savedSessionLog = sessionLogRepository.save(sessionLog);

        return convertToResponse(savedSessionLog);
    }

    /**
     * Get all session logs for a profile
     */
    public List<SessionLogResponse> getSessionLogsByProfileId(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        List<SessionLog> sessionLogs = sessionLogRepository.findByProfileId(profileId);

        return sessionLogs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get session logs within date range
     *
     * DATE RANGE QUERIES:
     * ===================
     * Use case: Get last 7 days, this month, this year, etc.
     *
     * Example (last 7 days):
     * Instant now = Instant.now();
     * Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
     * getSessionLogsByDateRange(profileId, weekAgo, now);
     */
    public List<SessionLogResponse> getSessionLogsByDateRange(
        Long profileId,
        Instant startDate,
        Instant endDate
    ) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        List<SessionLog> sessionLogs = sessionLogRepository
            .findByProfileIdAndStudiedAtBetween(profileId, startDate, endDate);

        return sessionLogs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get session log by ID with ownership check
     */
    public SessionLogResponse getSessionLogById(Long id, Long profileId) {
        SessionLog sessionLog = sessionLogRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Session log not found with id: " + id + " for profile: " + profileId
            ));

        return convertToResponse(sessionLog);
    }

    /**
     * Calculate total study time (minutes) for profile
     *
     * AGGREGATION:
     * SUM(duration_minutes) across all sessions
     */
    public Long getTotalStudyMinutes(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        return sessionLogRepository.sumDurationMinutesByProfileId(profileId);
    }

    /**
     * Calculate total study time within date range
     *
     * Use case: "You studied 15 hours this week!"
     */
    public Long getTotalStudyMinutesInRange(
        Long profileId,
        Instant startDate,
        Instant endDate
    ) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        return sessionLogRepository.sumDurationMinutesByProfileIdAndDateRange(
            profileId,
            startDate,
            endDate
        );
    }

    /**
     * Count total study sessions
     *
     * Use case: "You've completed 42 study sessions!"
     */
    public Long getTotalSessionCount(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        return sessionLogRepository.countByProfileId(profileId);
    }

    /**
     * Delete session log
     */
    @Transactional
    public void deleteSessionLog(Long id, Long profileId) {
        SessionLog sessionLog = sessionLogRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Session log not found with id: " + id + " for profile: " + profileId
            ));

        sessionLogRepository.delete(sessionLog);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Convert Entity → Response DTO
     *
     * HANDLING NULL RELATIONSHIPS:
     * planItem can be null, so we check before accessing properties
     *
     * Java ternary operator:
     * condition ? valueIfTrue : valueIfFalse
     */
    private SessionLogResponse convertToResponse(SessionLog sessionLog) {
        return new SessionLogResponse(
            sessionLog.getId(),
            sessionLog.getProfile().getId(),
            sessionLog.getSubject().getId(),
            sessionLog.getSubject().getName(),
            // Handle optional planItem
            sessionLog.getPlanItem() != null ? sessionLog.getPlanItem().getId() : null,
            sessionLog.getPlanItem() != null ? sessionLog.getPlanItem().getTitle() : null,
            sessionLog.getStudiedAt(),
            sessionLog.getDurationMinutes(),
            sessionLog.getNotes(),
            sessionLog.getCreatedAt(),
            sessionLog.getUpdatedAt()
        );
    }
}
