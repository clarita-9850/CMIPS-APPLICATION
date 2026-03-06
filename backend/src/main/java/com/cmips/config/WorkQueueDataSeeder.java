package com.cmips.config;

import com.cmips.entity.Task;
import com.cmips.entity.TaskType;
import com.cmips.entity.WorkQueue;
import com.cmips.repository.TaskTypeRepository;
import com.cmips.repository.WorkQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.cmips.entity.WorkQueue.QueueCategory.*;

/**
 * Seeds the work_queues and task_types tables with all DSD-defined queues and task types.
 * Only inserts if the tables are empty (safe to re-run).
 */
@Component
@Order(10)
public class WorkQueueDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WorkQueueDataSeeder.class);

    private final WorkQueueRepository queueRepo;
    private final TaskTypeRepository taskTypeRepo;

    public WorkQueueDataSeeder(WorkQueueRepository queueRepo, TaskTypeRepository taskTypeRepo) {
        this.queueRepo = queueRepo;
        this.taskTypeRepo = taskTypeRepo;
    }

    @Override
    public void run(String... args) {
        seedQueues();
        seedTaskTypes();
    }

    private void seedQueues() {
        if (queueRepo.count() > 0) {
            log.info("Work queues already seeded ({} exist), skipping", queueRepo.count());
            return;
        }

        log.info("Seeding work queues...");

        // Case Management Queues
        q("CASE_OWNER", "Case Owner Queue", "Primary queue for case assignment and general case management tasks", CASE_MGMT, false);
        q("CASE_WORKER", "Case Worker Queue", "Frontline tasks — death confirmations, notifications, determinations", CASE_MGMT, false);
        q("CASE_HOME", "Case Home Queue", "Home visits, health care certifications, case-specific maintenance", CASE_MGMT, false);
        q("CASE_CLOSURE", "Case Closure Queue", "Companion case rescissions, funding source updates, termination workflows", CASE_MGMT, false);
        q("WPCS_CASE", "WPCS Case Queue", "Welfare-to-Work/Payroll Services — Medi-Cal discontinuance for WPCS cases", CASE_MGMT, false);
        q("HEALTH_CARE_CERT", "Health Care Certification Queue", "Health care certification due date monitoring", CASE_MGMT, false);
        q("FORM_COMPLETION", "Form Completion Queue", "Electronic form submissions processing", CASE_MGMT, false);

        // QA Queues
        q("QA", "QA Queue", "Paid claim matching, death match processing (SCD source)", QA, false);
        q("QA_SUPERVISOR", "QA Supervisor Queue", "Escalated QA tasks — paid claim match not actioned within 5 days", QA, true);

        // Payroll Queues
        q("PAYMENTS_PENDING_APPROVAL", "Payments Pending Approval Queue", "Special transaction approvals, payment correction approvals", PAYROLL, false);
        q("PAYROLL_SUPERVISOR", "Payroll Supervisor Work Queue", "Overpayment recovery, escalated payment approvals", PAYROLL, true);
        q("SPECIAL_TRANSACTION_REQUESTER", "Special Transaction Requester Queue", "Rejected special transactions returned to requester for correction", PAYROLL, false);
        q("PAYMENT_SEARCH_BY_CASE", "Payment Search by Case Queue", "Cross-county advance pay reconciliation", PAYROLL, false);
        q("OVERPAYMENT_RECOVERY", "Overpayment Recovery Queue", "Overpayment recovery tracking, collection notifications", PAYROLL, false);

        // Training / CDSS Queues
        q("CDSS_PAYMENTS_PENDING", "CDSS Payments Pending Approval Queue", "Training time claim approvals (1st and 2nd level)", TRAINING, false);
        q("CDSS_CAREER_PATHWAYS", "CDSS Career Pathways Work Queue", "Career pathway training approvals", TRAINING, false);
        q("SUBMITTED_BY_WORKER", "Submitted By Worker Queue", "Rejected training time claims returned to submitter", TRAINING, false);

        // Provider Management Queues
        q("PROVIDER_MANAGEMENT", "Provider Management Queue", "Provider enrollment, modifications, overpayment recovery designation", PROVIDER, false);
        q("TIMESHEET_ELIGIBILITY", "Timesheet Eligibility Work Queue", "Provider timesheet validation, incorrect recipient entry", PROVIDER, false);
        q("BLIND_VISUALLY", "Blind Visually Work Queue", "IHSS-specific eligibility — blind/visually impaired recipient contact issues", PROVIDER, false);

        // Internal Operations Queues
        q("TRAVEL_CLAIM", "Travel Claim Work Queue", "Travel claim processing and validation", INTERNAL_OPS, false);
        q("TRAVEL_CLAIM_ERRORS", "Travel Claim Errors Work Queue", "Travel claim TPF errors — auto-closes after 10 days", INTERNAL_OPS, false);
        q("VIEW_TRAVEL_CLAIM", "View Travel Claim Queue", "Travel claim cancellations", INTERNAL_OPS, false);
        q("SICK_LEAVE_ERRORS", "Sick Leave Errors Work Queue", "Sick leave claim TPF errors — auto-closes after 10 days", INTERNAL_OPS, false);
        q("VIEW_SICK_LEAVE", "View Sick Leave Claim Queue", "Sick leave claim processing", INTERNAL_OPS, false);
        q("IHSS_ESP", "IHSS ESP Queue", "IHSS Electronic Services Portal batch processing", INTERNAL_OPS, false);

        // Supervisor Queues
        q("SW_RESERVE_ASSIGNED", "SW Reserve Assigned Tasks Queue", "Escalated tasks that exceeded deadlines — supervisor review", SUPERVISOR, true);
        q("SW_USER_APPROVALS", "SW User Approvals Queue", "Pending case approval requests for supervisor", SUPERVISOR, true);
        q("SW_MY_USERS", "SW My Users Queue", "Team workload monitoring and management", SUPERVISOR, true);
        q("ESCALATED", "Escalated Tasks Queue", "Default escalation destination for overdue tasks", SUPERVISOR, true);

        log.info("Seeded {} work queues", queueRepo.count());
    }

    private void q(String name, String displayName, String desc, WorkQueue.QueueCategory cat, boolean supervisorOnly) {
        WorkQueue wq = new WorkQueue();
        wq.setName(name);
        wq.setDisplayName(displayName);
        wq.setDescription(desc);
        wq.setQueueCategory(cat);
        wq.setSupervisorOnly(supervisorOnly);
        wq.setSubscriptionAllowed(!supervisorOnly);
        wq.setActive(true);
        wq.setSensitivityLevel(1);
        queueRepo.save(wq);
    }

    private void seedTaskTypes() {
        if (taskTypeRepo.count() > 0) {
            log.info("Task types already seeded ({} exist), skipping", taskTypeRepo.count());
            return;
        }

        log.info("Seeding task types...");

        // --- Case Management Tasks ---
        tt("CI-111180", "Case Review Approval/Rejection", "Upon approval or rejection of a Case Review Authorization",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-111181", "Multiple New Cases Assigned", "When multiple new cases are assigned through bulk assignment",
                "CASE_OWNER", true, 0, false, null, "Case Management");

        tt("CI-111182", "Case Pending Approval 5 Days", "Case submitted for approval not acted on in 5 business days",
                "CASE_OWNER", false, 5, false, null, "Case Management");

        tt("CI-111183", "Treatment/Authorization TAR", "Upon receipt of a TAR for an eligible case",
                "CASE_WORKER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-111184", "Provider Unreviewed Affidavits", "Provider has multiple unreviewed affidavits in a year",
                "CASE_OWNER", true, 0, false, null, "Case Management");

        tt("CI-111192", "Recipient Death Notification (MEDS/CDPH)", "Death match from CDPH, MEDS, or SSA — DOD not populated",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-111193", "Recipient Death Notification (SCD)", "Death match from SCD source — DOD not populated",
                "QA", false, 5, true, "QA_SUPERVISOR", "Case Management");

        tt("CI-111194", "Death Confirmation", "Death confirmation required for matched record",
                "CASE_WORKER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-488670", "Companion Case Rescission", "Terminated case in companion collection gets rescinded",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-488671", "Companion Case Funding Source Update", "Funding source updated on any case in companion collection",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-489033", "Recipient Phone Number Update", "Timesheet review shows incorrect phone number",
                "CASE_OWNER", false, 1, false, null, "Case Management");

        tt("CI-770242", "Health Care Certification Due in 10 Days", "HC certification approaching expiry",
                "HEALTH_CARE_CERT", false, 10, true, "SW_RESERVE_ASSIGNED", "Case Management");

        tt("CI-823959", "Medi-Cal Discontinuance (WPCS)", "Medi-Cal discontinued for WPCS case",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        // --- Payroll/Payment Tasks ---
        taskTypePayment("CI-111174", "Special Transaction Request", "Special transaction submitted for approval",
                "PAYMENTS_PENDING_APPROVAL", 3, TaskType.EscalationCheckType.NOT_RESERVED, 3, null, "PAYROLL_SUPERVISOR");

        taskTypePayment("CI-111176", "Cross County Special Transaction", "Payment correction for cross-county case",
                "PAYMENTS_PENDING_APPROVAL", 2, TaskType.EscalationCheckType.BOTH, 2, 2, "PAYROLL_SUPERVISOR");

        taskTypePayment("CI-111177", "Payment Correction Request", "Payment correction submitted for approval",
                "PAYMENTS_PENDING_APPROVAL", 3, TaskType.EscalationCheckType.NOT_RESERVED, 3, null, "PAYROLL_SUPERVISOR");

        tt("CI-111178", "Special Transaction Rejection", "Payroll approver rejected special transaction",
                "SPECIAL_TRANSACTION_REQUESTER", false, 5, true, "SW_RESERVE_ASSIGNED", "Payroll");

        tt("CI-111179", "Payment Correction Rejection", "Payroll approver rejected payment correction",
                "SPECIAL_TRANSACTION_REQUESTER", false, 5, true, "SW_RESERVE_ASSIGNED", "Payroll");

        // Payroll notifications
        tt("CI-111166", "Special Transaction Approval", "Payroll approver approved special transaction",
                "SPECIAL_TRANSACTION_REQUESTER", true, 0, false, null, "Payroll");

        tt("CI-111167", "Cross County Payroll Activity", "Payroll action on case in different county",
                "CASE_WORKER", true, 0, false, null, "Payroll");

        tt("CI-111168", "Cross County Special Transaction Activity", "Special transaction on cross-county case",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        tt("CI-111169", "Cross County Payment Correction Activity", "Payment correction on cross-county case",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        tt("CI-111170", "Overpayment Recovery Activity", "Overpayment recovery action taken",
                "CASE_WORKER", true, 0, false, null, "Payroll");

        tt("CI-111171", "Cross County Overpayment Recovery", "Overpayment recovery on cross-county case",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        tt("CI-111172", "Cross County Pay Reconciliation", "Advance pay recon action by service month",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        tt("CI-111173", "Overpayment Collection Complete", "Collection fully satisfies overpayment balance",
                "PAYROLL_SUPERVISOR", true, 0, false, null, "Payroll");

        // --- QA / Program Management Tasks ---
        tt("CI-111190", "Paid Claim Match", "Monthly Medi-Cal service match file received",
                "QA", false, 5, true, "QA_SUPERVISOR", "QA");

        tt("CI-111191", "Paid Claim Match Escalation", "QA worker hasn't actioned paid claim match in 5 days",
                "QA_SUPERVISOR", false, 5, false, null, "QA");

        // --- Training / CDSS Tasks ---
        tt("CI-823404", "Training Time Claim Approval", "CDSS worker submitted training time claim",
                "CDSS_PAYMENTS_PENDING", false, 0, false, null, "Training");

        tt("CI-823427", "Career Path Training Time Claim", "Training time claim for career pathway",
                "CDSS_CAREER_PATHWAYS", false, 0, false, null, "Training");

        tt("CI-823433", "Rejected Training Time Claim", "Training time claim rejected, returned to submitter",
                "SUBMITTED_BY_WORKER", false, 5, true, "SW_RESERVE_ASSIGNED", "Training");

        // --- Provider Management Tasks ---
        tt("CI-313305", "Provider Overpayment Recovery Designation", "Provider/payee designated for overpayment recovery",
                "PAYROLL_SUPERVISOR", true, 0, false, null, "Provider");

        tt("CI-486349", "Incorrect Recipient Entry (3x)", "Same incorrect recipient entered 3 times on timesheet",
                "TIMESHEET_ELIGIBILITY", false, 5, true, "SW_RESERVE_ASSIGNED", "Provider");

        // --- Internal Operations Tasks (auto-close) ---
        taskTypeAutoClose("CI-822312", "Travel Claim Exception", "TPF error during travel claim processing",
                "TRAVEL_CLAIM_ERRORS", 10);

        taskTypeAutoClose("CI-822321", "Sick Leave Claim Exception", "TPF error during sick leave claim processing",
                "SICK_LEAVE_ERRORS", 10);

        tt("CI-873788", "Travel Claim Cancellation", "Travel claim cancelled",
                "TRAVEL_CLAIM", false, 5, true, "SW_RESERVE_ASSIGNED", "Internal Ops");

        tt("CI-873789", "Travel Claim Cancellation (View)", "Travel claim cancellation notification",
                "VIEW_TRAVEL_CLAIM", true, 0, false, null, "Internal Ops");

        tt("CI-832559", "IHSS ESP Batch Status", "Batch job 5005DMAN executed",
                "IHSS_ESP", true, 0, false, null, "Internal Ops");

        tt("CI-824056", "Electronic Form Submission", "Electronic form submitted",
                "FORM_COMPLETION", false, 5, true, "SW_RESERVE_ASSIGNED", "Internal Ops");

        tt("CI-824057", "Electronic Form Completion", "Electronic form requires completion",
                "FORM_COMPLETION", false, 5, true, "SW_RESERVE_ASSIGNED", "Internal Ops");

        log.info("Seeded {} task types", taskTypeRepo.count());
    }

    /** Standard task type */
    private void tt(String code, String name, String desc, String targetQueue,
                    boolean isNotification, int deadlineDays, boolean escalation,
                    String escalationTarget, String area) {
        TaskType t = new TaskType();
        t.setTaskTypeCode(code);
        t.setName(name);
        t.setDescription(desc);
        t.setTargetQueue(targetQueue);
        t.setNotification(isNotification);
        t.setDeadlineBusinessDays(deadlineDays);
        t.setEscalationEnabled(escalation);
        t.setEscalationTargetQueue(escalationTarget);
        t.setEscalationCheckType(TaskType.EscalationCheckType.NOT_RESERVED);
        t.setPriorityDefault(isNotification ? Task.TaskPriority.LOW : Task.TaskPriority.MEDIUM);
        t.setFunctionalArea(area);
        t.setActive(true);
        taskTypeRepo.save(t);
    }

    /** Payment task type with dual-stage escalation */
    private void taskTypePayment(String code, String name, String desc, String targetQueue,
                                  int deadlineDays, TaskType.EscalationCheckType checkType,
                                  Integer reserveDeadline, Integer completionDeadline, String escalationTarget) {
        TaskType t = new TaskType();
        t.setTaskTypeCode(code);
        t.setName(name);
        t.setDescription(desc);
        t.setTargetQueue(targetQueue);
        t.setNotification(false);
        t.setDeadlineBusinessDays(deadlineDays);
        t.setEscalationEnabled(true);
        t.setEscalationTargetQueue(escalationTarget);
        t.setEscalationCheckType(checkType);
        t.setReserveDeadlineDays(reserveDeadline);
        t.setCompletionDeadlineDays(completionDeadline);
        t.setPriorityDefault(Task.TaskPriority.HIGH);
        t.setFunctionalArea("Payroll");
        t.setActive(true);
        taskTypeRepo.save(t);
    }

    /** Auto-close task type (no escalation) */
    private void taskTypeAutoClose(String code, String name, String desc, String targetQueue, int autoCloseDays) {
        TaskType t = new TaskType();
        t.setTaskTypeCode(code);
        t.setName(name);
        t.setDescription(desc);
        t.setTargetQueue(targetQueue);
        t.setNotification(false);
        t.setDeadlineBusinessDays(autoCloseDays);
        t.setEscalationEnabled(false);
        t.setAutoCloseEnabled(true);
        t.setAutoCloseDays(autoCloseDays);
        t.setPriorityDefault(Task.TaskPriority.MEDIUM);
        t.setFunctionalArea("Internal Ops");
        t.setActive(true);
        taskTypeRepo.save(t);
    }
}
