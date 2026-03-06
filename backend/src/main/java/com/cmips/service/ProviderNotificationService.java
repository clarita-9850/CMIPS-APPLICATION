package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.Notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Provider Notification Service
 * Generates notifications and tasks for provider enrollment events.
 * Implements PVM notification codes from DSD Section 23.
 */
@Service
public class ProviderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ProviderNotificationService.class);

    private final NotificationService notificationService;
    private final TaskService taskService;

    public ProviderNotificationService(NotificationService notificationService, TaskService taskService) {
        this.notificationService = notificationService;
        this.taskService = taskService;
    }

    /**
     * PVM-01,02,03: Medi-Cal match found - provider is on suspended list
     */
    public void notifyMediCalSuspended(ProviderEntity provider) {
        log.info("PVM-01/02/03: Medi-Cal suspended notification for provider {}", provider.getId());

        Notification notification = Notification.builder()
                .userId(provider.getCreatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") has been matched to the Medi-Cal Suspended/Ineligible list. " +
                        "Provider enrollment has been set to ineligible.")
                .notificationType(NotificationType.ALERT)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-14,15: Provider terminated from enrollment (eligible YES→NO)
     */
    public void notifyProviderTerminatedEnrollment(ProviderEntity provider, String reason) {
        log.info("PVM-14/15: Provider {} terminated enrollment, reason: {}", provider.getId(), reason);

        Notification notification = Notification.builder()
                .userId(provider.getUpdatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") enrollment has been terminated. Reason: " + reason +
                        ". All active case assignments have been terminated.")
                .notificationType(NotificationType.WARNING)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-10: Reinstatement requested - task to supervisor
     */
    public void notifyReinstatementRequested(ProviderEntity provider) {
        log.info("PVM-10: Reinstatement requested for provider {}", provider.getId());

        Task task = Task.builder()
                .title("Approve Reinstatement for Provider " + provider.getProviderNumber())
                .description("Provider " + provider.getFullName() +
                        " has requested reinstatement. Review and approve or reject.")
                .workQueue("PROVIDER_ENROLLMENT")
                .queueRole("SUPERVISOR")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.HIGH)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId() + "/approve-enrollment")
                .createdBy(provider.getUpdatedBy())
                .build();
        taskService.createTask(task);
    }

    /**
     * PVM-11: Reinstatement approved - notification to case owner
     */
    public void notifyReinstatementApproved(ProviderEntity provider) {
        log.info("PVM-11: Reinstatement approved for provider {}", provider.getId());

        Notification notification = Notification.builder()
                .userId(provider.getCreatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") reinstatement has been approved. Provider is now eligible to serve.")
                .notificationType(NotificationType.SUCCESS)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-12: Reinstatement rejected - task to case owner
     */
    public void notifyReinstatementRejected(ProviderEntity provider) {
        log.info("PVM-12: Reinstatement rejected for provider {}", provider.getId());

        Task task = Task.builder()
                .title("Reinstatement Rejected for Provider " + provider.getProviderNumber())
                .description("Provider " + provider.getFullName() +
                        " reinstatement request has been rejected. Provider remains ineligible.")
                .workQueue("PROVIDER_ENROLLMENT")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.MEDIUM)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .createdBy(provider.getUpdatedBy())
                .build();
        taskService.createTask(task);
    }

    /**
     * PVM-24,25: OT violation ineligible (3rd or 4th violation)
     */
    public void notifyOTViolationIneligible(ProviderEntity provider) {
        log.info("PVM-24/25: OT violation ineligible for provider {}", provider.getId());

        Notification notification = Notification.builder()
                .userId(provider.getUpdatedBy() != null ? provider.getUpdatedBy() : provider.getCreatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") has been set ineligible due to overtime violation. " +
                        "All active assignments will be terminated.")
                .notificationType(NotificationType.ALERT)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-28: Violation set inactive (dismissed)
     */
    public void notifyOTViolationDismissed(ProviderEntity provider) {
        log.info("PVM-28: OT violation dismissed for provider {}", provider.getId());

        Notification notification = Notification.builder()
                .userId(provider.getUpdatedBy() != null ? provider.getUpdatedBy() : provider.getCreatedBy())
                .message("Overtime violation for Provider " + provider.getFullName() +
                        " (ID: " + provider.getProviderNumber() + ") has been dismissed.")
                .notificationType(NotificationType.INFO)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-32: 90-day ineligibility period expired
     */
    public void notifyOT90DaysPassed(ProviderEntity provider) {
        log.info("PVM-32: 90-day OT ineligibility period expired for provider {}", provider.getId());

        Notification notification = Notification.builder()
                .userId(provider.getCreatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") 90-day overtime violation ineligibility period has expired. " +
                        "Provider may be eligible for reinstatement.")
                .notificationType(NotificationType.INFO)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-37: 75 days past enrollment begin date - enrollment due date notification
     */
    public void notifyEnrollmentDueDate(ProviderEntity provider) {
        log.info("PVM-37: Enrollment due date approaching for provider {}", provider.getId());

        Task task = Task.builder()
                .title("Enrollment Due Date Approaching for Provider " + provider.getProviderNumber())
                .description("Provider " + provider.getFullName() +
                        " enrollment due date is approaching. Enrollment begin date was " +
                        provider.getEnrollmentBeginDate() + ". Due date: " + provider.getEnrollmentDueDate())
                .workQueue("PROVIDER_ENROLLMENT")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.HIGH)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .createdBy("SYSTEM")
                .build();
        taskService.createTask(task);
    }

    /**
     * PVM-39: Pending provider inactivated on case
     */
    public void notifyProviderInactivatedOnCase(ProviderAssignmentEntity assignment) {
        log.info("PVM-39: Pending provider {} inactivated on case {}", assignment.getProviderId(), assignment.getCaseId());

        Notification notification = Notification.builder()
                .userId(assignment.getUpdatedBy())
                .message("Pending provider (ID: " + assignment.getProviderId() +
                        ") has been inactivated on case " + assignment.getCaseId() + ".")
                .notificationType(NotificationType.INFO)
                .relatedEntityType("PROVIDER_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .actionLink("/cases/" + assignment.getCaseId() + "/providers")
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-40: Provider assigned to case
     */
    public void notifyProviderAssigned(ProviderAssignmentEntity assignment) {
        log.info("PVM-40: Provider {} assigned to case {}", assignment.getProviderId(), assignment.getCaseId());

        Notification notification = Notification.builder()
                .userId(assignment.getCreatedBy())
                .message("Provider (ID: " + assignment.getProviderId() +
                        ") has been assigned to case " + assignment.getCaseId() +
                        ". SOC 2271 notification should be generated.")
                .notificationType(NotificationType.INFO)
                .relatedEntityType("PROVIDER_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .actionLink("/cases/" + assignment.getCaseId() + "/providers")
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-04: Provider added and terminated in single pay period
     */
    public void notifyProviderAddedTerminatedSamePeriod(ProviderAssignmentEntity assignment) {
        log.info("PVM-04: Provider {} added and terminated in same pay period on case {}",
                assignment.getProviderId(), assignment.getCaseId());

        Notification notification = Notification.builder()
                .userId(assignment.getUpdatedBy())
                .message("Provider (ID: " + assignment.getProviderId() +
                        ") was added and terminated within the same pay period on case " +
                        assignment.getCaseId() + ".")
                .notificationType(NotificationType.WARNING)
                .relatedEntityType("PROVIDER_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .actionLink("/cases/" + assignment.getCaseId() + "/providers")
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-06: Provider moved out of state (address state != CA)
     */
    public void notifyProviderMovedOutOfState(ProviderEntity provider) {
        log.info("PVM-06: Provider {} moved out of state", provider.getId());

        Notification notification = Notification.builder()
                .userId(provider.getUpdatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") address has been changed to a state other than California.")
                .notificationType(NotificationType.WARNING)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-07: Provider end date within 30 days
     */
    public void notifyProviderEndDateWithin30Days(ProviderAssignmentEntity assignment) {
        log.info("PVM-07: Provider {} end date within 30 days on case {}",
                assignment.getProviderId(), assignment.getCaseId());

        Notification notification = Notification.builder()
                .userId(assignment.getCreatedBy())
                .message("Active provider (ID: " + assignment.getProviderId() +
                        ") end date is within 30 days on case " + assignment.getCaseId() + ".")
                .notificationType(NotificationType.WARNING)
                .relatedEntityType("PROVIDER_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .actionLink("/cases/" + assignment.getCaseId() + "/providers")
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-45: Provider address matches recipient address
     */
    public void notifyProviderAddressMatchesRecipient(ProviderEntity provider, Long recipientId) {
        log.info("PVM-45: Provider {} address matches recipient {}", provider.getId(), recipientId);

        Notification notification = Notification.builder()
                .userId(provider.getUpdatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") address matches recipient (ID: " + recipientId + ") address.")
                .notificationType(NotificationType.INFO)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-15: WPCS provider terminated from enrollment
     * Separate notification for WPCS (Waiver Personal Care Services) providers
     */
    public void notifyWpcsProviderTerminated(ProviderEntity provider, String reason) {
        log.info("PVM-15: WPCS provider {} terminated, reason: {}", provider.getId(), reason);

        Notification notification = Notification.builder()
                .userId(provider.getUpdatedBy() != null ? provider.getUpdatedBy() : provider.getCreatedBy())
                .message("WPCS Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") enrollment has been terminated. Reason: " + reason +
                        ". All WPCS case assignments have been terminated. " +
                        "Reassessment of recipient WPCS hours may be required.")
                .notificationType(NotificationType.WARNING)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-42: SOC 2271 notification generated for provider assignment
     */
    public void notifySoc2271Generated(ProviderAssignmentEntity assignment) {
        log.info("PVM-42: SOC 2271 generated for provider {} on case {}",
                assignment.getProviderId(), assignment.getCaseId());

        Notification notification = Notification.builder()
                .userId(assignment.getCreatedBy())
                .message("SOC 2271 (Notice of Provider Enrollment) has been generated for provider (ID: " +
                        assignment.getProviderId() + ") on case " + assignment.getCaseId() + ".")
                .notificationType(NotificationType.INFO)
                .relatedEntityType("PROVIDER_ASSIGNMENT")
                .relatedEntityId(assignment.getId())
                .actionLink("/cases/" + assignment.getCaseId() + "/providers")
                .build();
        notificationService.createNotification(notification);
    }

    /**
     * PVM-43: Provider agreement notification
     */
    public void notifyProviderAgreementRequired(ProviderEntity provider) {
        log.info("PVM-43: Provider agreement required for provider {}", provider.getId());

        Task task = Task.builder()
                .title("Provider Agreement Required for " + provider.getProviderNumber())
                .description("Provider " + provider.getFullName() +
                        " requires a signed Provider Agreement (SOC 846). " +
                        "Both Provider Agreement and Overtime Agreement must be completed.")
                .workQueue("PROVIDER_ENROLLMENT")
                .status(Task.TaskStatus.OPEN)
                .priority(Task.TaskPriority.MEDIUM)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .createdBy("SYSTEM")
                .build();
        taskService.createTask(task);
    }

    /**
     * PVM-46: Provider enrolled in another county notification
     */
    public void notifyProviderEnrolledInAnotherCounty(ProviderEntity provider, String otherCounty) {
        log.info("PVM-46: Provider {} enrolled in another county: {}", provider.getId(), otherCounty);

        Notification notification = Notification.builder()
                .userId(provider.getCreatedBy())
                .message("Provider " + provider.getFullName() + " (ID: " + provider.getProviderNumber() +
                        ") is already enrolled in county " + otherCounty +
                        ". Multi-county enrollment rules apply.")
                .notificationType(NotificationType.WARNING)
                .relatedEntityType("PROVIDER")
                .relatedEntityId(provider.getId())
                .actionLink("/providers/" + provider.getId())
                .build();
        notificationService.createNotification(notification);
    }
}
