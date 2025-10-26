package com.example.studybuddy.repository;

import com.example.studybuddy.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ProfileRepository - Data access layer for Profile entity
 *
 * SPRING DATA JPA MAGIC:
 * ======================
 * By extending JpaRepository<Profile, Long>, you automatically get 20+ methods:
 *
 * CRUD Operations:
 * - save(Profile) - Insert or update
 * - findById(Long) - Find by ID
 * - findAll() - Get all profiles
 * - delete(Profile) - Delete a profile
 * - deleteById(Long) - Delete by ID
 * - count() - Count total profiles
 * - existsById(Long) - Check if exists
 *
 * Batch Operations:
 * - saveAll(Iterable<Profile>) - Save multiple
 * - deleteAll() - Delete all
 *
 * And more!
 *
 * HOW IT WORKS:
 * =============
 * 1. Spring scans for @Repository interfaces
 * 2. At startup, Spring creates a PROXY class that implements this interface
 * 3. The proxy uses EntityManager (JPA) to execute database operations
 * 4. You get all functionality WITHOUT writing any implementation code!
 *
 * @Repository annotation:
 * - Marks this as a Data Access Object (DAO)
 * - Enables exception translation (SQL exceptions → Spring DataAccessException)
 * - Optional but recommended for clarity
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * CUSTOM QUERY METHOD: Find profile by email
     *
     * METHOD NAME QUERY DERIVATION:
     * ==============================
     * Spring Data JPA parses the method name and generates the query!
     *
     * Pattern: findBy + PropertyName + [Condition]
     *
     * "findByEmail" translates to:
     * SELECT p FROM Profile p WHERE p.email = ?1
     *
     * More examples:
     * - findByName(String name) → WHERE p.name = ?1
     * - findByEmailAndName(String email, String name) → WHERE p.email = ?1 AND p.name = ?2
     * - findByNameContaining(String name) → WHERE p.name LIKE %?1%
     * - findByCreatedAtAfter(Instant date) → WHERE p.created_at > ?1
     *
     * WHY OPTIONAL?
     * =============
     * Optional<Profile> instead of Profile because:
     * - Explicitly handles "not found" case
     * - Avoids NullPointerException
     * - Forces caller to handle missing data
     *
     * Usage:
     * Optional<Profile> result = profileRepository.findByEmail("test@example.com");
     * if (result.isPresent()) {
     *     Profile profile = result.get();
     * } else {
     *     // Handle not found
     * }
     *
     * Or use modern Java:
     * Profile profile = profileRepository.findByEmail(email)
     *     .orElseThrow(() -> new NotFoundException("Profile not found"));
     */
    Optional<Profile> findByEmail(String email);

    /**
     * Check if a profile exists with given email
     *
     * "existsByEmail" translates to:
     * SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profile p WHERE p.email = ?1
     *
     * WHY USE THIS?
     * - More efficient than findByEmail when you only need to check existence
     * - Returns boolean directly (no Optional needed)
     * - Database can optimize COUNT query
     *
     * Usage:
     * if (profileRepository.existsByEmail("test@example.com")) {
     *     throw new ConflictException("Email already exists");
     * }
     */
    boolean existsByEmail(String email);
}
