package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateSubjectRequest;
import com.example.studybuddy.dto.SubjectResponse;
import com.example.studybuddy.entity.Profile;
import com.example.studybuddy.entity.Subject;
import com.example.studybuddy.exception.ResourceNotFoundException;
import com.example.studybuddy.repository.ProfileRepository;
import com.example.studybuddy.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SubjectService - Business logic for Subject management
 *
 * DEMONSTRATES:
 * - Multi-repository coordination
 * - Ownership validation (security)
 * - Stream API for bulk DTO conversion
 */
@Service
@Transactional(readOnly = true)
public class SubjectService {

    /**
     * MULTIPLE DEPENDENCIES:
     * ======================
     * SubjectService needs both repositories:
     * - SubjectRepository: For subject operations
     * - ProfileRepository: To validate profile exists
     *
     * Spring injects both automatically!
     */
    private final SubjectRepository subjectRepository;
    private final ProfileRepository profileRepository;

    public SubjectService(SubjectRepository subjectRepository,
                         ProfileRepository profileRepository) {
        this.subjectRepository = subjectRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Create a new subject
     *
     * BUSINESS LOGIC:
     * 1. Validate profile exists (can't create subject for non-existent profile)
     * 2. Create subject linked to profile
     * 3. Save and return
     *
     * @Transactional ensures:
     * - If profile lookup fails → no subject created
     * - If subject save fails → rollback
     * - Atomicity: All-or-nothing
     */
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        // STEP 1: Validate profile exists
        // Why? Foreign key constraint would fail, but we want a better error message
        Profile profile = profileRepository.findById(request.getProfileId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Profile not found with id: " + request.getProfileId()
            ));

        // STEP 2: Create subject entity
        Subject subject = new Subject(
            profile,
            request.getName(),
            request.getDescription()
        );

        // STEP 3: Save to database
        Subject savedSubject = subjectRepository.save(subject);

        // STEP 4: Convert and return
        return convertToResponse(savedSubject);
    }

    /**
     * Get all subjects for a profile
     *
     * STREAM API:
     * ===========
     * Stream<Subject> → map() → Stream<SubjectResponse> → collect() → List
     *
     * Why streams?
     * - Functional style (declarative, readable)
     * - Chainable operations (map, filter, sort, etc.)
     * - Lazy evaluation (efficient for large datasets)
     *
     * Equivalent imperative code:
     * List<SubjectResponse> responses = new ArrayList<>();
     * for (Subject s : subjects) {
     *     responses.add(convertToResponse(s));
     * }
     * return responses;
     */
    public List<SubjectResponse> getSubjectsByProfileId(Long profileId) {
        // Validate profile exists (optional, but good practice)
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                "Profile not found with id: " + profileId
            );
        }

        List<Subject> subjects = subjectRepository.findByProfileId(profileId);

        // STREAM CONVERSION:
        // .stream() → convert List to Stream
        // .map(this::convertToResponse) → transform each Subject to SubjectResponse
        // .collect(Collectors.toList()) → collect back to List
        return subjects.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get subject by ID with ownership check
     *
     * OWNERSHIP VALIDATION (CRITICAL FOR SECURITY!):
     * ===============================================
     * Problem: Without check, user A could access user B's subject
     * Example: GET /subjects/999?profileId=1
     * - Subject 999 exists but belongs to profile 2
     * - Without check: Returns subject 999 (SECURITY BUG!)
     * - With check: Throws 404 (SECURE!)
     *
     * Pattern: findByIdAndProfileId
     * - Returns subject ONLY if it belongs to the specified profile
     * - Database does the check (efficient, one query)
     */
    public SubjectResponse getSubjectById(Long id, Long profileId) {
        Subject subject = subjectRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Subject not found with id: " + id + " for profile: " + profileId
            ));

        return convertToResponse(subject);
    }

    /**
     * Delete subject
     *
     * BUSINESS RULE: Can only delete own subjects
     *
     * @Transactional required for DELETE operations
     *
     * CASCADE DELETE:
     * - When subject is deleted, what happens to its plan items?
     * - Database CASCADE: DELETE FROM plan_items WHERE subject_id = ?
     * - Automatic cleanup!
     */
    @Transactional
    public void deleteSubject(Long id, Long profileId) {
        // Ownership check: Ensure subject belongs to profile
        Subject subject = subjectRepository.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Subject not found with id: " + id + " for profile: " + profileId
            ));

        // Delete (cascades to plan_items and session_logs)
        subjectRepository.delete(subject);
        // Alternative: subjectRepository.deleteById(id);
    }

    /**
     * Count subjects for a profile
     *
     * Simple count, useful for limits/quotas
     * Example business rule: "Maximum 10 subjects per profile"
     */
    public long countSubjectsByProfileId(Long profileId) {
        return subjectRepository.countByProfileId(profileId);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Convert Entity → Response DTO
     *
     * Note: We return profileId, not the full Profile object
     * - Avoids circular reference issues
     * - Smaller JSON payload
     * - Client usually already knows profileId
     */
    private SubjectResponse convertToResponse(Subject subject) {
        return new SubjectResponse(
            subject.getId(),
            subject.getProfile().getId(),  // Just the ID, not the whole object
            subject.getName(),
            subject.getDescription(),
            subject.getCreatedAt(),
            subject.getUpdatedAt()
        );
    }
}
