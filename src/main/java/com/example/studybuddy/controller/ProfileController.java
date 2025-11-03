package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CreateProfileRequest;
import com.example.studybuddy.dto.ProfileResponse;
import com.example.studybuddy.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ProfileController - REST API endpoints for Profile management
 *
 * REST CONTROLLER:
 * ================
 * @RestController = @Controller + @ResponseBody
 * - @Controller: Marks this as a Spring MVC controller
 * - @ResponseBody: Automatically converts return values to JSON
 *
 * WITHOUT @ResponseBody:
 * return "profile";  → Looks for view template "profile.html"
 *
 * WITH @ResponseBody (via @RestController):
 * return profileResponse;  → Converts to JSON automatically!
 *
 * JACKSON (JSON library):
 * - Spring Boot includes Jackson by default
 * - Automatically serializes Java objects → JSON
 * - Automatically deserializes JSON → Java objects
 *
 * @RequestMapping("/api/v1/profiles"):
 * - Base path for all endpoints in this controller
 * - Example: POST /api/v1/profiles
 * - Version in path (/v1) allows API evolution without breaking clients
 */
@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    /**
     * DEPENDENCY INJECTION:
     * ProfileService is injected by Spring (constructor injection)
     */
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * CREATE PROFILE
     * ==============
     * Endpoint: POST /api/v1/profiles
     *
     * Request body example:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com"
     * }
     *
     * @PostMapping:
     * - Maps HTTP POST requests to this method
     * - Shortcut for: @RequestMapping(method = RequestMethod.POST)
     *
     * @RequestBody:
     * - Binds HTTP request body to Java object
     * - Jackson deserializes JSON → CreateProfileRequest
     *
     * @Valid:
     * - Triggers Bean Validation on CreateProfileRequest
     * - Checks: @NotBlank, @Email, @Size, etc.
     * - If validation fails → 400 BAD REQUEST (automatic!)
     * - Error details included in response (by Spring)
     *
     * ResponseEntity:
     * - Wrapper for HTTP response
     * - Allows setting status code + headers + body
     * - status(HttpStatus.CREATED) → 201 Created
     * - body(response) → Response body (JSON)
     *
     * HTTP Status 201 CREATED:
     * - Standard status for successful POST (resource creation)
     * - Indicates new resource was created
     * - Client can use returned ID for subsequent requests
     */
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
        @Valid @RequestBody CreateProfileRequest request
    ) {
        ProfileResponse response = profileService.createProfile(request);

        // Return 201 CREATED with response body
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    /**
     * GET PROFILE BY ID
     * =================
     * Endpoint: GET /api/v1/profiles/{id}
     * Example: GET /api/v1/profiles/123
     *
     * @GetMapping("/{id}"):
     * - Maps HTTP GET requests with path variable
     * - {id} is a placeholder for actual value
     *
     * @PathVariable Long id:
     * - Extracts value from URL path
     * - GET /api/v1/profiles/123 → id = 123
     * - Automatically converts String "123" → Long 123
     * - If conversion fails (e.g., "abc") → 400 BAD REQUEST
     *
     * ResponseEntity.ok():
     * - Shortcut for status(200).body(...)
     * - 200 OK = Standard success status for GET
     *
     * WHAT IF PROFILE NOT FOUND?
     * - Service throws ResourceNotFoundException
     * - @ControllerAdvice catches it (we'll create this next!)
     * - Returns 404 NOT FOUND with error details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(
        @PathVariable Long id
    ) {
        ProfileResponse response = profileService.getProfileById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET PROFILE BY EMAIL
     * ====================
     * Endpoint: GET /api/v1/profiles/email?email=john@example.com
     *
     * @RequestParam String email:
     * - Extracts value from query string
     * - GET /api/v1/profiles/email?email=test@example.com → email = "test@example.com"
     *
     * QUERY PARAM vs PATH VARIABLE:
     * =============================
     * Path Variable (/@PathVariable):
     * - Required by default
     * - Part of resource identification
     * - Example: /profiles/123 (ID is part of resource path)
     *
     * Query Param (@RequestParam):
     * - Optional by default (use required=true to make required)
     * - Filters/search criteria
     * - Example: /profiles?status=active (filter by status)
     *
     * In this case: email is search criteria, so query param is appropriate
     */
    @GetMapping("/email")
    public ResponseEntity<ProfileResponse> getProfileByEmail(
        @RequestParam String email
    ) {
        ProfileResponse response = profileService.getProfileByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * CHECK IF PROFILE EXISTS
     * =======================
     * Endpoint: GET /api/v1/profiles/{id}/exists
     * Example: GET /api/v1/profiles/123/exists
     *
     * Response: true or false (JSON)
     *
     * USE CASE:
     * - Quick existence check without fetching full data
     * - Useful for validation in UI
     *
     * Alternative endpoint design:
     * HEAD /api/v1/profiles/{id}
     * - Returns 200 if exists, 404 if not
     * - No response body (lighter)
     * - Standard REST practice for existence checks
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> profileExists(
        @PathVariable Long id
    ) {
        boolean exists = profileService.profileExists(id);
        return ResponseEntity.ok(exists);
    }

    /**
     * FUTURE ENDPOINTS (not implemented yet):
     * ========================================
     *
     * Update profile:
     * @PutMapping("/{id}")
     * - Full update (replace entire resource)
     * - All fields required
     *
     * @PatchMapping("/{id}")
     * - Partial update (update specific fields)
     * - Only changed fields required
     *
     * Delete profile:
     * @DeleteMapping("/{id}")
     * - Delete resource
     * - Return 204 NO CONTENT (successful delete, no response body)
     *
     * List all profiles:
     * @GetMapping
     * - Return List<ProfileResponse>
     * - Add pagination: @RequestParam(defaultValue = "0") int page
     */
}
