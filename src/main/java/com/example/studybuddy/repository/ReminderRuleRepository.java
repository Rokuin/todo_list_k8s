package com.example.studybuddy.repository;

import com.example.studybuddy.entity.ReminderRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ReminderRuleRepository - Data access layer for ReminderRule entity
 *
 * This is a simpler repository since reminder functionality is not fully implemented yet.
 * In the future, a Kubernetes CronJob would query this repository to find active reminders
 * and trigger notifications.
 */
@Repository
public interface ReminderRuleRepository extends JpaRepository<ReminderRule, Long> {

    /**
     * Find all reminder rules for a profile
     */
    List<ReminderRule> findByProfileId(Long profileId);

    /**
     * Find reminder rule by ID with ownership check
     */
    Optional<ReminderRule> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Find all active reminder rules for a profile
     *
     * "findByProfileIdAndActive" translates to:
     * SELECT r FROM ReminderRule r
     * WHERE r.profile.id = ?1 AND r.active = ?2
     *
     * BOOLEAN QUERIES:
     * - findByActive(true) → WHERE r.active = true
     * - findByActiveTrue() → WHERE r.active = true (shorter syntax)
     * - findByActiveFalse() → WHERE r.active = false
     *
     * Usage:
     * List<ReminderRule> activeReminders = reminderRuleRepository
     *     .findByProfileIdAndActive(profileId, true);
     *
     * Or use the shorter syntax:
     * List<ReminderRule> activeReminders = reminderRuleRepository
     *     .findByProfileIdAndActiveTrue(profileId);
     */
    List<ReminderRule> findByProfileIdAndActive(Long profileId, Boolean active);

    /**
     * Shorter syntax for finding active reminders
     * Equivalent to findByProfileIdAndActive(profileId, true)
     */
    List<ReminderRule> findByProfileIdAndActiveTrue(Long profileId);

    /**
     * Find all active reminder rules (across all profiles)
     *
     * FUTURE USE CASE:
     * A Kubernetes CronJob would periodically call this method:
     *
     * @Scheduled(cron = "0 * * * * *")  // Every minute
     * public void checkReminders() {
     *     List<ReminderRule> activeRules = reminderRuleRepository.findByActiveTrue();
     *     for (ReminderRule rule : activeRules) {
     *         if (shouldTrigger(rule)) {
     *             sendNotification(rule.getProfile());
     *         }
     *     }
     * }
     */
    List<ReminderRule> findByActiveTrue();

    /**
     * Count active reminders for a profile
     *
     * Usage:
     * long activeCount = reminderRuleRepository.countByProfileIdAndActiveTrue(profileId);
     * System.out.println("You have " + activeCount + " active reminders");
     */
    long countByProfileIdAndActiveTrue(Long profileId);
}
