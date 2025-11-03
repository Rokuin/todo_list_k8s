package com.example.studybuddy.controller;

import com.example.studybuddy.dto.CreatePlanItemRequest;
import com.example.studybuddy.dto.PlanItemResponse;
import com.example.studybuddy.entity.PlanStatus;
import com.example.studybuddy.service.PlanItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * PlanItemController - REST API endpoints for Plan Item management
 *
 * DEMONSTRATES:
 * - PATCH operations (partial updates)
 * - Enum as request parameter
 * - Date range queries
 * - Multiple query parameters
 */
@RestController
@RequestMapping("/api/v1/plan-items")
public class PlanItemController {

    private final PlanItemService planItemService;

    public PlanItemController(PlanItemService planItemService) {
        this.planItemService = planItemService;
    }

    /**
     * CREATE PLAN ITEM
     * ================
     * Endpoint: POST /api/v1/plan-items
     *
     * Request body:
     * {
     *   "profileId": 1,
     *   "subjectId": 2,
     *   "title": "Complete Chapter 5",
     *   "deadline": "2024-01-20T10:00:00Z",
     *   "targetMinutes": 180
     * }
     */
    @PostMapping
    public ResponseEntity<PlanItemResponse> createPlanItem(
        @Valid @RequestBody CreatePlanItemRequest request
    ) {
        PlanItemResponse response = planItemService.createPlanItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET PLAN ITEMS
     * ==============
     * Endpoint: GET /api/v1/plan-items?profileId=1
     *
     * Optional filter by status:
     * GET /api/v1/plan-items?profileId=1&status=OPEN
     *
     * @RequestParam(required = false):
     * - Optional query parameter
     * - If not provided → null
     * - Service returns all plan items (no status filter)
     *
     * ENUM AS REQUEST PARAMETER:
     * ==========================
     * Spring automatically converts String → Enum
     * - "OPEN" → PlanStatus.OPEN
     * - "DONE" → PlanStatus.DONE
     * - "invalid" → 400 BAD REQUEST (invalid enum value)
     *
     * Case-insensitive by default:
     * - "open", "OPEN", "Open" all work
     */
    @GetMapping
    public ResponseEntity<List<PlanItemResponse>> getPlanItems(
        @RequestParam Long profileId,
        @RequestParam(required = false) PlanStatus status
    ) {
        List<PlanItemResponse> planItems;

        if (status != null) {
            // Filter by status
            planItems = planItemService.getPlanItemsByProfileIdAndStatus(profileId, status);
        } else {
            // Return all
            planItems = planItemService.getPlanItemsByProfileId(profileId);
        }

        return ResponseEntity.ok(planItems);
    }

    /**
     * GET PLAN ITEM BY ID
     * ===================
     * Endpoint: GET /api/v1/plan-items/{id}?profileId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanItemResponse> getPlanItemById(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        PlanItemResponse response = planItemService.getPlanItemById(id, profileId);
        return ResponseEntity.ok(response);
    }

    /**
     * MARK PLAN ITEM AS DONE
     * ======================
     * Endpoint: PATCH /api/v1/plan-items/{id}/done?profileId=1
     *
     * @PatchMapping:
     * - Partial update (update specific field/aspect)
     * - vs PUT: Full update (replace entire resource)
     *
     * WHY PATCH FOR STATUS CHANGE?
     * - We're only changing status, not the whole plan item
     * - RESTful: PATCH = partial update
     * - Simpler: No request body needed
     *
     * IDEMPOTENT:
     * - Calling multiple times has same effect
     * - OPEN → DONE (first call)
     * - DONE → DONE (subsequent calls, no error)
     *
     * Alternative design (not RESTful, but simpler):
     * POST /api/v1/plan-items/{id}/mark-done
     * - More action-oriented (RPC style)
     * - Less RESTful, but clearer intent
     *
     * Response: Updated plan item with status = DONE
     */
    @PatchMapping("/{id}/done")
    public ResponseEntity<PlanItemResponse> markPlanItemAsDone(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        PlanItemResponse response = planItemService.markPlanItemAsDone(id, profileId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET OVERDUE PLAN ITEMS
     * ======================
     * Endpoint: GET /api/v1/plan-items/overdue?profileId=1
     *
     * Returns plan items where:
     * - status = OPEN
     * - deadline < now
     *
     * USE CASE:
     * - Dashboard: "You have 3 overdue tasks!"
     * - Notifications
     * - Priority sorting
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<PlanItemResponse>> getOverduePlanItems(
        @RequestParam Long profileId
    ) {
        List<PlanItemResponse> overdue = planItemService.getOverduePlanItems(profileId);
        return ResponseEntity.ok(overdue);
    }

    /**
     * GET UPCOMING PLAN ITEMS
     * =======================
     * Endpoint: GET /api/v1/plan-items/upcoming?profileId=1&days=7
     *
     * Returns plan items due in next N days
     *
     * @RequestParam(defaultValue = "7"):
     * - Optional with default value
     * - If not provided → use 7
     * - Example: ?profileId=1 → days = 7
     * - Example: ?profileId=1&days=14 → days = 14
     *
     * DATE RANGE CALCULATION:
     * - Start: now
     * - End: now + days
     * - ChronoUnit.DAYS: Java 8+ time API
     *
     * USE CASE:
     * - "Tasks due this week"
     * - "Tasks due in next 2 weeks"
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<PlanItemResponse>> getUpcomingPlanItems(
        @RequestParam Long profileId,
        @RequestParam(defaultValue = "7") int days
    ) {
        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(days, ChronoUnit.DAYS);

        List<PlanItemResponse> upcoming = planItemService.getUpcomingPlanItems(
            profileId,
            startDate,
            endDate
        );

        return ResponseEntity.ok(upcoming);
    }

    /**
     * GET TOTAL TARGET MINUTES
     * ========================
     * Endpoint: GET /api/v1/plan-items/target-minutes?profileId=1
     *
     * Returns: Total planned study time (in minutes)
     *
     * USE CASE:
     * - Dashboard: "Total planned: 20 hours"
     * - Progress tracking
     */
    @GetMapping("/target-minutes")
    public ResponseEntity<Long> getTotalTargetMinutes(
        @RequestParam Long profileId
    ) {
        Long totalMinutes = planItemService.getTotalTargetMinutes(profileId);
        return ResponseEntity.ok(totalMinutes);
    }

    /**
     * GET REMAINING TARGET MINUTES
     * ============================
     * Endpoint: GET /api/v1/plan-items/remaining-minutes?profileId=1
     *
     * Returns: Planned time for OPEN items only
     *
     * USE CASE:
     * - Dashboard: "You have 10 hours of study left"
     * - Progress: "50% complete" (done minutes / total minutes)
     */
    @GetMapping("/remaining-minutes")
    public ResponseEntity<Long> getRemainingTargetMinutes(
        @RequestParam Long profileId
    ) {
        Long remainingMinutes = planItemService.getRemainingTargetMinutes(profileId);
        return ResponseEntity.ok(remainingMinutes);
    }

    /**
     * DELETE PLAN ITEM
     * ================
     * Endpoint: DELETE /api/v1/plan-items/{id}?profileId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlanItem(
        @PathVariable Long id,
        @RequestParam Long profileId
    ) {
        planItemService.deletePlanItem(id, profileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ADVANCED ENDPOINT PATTERNS (not implemented):
     * ============================================
     *
     * Bulk operations:
     * POST /api/v1/plan-items/bulk
     * - Create multiple plan items at once
     * - Request body: List<CreatePlanItemRequest>
     *
     * Reopen task (undo done):
     * PATCH /api/v1/plan-items/{id}/reopen
     * - DONE → OPEN transition
     *
     * Update deadline:
     * PATCH /api/v1/plan-items/{id}/deadline
     * - Request body: {"deadline": "2024-01-25T10:00:00Z"}
     *
     * Pagination for large lists:
     * GET /api/v1/plan-items?profileId=1&page=0&size=20&sort=deadline,asc
     * - Spring Data provides Pageable support
     */
}
