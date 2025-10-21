package com.example.studybuddy.entity;

/**
 * PlanStatus Enum - Represents the status of a plan item
 *
 * ENUM in Java = A fixed set of constants
 * Benefits:
 * - Type safety: Can't assign invalid values
 * - Readable: OPEN is clearer than 0 or "open"
 * - Compiler checks: Typos caught at compile time
 *
 * Example usage:
 *   PlanStatus status = PlanStatus.OPEN;
 *   if (status == PlanStatus.DONE) { ... }
 */
public enum PlanStatus {
    /**
     * Task is not yet completed
     */
    OPEN,

    /**
     * Task has been completed
     */
    DONE
}
