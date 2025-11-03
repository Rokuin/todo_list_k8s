package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CreateSubjectRequest;
import com.example.studybuddy.dto.SubjectResponse;
import com.example.studybuddy.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SubjectController - REST API endpoints for Subject management
 *
 * DEMONSTRATES:
 * - Query parameters for filtering
 * - DELETE operations
 * - List responses
 */
@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    /**
     * CREATE SUBJECT
     * ==============
     * Endpoint: POST /api/v1/subjects
     *
     * Request body:
     * {
     *   "profileId": 1,
     *   "name": "Math",
     *   "description": "Mathematics course"
     * }
     *
     * VALIDATION FLOW:
     * 1. @Valid triggers Bean Validation on CreateSubjectRequest
     * 2. Checks @NotNull, @NotBlank, @Size
     * 3. If passes → call service
     * 4. Service validates: profile exists, etc.
     * 5. If all valid → 201 CREATED
     *
     * POSSIBLE ERRORS:
     * - 400 BAD REQUEST: Validation failure (@NotBlank, etc.)
     * - 404 NOT FOUND: Profile doesn't exist
     */
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
        @Valid @RequestBody CreateSubjectRequest request
    ) {
        SubjectResponse response = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET SUBJECTS BY PROFILE
     * =======================
     * Endpoint: GET /api/v1/subjects?profileId=1
     *
     * @RequestParam Long profileId:
     * - Required query parameter
     * - Client MUST provide profileId
     * - If missing → 400 BAD REQUEST
     *
     * WHY REQUIRED?
     * - Without profileId, we'd return ALL subjects (security issue!)
     * - Users should only see their own subjects
     *
     * WITH AUTHENTICATION (future):
     * - Get profileId from JWT token (no query param needed)
     * - GET /api/v1/subjects → returns current user's subjects
     *
     * Response: Array of subjects
     * [
     *   {"id": 1, "name": "Math", ...},
     *   {"id": 2, "name": "English", ...}
     * ]
     *
     * EMPTY LIST vs 404:
     * - If no subjects: Return [] (empty array) with 200 OK
     * - NOT 404! 404 means "endpoint not found", not "no data"
     */
    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getSubjects(
        @RequestParam Long profileId
    ) {
        List<SubjectResponse> subjects = subjectService.getSubjectsByProfileId(profileId);
        return ResponseEntity.ok(subjects);
    }

    /**
     * GET SUBJECT BY ID
     * =================
     * Endpoint: GET /api/v1/subjects/{id}?profileId=1
     *
     * COMBINES:
     * - @PathVariable Long id (from URL path)
     * - @RequestParam Long profileId (from query string)
     *
     * Example: GET /api/v1/subjects/5?profileId=1
     * - id = 5
     * - profileId = 1
     *
     * OWNERSHIP CHECK:
     * - Service validates: subject 5 belongs to profile 1
     * - If not → 404 NOT FOUND (security!)
     *
     * WHY BOTH ID AND PROFILE ID?
     * - ID identifies the resource
     * - profileId enforces ownership
     * - Alternative: Get profileId from authentication token
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubjectById(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        SubjectResponse response = subjectService.getSubjectById(id, profileId);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE SUBJECT
     * ==============
     * Endpoint: DELETE /api/v1/subjects/{id}?profileId=1
     *
     * @DeleteMapping:
     * - Maps HTTP DELETE requests
     * - Idempotent: Deleting same resource twice has same effect
     * - First delete: 204 NO CONTENT
     * - Second delete: 404 NOT FOUND (already gone)
     *
     * ResponseEntity.noContent().build():
     * - 204 NO CONTENT status
     * - No response body
     * - Standard for successful DELETE operations
     *
     * WHY 204 vs 200?
     * - 200 OK: Usually has response body
     * - 204 NO CONTENT: Success, but no body (resource is gone!)
     *
     * CASCADE DELETE:
     * - Database CASCADE: Deletes related plan_items and session_logs
     * - Automatic cleanup!
     * - Alternative: Soft delete (mark as deleted, don't actually delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        subjectService.deleteSubject(id, profileId);

        // Return 204 NO CONTENT (successful delete, no body)
        return ResponseEntity.noContent().build();
    }

    /**
     * COUNT SUBJECTS
     * ==============
     * Endpoint: GET /api/v1/subjects/count?profileId=1
     *
     * Response: {"count": 5} or just 5
     *
     * USE CASE:
     * - Check if user reached subject limit
     * - Display statistics
     *
     * DESIGN CHOICE:
     * We return primitive long (wrapped in ResponseEntity)
     * Jackson serializes: 5 (just a number)
     *
     * Alternative: Return wrapper object {"count": 5}
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSubjects(
        @RequestParam Long profileId
    ) {
        long count = subjectService.countSubjectsByProfileId(profileId);
        return ResponseEntity.ok(count);
    }

    /**
     * REST API DESIGN BEST PRACTICES:
     * ================================
     *
     * 1. Use HTTP methods correctly:
     *    - GET: Read (idempotent, safe)
     *    - POST: Create (not idempotent)
     *    - PUT: Full update (idempotent)
     *    - PATCH: Partial update (idempotent)
     *    - DELETE: Delete (idempotent)
     *
     * 2. Use correct status codes:
     *    - 200 OK: Successful GET/PATCH/PUT
     *    - 201 CREATED: Successful POST
     *    - 204 NO CONTENT: Successful DELETE
     *    - 400 BAD REQUEST: Validation error
     *    - 404 NOT FOUND: Resource not found
     *    - 409 CONFLICT: Duplicate resource
     *
     * 3. Use plural nouns for resources:
     *    - /profiles (not /profile)
     *    - /subjects (not /subject)
     *
     * 4. Use nested resources sparingly:
     *    - Good: /subjects?profileId=1
     *    - Avoid: /profiles/1/subjects/2/plan-items/3 (too deep!)
     *
     * 5. Version your API:
     *    - /api/v1/subjects
     *    - Allows breaking changes in v2 without affecting v1 clients
     *
     * 6. Use query params for filtering/pagination:
     *    - /subjects?profileId=1&status=active&page=0&size=20
     */
}
