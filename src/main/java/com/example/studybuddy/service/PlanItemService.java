package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreatePlanItemRequest;
import com.example.studybuddy.dto.PlanItemResponse;
import com.example.studybuddy.entity.PlanItem;
import com.example.studybuddy.entity.PlanStatus;
import com.example.studybuddy.entity.Profile;
import com.example.studybuddy.entity.Subject;
import com.example.studybuddy.exception.InvalidRequestException;
import com.example.studybuddy.exception.ResourceNotFoundException;
import com.example.studybuddy.repository.PlanItemRepository;
import com.example.studybuddy.repository.ProfileRepository;
import com.example.studybuddy.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PlanItemService - Business logic for Plan Item management
 *
 * DEMONSTRATES:
 * - Coordinating 3 repositories
 * - Status transitions (state machine)
 * - Complex business rules
 * - Overdue detection
 */
@Service
@Transactional(readOnly = true)
public class PlanItemService {

    private final PlanItemRepository planItemRepository;
    private final ProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;

    /**
     * CONSTRUCTOR INJECTION with 3 dependencies
     * Spring resolves all automatically!
     */
    public PlanItemService(PlanItemRepository planItemRepository,
                          ProfileRepository profileRepository,
                          SubjectRepository subjectRepository) {
        this.planItemRepository = planItemRepository;
        this.profileRepository = profileRepository;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Create a new plan item
     *
     * COMPLEX VALIDATION:
     * 1. Profile must exist
     * 2. Subject must exist
     * 3. Subject must belong to profile (ownership!)
     *
     * Why validate subject ownership?
     * - Prevent user A from creating plan items for user B's subjects
     * - Security: Ensure all data belongs to same user
     */
    @Transactional
    public PlanItemResponse createPlanItem(CreatePlanItemRequest request) {
        // STEP 1: Validate profile exists
        Profile profile = profileRepository.findById(request.getProfileId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Profile not found with id: " + request.getProfileId()
            ));

        // STEP 2: Validate subject exists AND belongs to profile
        Subject subject = subjectRepository
            .findByIdAndProfileId(request.getSubjectId(), request.getProfileId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Subject not found with id: " + request.getSubjectId() +
                " for profile: " + request.getProfileId()
            ));

        // STEP 3: Create plan item
        PlanItem planItem = new PlanItem(profile, subject, request.getTitle());
        planItem.setDeadline(request.getDeadline());
        planItem.setTargetMinutes(request.getTargetMinutes());
        // Note: status is automatically set to OPEN by @PrePersist

        // STEP 4: Save
        PlanItem savedPlanItem = planItemRepository.save(planItem);

        // STEP 5: Convert and return
        return convertToResponse(savedPlanItem);
    }

    /**
     * Get all plan items for a profile
     */
    public List<PlanItemResponse> getPlanItemsByProfileId(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        List<PlanItem> planItems = planItemRepository.findByProfileId(profileId);

        return planItems.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get plan items filtered by status
     *
     * Use cases:
     * - Show only OPEN tasks (todo list)
     * - Show only DONE tasks (completed work)
     */
    public List<PlanItemResponse> getPlanItemsByProfileIdAndStatus(
        Long profileId,
        PlanStatus status
    ) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        List<PlanItem> planItems = planItemRepository
            .findByProfileIdAndStatus(profileId, status);

        return planItems.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get plan item by ID with ownership check
     */
    public PlanItemResponse getPlanItemById(Long id, Long profileId) {
        PlanItem planItem = planItemRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Plan item not found with id: " + id + " for profile: " + profileId
            ));

        return convertToResponse(planItem);
    }

    /**
     * Mark plan item as done
     *
     * STATE TRANSITION:
     * =================
     * OPEN → DONE (allowed)
     * DONE → DONE (idempotent, no error)
     *
     * IDEMPOTENT: Calling multiple times has same effect as calling once
     * Example: Clicking "Mark Done" button twice doesn't cause error
     *
     * Alternative approach (strict):
     * if (planItem.getStatus() == PlanStatus.DONE) {
     *     throw new InvalidRequestException("Plan item already done");
     * }
     */
    @Transactional
    public PlanItemResponse markPlanItemAsDone(Long id, Long profileId) {
        // Ownership check
        PlanItem planItem = planItemRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Plan item not found with id: " + id + " for profile: " + profileId
            ));

        // State transition
        planItem.markAsDone();  // Sets status to DONE

        // Save (JPA automatically detects changes and updates)
        // @PreUpdate callback sets updatedAt timestamp
        PlanItem updatedPlanItem = planItemRepository.save(planItem);

        return convertToResponse(updatedPlanItem);
    }

    /**
     * Get overdue plan items for a profile
     *
     * BUSINESS LOGIC:
     * - Status = OPEN
     * - Deadline < now
     *
     * Use case: "You have 3 overdue tasks!"
     */
    public List<PlanItemResponse> getOverduePlanItems(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        List<PlanItem> overdue = planItemRepository
            .findByProfileIdAndStatusAndDeadlineBefore(
                profileId,
                PlanStatus.OPEN,
                Instant.now()
            );

        return overdue.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get upcoming plan items (due soon)
     *
     * Example: Tasks due in next 7 days
     *
     * @param profileId Profile ID
     * @param startDate Start of range (usually now)
     * @param endDate End of range (e.g., 7 days from now)
     */
    public List<PlanItemResponse> getUpcomingPlanItems(
        Long profileId,
        Instant startDate,
        Instant endDate
    ) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        List<PlanItem> upcoming = planItemRepository
            .findByProfileIdAndDeadlineBetween(profileId, startDate, endDate);

        return upcoming.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Calculate total target minutes for profile
     *
     * AGGREGATION:
     * Sum of all targetMinutes for profile's plan items
     *
     * Use case: "Total planned study time: 20 hours"
     */
    public Long getTotalTargetMinutes(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        return planItemRepository.sumTargetMinutesByProfileId(profileId);
    }

    /**
     * Calculate remaining target minutes (OPEN items only)
     *
     * Use case: "You have 10 hours of study planned"
     */
    public Long getRemainingTargetMinutes(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }

        return planItemRepository.sumTargetMinutesByProfileIdAndStatus(
            profileId,
            PlanStatus.OPEN
        );
    }

    /**
     * Delete plan item
     */
    @Transactional
    public void deletePlanItem(Long id, Long profileId) {
        PlanItem planItem = planItemRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Plan item not found with id: " + id + " for profile: " + profileId
            ));

        planItemRepository.delete(planItem);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Convert Entity → Response DTO
     *
     * Include subject name for convenience
     * Client can display "Math: Complete Chapter 5" without extra request
     */
    private PlanItemResponse convertToResponse(PlanItem planItem) {
        return new PlanItemResponse(
            planItem.getId(),
            planItem.getProfile().getId(),
            planItem.getSubject().getId(),
            planItem.getSubject().getName(),  // Include subject name
            planItem.getTitle(),
            planItem.getDeadline(),
            planItem.getTargetMinutes(),
            planItem.getStatus(),
            planItem.getCreatedAt(),
            planItem.getUpdatedAt()
        );
    }
}
