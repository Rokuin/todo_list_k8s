package com.example.studybuddy.service;

import com.example.studybuddy.dto.CreateProfileRequest;
import com.example.studybuddy.dto.ProfileResponse;
import com.example.studybuddy.entity.Profile;
import com.example.studybuddy.exception.DuplicateResourceException;
import com.example.studybuddy.exception.ResourceNotFoundException;
import com.example.studybuddy.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileService - Business logic for Profile management
 *
 * SERVICE LAYER PATTERN:
 * ======================
 * Controller → calls → Service → calls → Repository → Database
 *
 * Controller: HTTP concerns (request/response)
 * Service: Business logic (THIS CLASS)
 * Repository: Data access (SQL)
 *
 * @Service:
 * - Marks this as a Spring-managed bean
 * - Spring creates ONE instance (singleton)
 * - Automatically injects dependencies (constructor injection)
 *
 * DEPENDENCY INJECTION:
 * =====================
 * Old way (manual):
 * ProfileRepository repo = new ProfileRepository();  // BAD!
 *
 * Spring way (automatic):
 * public ProfileService(ProfileRepository repo) {    // GOOD!
 *     this.repository = repo;
 * }
 * Spring automatically passes the repository instance!
 */
@Service
@Transactional(readOnly = true)  // All methods read-only by default (optimization)
public class ProfileService {

    /**
     * DEPENDENCY INJECTION:
     * ProfileRepository is injected by Spring
     *
     * Why 'final'?
     * - Immutable reference (can't be reassigned)
     * - Ensures dependency is always present
     * - Thread-safe
     */
    private final ProfileRepository profileRepository;

    /**
     * CONSTRUCTOR INJECTION:
     * ======================
     * Spring automatically calls this constructor and passes dependencies
     *
     * Benefits over field injection (@Autowired private Repository repo):
     * 1. Testability: Easy to create service in tests with mock repository
     * 2. Immutability: Can use 'final' fields
     * 3. Required dependencies: Constructor parameters are always required
     * 4. No reflection: Constructor is normal Java code
     *
     * Spring knows what to inject because:
     * - ProfileRepository is a Spring bean (@Repository)
     * - Constructor has ProfileRepository parameter
     * - Spring matches them automatically!
     */
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Create a new profile
     *
     * @Transactional (without readOnly):
     * - Starts a database transaction
     * - If method completes normally → COMMIT
     * - If exception is thrown → ROLLBACK
     *
     * FLOW:
     * 1. Check if email already exists (business rule)
     * 2. Convert DTO → Entity
     * 3. Save to database
     * 4. Convert Entity → DTO
     * 5. Return DTO to controller
     *
     * WHY THIS FLOW?
     * - Controller works with DTOs (clean API)
     * - Service works with both (translation layer)
     * - Repository works with Entities (JPA)
     */
    @Transactional  // READ-WRITE transaction (overrides class-level readOnly)
    public ProfileResponse createProfile(CreateProfileRequest request) {
        // BUSINESS RULE: Email must be unique
        // Bean Validation checks format, but not uniqueness (can't query DB)
        // Service layer checks uniqueness against database
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "Profile with email '" + request.getEmail() + "' already exists"
            );
        }

        // CONVERT DTO → ENTITY
        Profile profile = new Profile(
            request.getName(),
            request.getEmail()
        );
        // Note: ID and timestamps are set by JPA (@GeneratedValue, @PrePersist)

        // SAVE TO DATABASE
        Profile savedProfile = profileRepository.save(profile);
        // After save(), savedProfile has:
        // - Generated ID
        // - Timestamps set by @PrePersist

        // CONVERT ENTITY → DTO
        return convertToResponse(savedProfile);
    }

    /**
     * Get profile by ID
     *
     * @Transactional(readOnly = true):
     * - Read-only transaction (optimization)
     * - Database can optimize (no need for locks)
     * - Inherits from class-level annotation
     *
     * Optional<Profile> pattern:
     * - Repository returns Optional (might not exist)
     * - .orElseThrow() converts to exception if not found
     * - Controller will handle exception (404 status)
     */
    public ProfileResponse getProfileById(Long id) {
        Profile profile = profileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Profile not found with id: " + id
            ));

        return convertToResponse(profile);
    }

    /**
     * Get profile by email
     *
     * Useful for login/lookup scenarios
     */
    public ProfileResponse getProfileByEmail(String email) {
        Profile profile = profileRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Profile not found with email: " + email
            ));

        return convertToResponse(profile);
    }

    /**
     * Check if profile exists
     *
     * Simple boolean check, no DTO conversion needed
     */
    public boolean profileExists(Long id) {
        return profileRepository.existsById(id);
    }

    // ============================================
    // HELPER METHODS (private)
    // ============================================

    /**
     * Convert Entity → Response DTO
     *
     * MAPPER PATTERN:
     * ===============
     * Centralized conversion logic
     * - Reused by all service methods
     * - Easy to maintain (one place to update)
     * - Could be replaced by MapStruct library for complex apps
     *
     * MapStruct = Annotation-based mapper generator
     * Generates conversion code at compile time (very fast!)
     * For this project, manual mapping is fine (educational)
     */
    private ProfileResponse convertToResponse(Profile profile) {
        return new ProfileResponse(
            profile.getId(),
            profile.getName(),
            profile.getEmail(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
