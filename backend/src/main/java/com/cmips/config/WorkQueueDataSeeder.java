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
        updateClosureRequirements();
    }

    private void seedQueues() {
        long existingCount = queueRepo.count();
        log.info("Seeding work queues (existing: {})...", existingCount);

        // Case Management Queues
        qIfAbsent("CASE_OWNER", "Case Owner Queue", "Primary queue for case assignment and general case management tasks", CASE_MGMT, false);
        qIfAbsent("CASE_WORKER", "Case Worker Queue", "Frontline tasks — death confirmations, notifications, determinations", CASE_MGMT, false);
        qIfAbsent("CASE_HOME", "Case Home Queue", "Home visits, health care certifications, case-specific maintenance", CASE_MGMT, false);
        qIfAbsent("CASE_CLOSURE", "Case Closure Queue", "Companion case rescissions, funding source updates, termination workflows", CASE_MGMT, false);
        qIfAbsent("WPCS_CASE", "WPCS Case Queue", "Welfare-to-Work/Payroll Services — Medi-Cal discontinuance for WPCS cases", CASE_MGMT, false);
        qIfAbsent("HEALTH_CARE_CERT", "Health Care Certification Queue", "Health care certification due date monitoring", CASE_MGMT, false);
        qIfAbsent("FORM_COMPLETION", "Form Completion Queue", "Electronic form submissions processing", CASE_MGMT, false);
        qIfAbsent("ICT_COORDINATOR", "Inter-County Transfer Coordinator Queue", "ICT referral processing, hearing coordination, transfer cancellations", CASE_MGMT, false);
        qIfAbsent("COUNTY_CONTRACT_COORDINATOR", "County Contract Coordinator Queue", "County contractor billing invoices and contract management", CASE_MGMT, false);

        // QA Queues
        qIfAbsent("QA", "QA Queue", "Paid claim matching, death match processing (SCD source)", QA, false);
        qIfAbsent("QA_SUPERVISOR", "QA Supervisor Queue", "Escalated QA tasks — paid claim match not actioned within 5 days", QA, true);

        // Payroll Queues
        qIfAbsent("PAYMENTS_PENDING_APPROVAL", "Payments Pending Approval Queue", "Special transaction approvals, payment correction approvals", PAYROLL, false);
        qIfAbsent("PAYROLL_SUPERVISOR", "Payroll Supervisor Work Queue", "Overpayment recovery, escalated payment approvals", PAYROLL, true);
        qIfAbsent("SPECIAL_TRANSACTION_REQUESTER", "Special Transaction Requester Queue", "Rejected special transactions returned to requester for correction", PAYROLL, false);
        qIfAbsent("PAYMENT_SEARCH_BY_CASE", "Payment Search by Case Queue", "Cross-county advance pay reconciliation", PAYROLL, false);
        qIfAbsent("OVERPAYMENT_RECOVERY", "Overpayment Recovery Queue", "Overpayment recovery tracking, collection notifications", PAYROLL, false);

        // Training / CDSS Queues
        qIfAbsent("CDSS_PAYMENTS_PENDING", "CDSS Payments Pending Approval Queue", "Training time claim approvals (1st and 2nd level)", TRAINING, false);
        qIfAbsent("CDSS_CAREER_PATHWAYS", "CDSS Career Pathways Work Queue", "Career pathway training approvals", TRAINING, false);
        qIfAbsent("SUBMITTED_BY_WORKER", "Submitted By Worker Queue", "Rejected training time claims returned to submitter", TRAINING, false);

        // Provider Management Queues
        qIfAbsent("PROVIDER_MANAGEMENT", "Provider Management Queue", "Provider enrollment, modifications, overpayment recovery designation", PROVIDER, false);
        qIfAbsent("TIMESHEET_ELIGIBILITY", "Timesheet Eligibility Work Queue", "Provider timesheet validation, incorrect recipient entry", PROVIDER, false);
        qIfAbsent("BLIND_VISUALLY", "Blind Visually Work Queue", "IHSS-specific eligibility — blind/visually impaired recipient contact issues", PROVIDER, false);

        // Internal Operations Queues
        qIfAbsent("TRAVEL_CLAIM", "Travel Claim Work Queue", "Travel claim processing and validation", INTERNAL_OPS, false);
        qIfAbsent("TRAVEL_CLAIM_ERRORS", "Travel Claim Errors Work Queue", "Travel claim TPF errors — auto-closes after 10 days", INTERNAL_OPS, false);
        qIfAbsent("VIEW_TRAVEL_CLAIM", "View Travel Claim Queue", "Travel claim cancellations", INTERNAL_OPS, false);
        qIfAbsent("SICK_LEAVE_ERRORS", "Sick Leave Errors Work Queue", "Sick leave claim TPF errors — auto-closes after 10 days", INTERNAL_OPS, false);
        qIfAbsent("VIEW_SICK_LEAVE", "View Sick Leave Claim Queue", "Sick leave claim processing", INTERNAL_OPS, false);
        qIfAbsent("IHSS_ESP", "IHSS ESP Queue", "IHSS Electronic Services Portal batch processing", INTERNAL_OPS, false);

        // Supervisor Queues
        qIfAbsent("SW_RESERVE_ASSIGNED", "SW Reserve Assigned Tasks Queue", "Escalated tasks that exceeded deadlines — supervisor review", SUPERVISOR, true);
        qIfAbsent("SW_USER_APPROVALS", "SW User Approvals Queue", "Pending case approval requests for supervisor", SUPERVISOR, true);
        qIfAbsent("SW_MY_USERS", "SW My Users Queue", "Team workload monitoring and management", SUPERVISOR, true);
        qIfAbsent("ESCALATED", "Escalated Tasks Queue", "Default escalation destination for overdue tasks", SUPERVISOR, true);

        log.info("Seeded work queues (total: {})", queueRepo.count());
    }

    /** Idempotent queue seeder — only inserts if queue name does not exist */
    private void qIfAbsent(String name, String displayName, String desc, WorkQueue.QueueCategory cat, boolean supervisorOnly) {
        if (queueRepo.findByName(name).isPresent()) return;
        q(name, displayName, desc, cat, supervisorOnly);
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
        long existingCount = taskTypeRepo.count();
        log.info("Seeding task types (existing: {})...", existingCount);

        // --- Case Management Tasks ---
        ttIfAbsent("CI-111180", "Case Review Approval/Rejection", "Upon approval or rejection of a Case Review Authorization",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-111181", "Multiple New Cases Assigned", "When multiple new cases are assigned through bulk assignment",
                "CASE_OWNER", true, 0, false, null, "Case Management");

        ttIfAbsent("CI-111182", "Case Pending Approval 5 Days", "Case submitted for approval not acted on in 5 business days",
                "CASE_OWNER", false, 5, false, null, "Case Management");

        ttIfAbsent("CI-111183", "Treatment/Authorization TAR", "Upon receipt of a TAR for an eligible case",
                "CASE_WORKER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-111184", "Provider Unreviewed Affidavits", "Provider has multiple unreviewed affidavits in a year",
                "CASE_OWNER", true, 0, false, null, "Case Management");

        ttIfAbsent("CI-111192", "Recipient Death Notification (MEDS/CDPH)", "Death match from CDPH, MEDS, or SSA — DOD not populated",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-111193", "Recipient Death Notification (SCD)", "Death match from SCD source — DOD not populated",
                "QA", false, 5, true, "QA_SUPERVISOR", "Case Management");

        ttIfAbsent("CI-111194", "Death Confirmation", "Death confirmation required for matched record",
                "CASE_WORKER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-488670", "Companion Case Rescission", "Terminated case in companion collection gets rescinded",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-488671", "Companion Case Funding Source Update", "Funding source updated on any case in companion collection",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-489033", "Recipient Phone Number Update", "Timesheet review shows incorrect phone number",
                "CASE_OWNER", false, 1, false, null, "Case Management");

        ttIfAbsent("CI-770242", "Health Care Certification Due in 10 Days", "HC certification approaching expiry",
                "HEALTH_CARE_CERT", false, 10, true, "SW_RESERVE_ASSIGNED", "Case Management");

        ttIfAbsent("CI-823959", "Medi-Cal Discontinuance (WPCS)", "Medi-Cal discontinued for WPCS case",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Management");

        // --- Case Maintenance Tasks (DSD Section 30) ---
        // CM 01 - Recipient address changed to out of state
        ttIfAbsent("CM-001", "Recipient Address Out of State", "Recipient address changed to outside California — requires case review",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 02 - Medi-Cal share of cost change from MEDS
        ttIfAbsent("CM-002", "Medi-Cal Share of Cost Change", "MEDS interface reported share of cost change for recipient",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 05 - Recipient SSN changed (notification)
        ttIfAbsent("CM-005", "Recipient SSN Changed", "Recipient SSN has been updated — informational notification",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 06 - ICT referral received from sending county
        ttIfAbsent("CM-006", "ICT Referral from Sending County", "Inter-County Transfer referral received from sending county",
                "ICT_COORDINATOR", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 07 - Advance pay not reconciled after 45 days (notification)
        ttIfAbsent("CM-007", "Advance Pay Not Reconciled 45 Days", "Advance pay has not been reconciled after 45 days",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 08 - Advance pay not reconciled after 75 days (task)
        ttIfAbsent("CM-008", "Advance Pay Not Reconciled 75 Days", "Advance pay not reconciled after 75 days — action required",
                "CASE_OWNER", false, 2, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 09 - State hearing added for case with ICT (task + 2 notifications)
        ttIfAbsent("CM-009-T", "ICT State Hearing Added (Task)", "State hearing added for case with active ICT — ICT coordinator action required",
                "ICT_COORDINATOR", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        ttIfAbsent("CM-009-N1", "ICT State Hearing Added (Case Owner)", "State hearing added for case with active ICT — case owner notification",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance", Task.TaskPriority.HIGH);

        ttIfAbsent("CM-009-N2", "ICT State Hearing Added (Recv Worker)", "State hearing added for case with active ICT — receiving worker notification",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 10 - ICT requested with existing state hearing (notification)
        ttIfAbsent("CM-010", "ICT State Hearing Pending", "ICT requested for case that has a pending state hearing",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 11 - Application withdrawal (notification)
        ttIfAbsent("CM-011", "Application Withdrawal", "Application has been withdrawn by recipient",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 13 - Case has IP mode of service but no assigned providers (notification)
        ttIfAbsent("CM-013", "Case Has No Assigned Providers", "Case with IP mode of service has no assigned providers",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 14 - Recipient turns 18 next month
        ttIfAbsent("CM-014", "Recipient Turns 18", "Recipient turns 18 next month — reassessment may be needed",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 15 - Paramedical service authorization expiring (notification)
        ttIfAbsent("CM-015", "Paramedical Service Auth Expiring", "Paramedical service authorization expiring within 15 days",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 16 - Time limited service expired (notification)
        ttIfAbsent("CM-016", "Time Limited Service Expired", "A time-limited service has expired",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 17 - County contractor billing invoice
        ttIfAbsent("CM-017", "County Contractor Billing Invoice", "County contractor billing invoice requires processing",
                "COUNTY_CONTRACT_COORDINATOR", false, 0, false, null, "Case Maintenance");

        // CM 19 - Disability determination from Medi-Cal
        ttIfAbsent("CM-019", "Disability Determination from Medi-Cal", "MEDS reported disability determination — case review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 20 - Recipient child turned 14
        ttIfAbsent("CM-020", "Recipient Child Turned 14", "Recipient's child turned 14 — reassessment for extraordinary services",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 23 - ICT cancelled (task + 2 notifications)
        ttIfAbsent("CM-023-T", "ICT Cancelled (Recv Worker Task)", "ICT cancelled — receiving worker task to complete remaining actions",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        ttIfAbsent("CM-023-N1", "ICT Cancelled (Case Owner Notif)", "ICT cancelled — case owner notification",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        ttIfAbsent("CM-023-N2", "ICT Cancelled (ICT Coordinator Notif)", "ICT cancelled — ICT coordinator notification",
                "ICT_COORDINATOR", true, 0, false, null, "Case Maintenance");

        // CM 25 - ICT case assigned to new worker (notification)
        ttIfAbsent("CM-025", "ICT Case Assigned", "Inter-county transfer case assigned to new case worker",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 26 - Pending protective supervision 30 days (notification)
        ttIfAbsent("CM-026", "Pending Protective Supervision 30 Days", "Protective supervision pending for 30 days without resolution",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 27 - Pending paramedical authorization 30 days (notification)
        ttIfAbsent("CM-027", "Pending Paramedical Auth 30 Days", "Paramedical authorization pending for 30 days without resolution",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 29 - No initial Medi-Cal eligibility within 90 days
        ttIfAbsent("CM-029", "No Initial Medi-Cal Eligibility 90 Days", "No initial Medi-Cal eligibility received within 90 days of case creation",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 30 - Initial Medi-Cal eligibility received (notification)
        ttIfAbsent("CM-030", "Initial Medi-Cal Eligibility Received", "Initial Medi-Cal eligibility received for case",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 32 - SAWS discontinuance
        ttIfAbsent("CM-032", "SAWS Discontinuance", "SAWS reported Medi-Cal discontinuance — case review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 33 - SAWS rescission of discontinuance (notification)
        ttIfAbsent("CM-033", "SAWS Rescission of Discontinuance", "SAWS rescinded previous Medi-Cal discontinuance",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 34 - Medi-Cal eligibility termination (2 notifications)
        ttIfAbsent("CM-034-N1", "Medi-Cal Eligibility Termination (Owner)", "Medi-Cal eligibility terminated — case owner notification",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        ttIfAbsent("CM-034-N2", "Medi-Cal Eligibility Termination (Supv)", "Medi-Cal eligibility terminated — supervisor notification",
                "SW_RESERVE_ASSIGNED", true, 0, false, null, "Case Maintenance");

        // CM 35 - State hearing compliance form due
        ttIfAbsent("CM-035", "State Hearing Compliance Form Due", "State hearing granted — compliance form due",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 37 - WPCS hours terminated
        ttIfAbsent("CM-037", "WPCS Hours Terminated", "WPCS hours terminated — case worker action required",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 38 - IHSS recipient on leave, verify WPCS
        ttIfAbsent("CM-038", "IHSS Recipient on Leave - Verify WPCS", "IHSS recipient placed on leave — verify WPCS hours status",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 39 - Recipient address changed by non-case owner (notification)
        ttIfAbsent("CM-039", "Recipient Address Change by Non-Owner", "Recipient address changed by someone other than case owner",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 40 - WPCS recipient back from leave
        ttIfAbsent("CM-040", "WPCS Recipient Back from Leave", "WPCS recipient returned from leave — verify WPCS hours status",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 41-44 - SAWS demographic updates (notifications)
        ttIfAbsent("CM-041", "SAWS SSN Update", "SAWS reported SSN update for recipient",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        ttIfAbsent("CM-042", "SAWS DOB Update", "SAWS reported date of birth update for recipient",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        ttIfAbsent("CM-043", "SAWS Name Update", "SAWS reported name update for recipient",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        ttIfAbsent("CM-044", "SAWS Gender Update", "SAWS reported gender update for recipient",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 46 - Recipient SSN/ITIN pending 120 days
        ttIfAbsent("CM-046", "Recipient SSN/ITIN Pending 120 Days", "Recipient SSN/ITIN has been pending for 120 days",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 49 - ICT authorization complete, assign WPCS provider
        ttIfAbsent("CM-049", "ICT Auth Complete - Assign WPCS Provider", "ICT authorization complete — WPCS provider assignment needed",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 50 - ICT created with WPCS hours
        ttIfAbsent("CM-050", "ICT Created with WPCS Hours", "ICT created for case with active WPCS hours",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 51 - ICT cancelled with WPCS hours
        ttIfAbsent("CM-051", "ICT Cancelled with WPCS Hours", "ICT cancelled for case with active WPCS hours — review needed",
                "WPCS_CASE", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 52 - ICT complete, review WPCS hours (notification)
        ttIfAbsent("CM-052", "ICT Complete - Review WPCS Hours", "ICT completed — review WPCS hours in receiving county",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 53 - Health care cert due in 1 business day
        ttIfAbsent("CM-053", "Health Care Cert Due 1 Business Day", "Health care certification due in 1 business day — urgent action required",
                "CASE_OWNER", false, 1, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 54-55 - CIN re-clearance needed
        ttIfAbsent("CM-054", "CIN Re-Clearance Needed (Name Change)", "Recipient name changed — CIN re-clearance required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        ttIfAbsent("CM-055", "CIN Re-Clearance Needed (DOB/Gender)", "Recipient DOB or gender changed — CIN re-clearance required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 56-59 - SCI daily updates
        ttIfAbsent("CM-056", "SCI Daily Update - Name", "SCI daily file reported name update — case review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        ttIfAbsent("CM-057", "SCI Daily Update - DOB", "SCI daily file reported DOB update — case review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        ttIfAbsent("CM-058", "SCI Daily Update - Gender", "SCI daily file reported gender update — case review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        ttIfAbsent("CM-059", "SCI Daily Update - SSN", "SCI daily file reported SSN update — case review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 60 - Incorrect recipient authorization number entered 3 times
        ttIfAbsent("CM-060", "Incorrect Recipient Auth Number 3x", "Incorrect recipient authorization number entered 3 times via phone",
                "BLIND_VISUALLY", false, 1, false, null, "Case Maintenance");

        // CM 61 - Case rescission with WPCS hours
        ttIfAbsent("CM-061", "Case Rescission - WPCS Hours", "Case rescinded that had active WPCS hours",
                "WPCS_CASE", false, 0, false, null, "Case Maintenance");

        // CM 62 - SAWS discontinuance for WPCS case
        ttIfAbsent("CM-062", "SAWS Discontinuance - WPCS Case", "SAWS discontinuance received for case with WPCS hours",
                "WPCS_CASE", false, 0, false, null, "Case Maintenance");

        // CM 63 - Medi-Cal termination with WPCS hours
        ttIfAbsent("CM-063", "Medi-Cal Termination - WPCS Hours", "Medi-Cal eligibility terminated for case with WPCS hours",
                "WPCS_CASE", false, 0, false, null, "Case Maintenance");

        // CM 70 - UHV non-compliance
        ttIfAbsent("CM-070", "UHV Non-Compliance", "Unannounced home visit non-compliance — supervisor review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 72 - IHSS ESP inactivation
        ttIfAbsent("CM-072", "IHSS ESP Inactivation", "IHSS ESP account inactivated — payroll supervisor review",
                "PAYROLL_SUPERVISOR", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 73 - SAWS case terminated (notification)
        ttIfAbsent("CM-073", "SAWS Case Terminated", "SAWS reported case termination",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 74 - SAWS discontinuance rescinded (notification)
        ttIfAbsent("CM-074", "SAWS Discontinuance Rescinded", "SAWS rescinded previous discontinuance for case",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 75 - Non-compliance 90 days, auto-rescind disabled (notification)
        ttIfAbsent("CM-075", "Non-Compliance 90 Days Rescind Disabled", "Terminated case non-compliant for 90 days but auto-rescind is disabled",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 76 - SAWS SSN matches another person
        ttIfAbsent("CM-076", "SAWS SSN Matches Another Person", "SAWS reported SSN matches an existing person record — investigation required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 77 - SAWS CIN matches another person
        ttIfAbsent("CM-077", "SAWS CIN Matches Another Person", "SAWS reported CIN matches an existing person record — investigation required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance", Task.TaskPriority.HIGH);

        // CM 78 - Case rescission notification
        ttIfAbsent("CM-078", "Rescind Case Notification", "Case has been rescinded — supervisor notification",
                "CASE_OWNER", true, 0, false, null, "Case Maintenance");

        // CM 79 - Recipient address matches provider address
        ttIfAbsent("CM-079", "Recipient Address Matches Provider", "Recipient address matches an assigned provider address — review required",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 80 - Electronic form from CMIPS Services Portal
        ttIfAbsent("CM-080", "Electronic Form from CSP", "Electronic form submitted via CMIPS Services Portal",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // CM 81 - Electronic form from ESP
        ttIfAbsent("CM-081", "Electronic Form from ESP", "Electronic form submitted via Electronic Services Portal",
                "CASE_OWNER", false, 5, true, "SW_RESERVE_ASSIGNED", "Case Maintenance");

        // --- Payroll/Payment Tasks ---
        ttIfAbsentPayment("CI-111174", "Special Transaction Request", "Special transaction submitted for approval",
                "PAYMENTS_PENDING_APPROVAL", 3, TaskType.EscalationCheckType.NOT_RESERVED, 3, null, "PAYROLL_SUPERVISOR");

        ttIfAbsentPayment("CI-111176", "Cross County Special Transaction", "Payment correction for cross-county case",
                "PAYMENTS_PENDING_APPROVAL", 2, TaskType.EscalationCheckType.BOTH, 2, 2, "PAYROLL_SUPERVISOR");

        ttIfAbsentPayment("CI-111177", "Payment Correction Request", "Payment correction submitted for approval",
                "PAYMENTS_PENDING_APPROVAL", 3, TaskType.EscalationCheckType.NOT_RESERVED, 3, null, "PAYROLL_SUPERVISOR");

        ttIfAbsent("CI-111178", "Special Transaction Rejection", "Payroll approver rejected special transaction",
                "SPECIAL_TRANSACTION_REQUESTER", false, 5, true, "SW_RESERVE_ASSIGNED", "Payroll");

        ttIfAbsent("CI-111179", "Payment Correction Rejection", "Payroll approver rejected payment correction",
                "SPECIAL_TRANSACTION_REQUESTER", false, 5, true, "SW_RESERVE_ASSIGNED", "Payroll");

        // Payroll notifications
        ttIfAbsent("CI-111166", "Special Transaction Approval", "Payroll approver approved special transaction",
                "SPECIAL_TRANSACTION_REQUESTER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111167", "Cross County Payroll Activity", "Payroll action on case in different county",
                "CASE_WORKER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111168", "Cross County Special Transaction Activity", "Special transaction on cross-county case",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111169", "Cross County Payment Correction Activity", "Payment correction on cross-county case",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111170", "Overpayment Recovery Activity", "Overpayment recovery action taken",
                "CASE_WORKER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111171", "Cross County Overpayment Recovery", "Overpayment recovery on cross-county case",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111172", "Cross County Pay Reconciliation", "Advance pay recon action by service month",
                "CASE_OWNER", true, 0, false, null, "Payroll");

        ttIfAbsent("CI-111173", "Overpayment Collection Complete", "Collection fully satisfies overpayment balance",
                "PAYROLL_SUPERVISOR", true, 0, false, null, "Payroll");

        // --- QA / Program Management Tasks ---
        ttIfAbsent("CI-111190", "Paid Claim Match", "Monthly Medi-Cal service match file received",
                "QA", false, 5, true, "QA_SUPERVISOR", "QA");

        ttIfAbsent("CI-111191", "Paid Claim Match Escalation", "QA worker hasn't actioned paid claim match in 5 days",
                "QA_SUPERVISOR", false, 5, false, null, "QA");

        // --- Training / CDSS Tasks ---
        ttIfAbsent("CI-823404", "Training Time Claim Approval", "CDSS worker submitted training time claim",
                "CDSS_PAYMENTS_PENDING", false, 0, false, null, "Training");

        ttIfAbsent("CI-823427", "Career Path Training Time Claim", "Training time claim for career pathway",
                "CDSS_CAREER_PATHWAYS", false, 0, false, null, "Training");

        ttIfAbsent("CI-823433", "Rejected Training Time Claim", "Training time claim rejected, returned to submitter",
                "SUBMITTED_BY_WORKER", false, 5, true, "SW_RESERVE_ASSIGNED", "Training");

        // --- Provider Management Tasks ---
        ttIfAbsent("CI-313305", "Provider Overpayment Recovery Designation", "Provider/payee designated for overpayment recovery",
                "PAYROLL_SUPERVISOR", true, 0, false, null, "Provider");

        ttIfAbsent("CI-486349", "Incorrect Recipient Entry (3x)", "Same incorrect recipient entered 3 times on timesheet",
                "TIMESHEET_ELIGIBILITY", false, 5, true, "SW_RESERVE_ASSIGNED", "Provider");

        // --- Internal Operations Tasks (auto-close) ---
        ttIfAbsentAutoClose("CI-822312", "Travel Claim Exception", "TPF error during travel claim processing",
                "TRAVEL_CLAIM_ERRORS", 10);

        ttIfAbsentAutoClose("CI-822321", "Sick Leave Claim Exception", "TPF error during sick leave claim processing",
                "SICK_LEAVE_ERRORS", 10);

        ttIfAbsent("CI-873788", "Travel Claim Cancellation", "Travel claim cancelled",
                "TRAVEL_CLAIM", false, 5, true, "SW_RESERVE_ASSIGNED", "Internal Ops");

        ttIfAbsent("CI-873789", "Travel Claim Cancellation (View)", "Travel claim cancellation notification",
                "VIEW_TRAVEL_CLAIM", true, 0, false, null, "Internal Ops");

        ttIfAbsent("CI-832559", "IHSS ESP Batch Status", "Batch job 5005DMAN executed",
                "IHSS_ESP", true, 0, false, null, "Internal Ops");

        ttIfAbsent("CI-824056", "Electronic Form Submission", "Electronic form submitted",
                "FORM_COMPLETION", false, 5, true, "SW_RESERVE_ASSIGNED", "Internal Ops");

        ttIfAbsent("CI-824057", "Electronic Form Completion", "Electronic form requires completion",
                "FORM_COMPLETION", false, 5, true, "SW_RESERVE_ASSIGNED", "Internal Ops");

        log.info("Seeded task types (total: {})", taskTypeRepo.count());
    }

    /**
     * DSD GAP 2 + GAP 3: Update existing task types with requiredActionForClosure and autoCloseEvent.
     * Runs on every startup to ensure these fields are populated even on existing deployments.
     */
    private void updateClosureRequirements() {
        log.info("Updating closure requirements and auto-close events on task types...");
        int updated = 0;

        // CM 01 — Case is Terminated, close with comment
        updated += setClosureReq("CM-001", "Case is Terminated, close with comment", "case.terminated");
        // CM 02 — Create Change Assessment and update Income Evidence
        updated += setClosureReq("CM-002", "Create Change Assessment and update Income Evidence", "change_assessment.created");
        // CM 006 — Complete ICT processing
        updated += setClosureReq("CM-006", "Complete ICT processing", "ict.completed");
        // CM 008 — Create Change Assessment
        updated += setClosureReq("CM-008", "Create Change Assessment", "change_assessment.created");
        // CM 009-T — Resolve state hearing with ICT
        updated += setClosureReq("CM-009-T", "Resolve state hearing with ICT coordinator", null);
        // CM 013 — Assign provider to case (auto-close when provider assigned)
        updated += setClosureReq("CM-013", null, "provider.assigned");
        // CM 014 — Complete age 18 reassessment
        updated += setClosureReq("CM-014", "Complete reassessment for recipient turning 18", null);
        // CM 015 — Renew paramedical authorization
        updated += setClosureReq("CM-015", null, "paramedical_auth.approved");
        // CM 017 — Process county contractor billing invoice
        updated += setClosureReq("CM-017", "Process billing invoice", null);
        // CM 019 — Update disability determination on case
        updated += setClosureReq("CM-019", "Update disability determination on case", null);
        // CM 020 — Complete child age 14 review
        updated += setClosureReq("CM-020", "Complete child age 14 protective supervision review", null);
        // CM 023-T — Complete remaining ICT actions after cancellation
        updated += setClosureReq("CM-023-T", "Complete remaining ICT actions after cancellation", null);
        // CM 026 — Approve or deny protective supervision (auto-close)
        updated += setClosureReq("CM-026", null, "protective_supervision.approved");
        // CM 027 — Approve or deny paramedical auth (auto-close)
        updated += setClosureReq("CM-027", null, "paramedical_auth.approved");
        // CM 029 — Verify Medi-Cal eligibility or terminate case (auto-close when received)
        updated += setClosureReq("CM-029", "Verify Medi-Cal eligibility or initiate termination", "medi_cal.eligibility_received");
        // CM 032 — Update Discontinuance NOA
        updated += setClosureReq("CM-032", "Update Discontinuance NOA", "discontinuance_noa.updated");
        // CM 035 — Submit compliance form
        updated += setClosureReq("CM-035", "Submit state hearing compliance form", null);
        // CM 037 — Review and terminate WPCS hours
        updated += setClosureReq("CM-037", "Review and terminate WPCS hours", null);
        // CM 038 — Verify WPCS hours while on leave
        updated += setClosureReq("CM-038", "Verify WPCS hours during leave period", null);
        // CM 040 — Verify WPCS hours after return from leave
        updated += setClosureReq("CM-040", "Verify WPCS hours after return from leave", null);
        // CM 046 — Obtain SSN/ITIN (auto-close when provided)
        updated += setClosureReq("CM-046", "Obtain SSN or ITIN for recipient", "ssn_itin.provided");
        // CM 049 — Assign WPCS provider after ICT auth
        updated += setClosureReq("CM-049", "Assign WPCS provider", null);
        // CM 050 — Review WPCS hours for ICT case
        updated += setClosureReq("CM-050", "Review WPCS hours for ICT case", null);
        // CM 051 — Review WPCS hours after ICT cancellation
        updated += setClosureReq("CM-051", "Review WPCS hours after ICT cancellation", null);
        // CM 053 — Submit health care certification (auto-close)
        updated += setClosureReq("CM-053", "Submit health care certification", "health_care_cert.submitted");
        // CM 054 — Complete CIN re-clearance (auto-close)
        updated += setClosureReq("CM-054", "Complete CIN re-clearance after name change", "cin_clearance.completed");
        // CM 055 — Complete CIN re-clearance (auto-close)
        updated += setClosureReq("CM-055", "Complete CIN re-clearance after DOB/gender change", "cin_clearance.completed");
        // CM 056-059 — SCI daily updates: verify and update
        updated += setClosureReq("CM-056", "Verify and update recipient name from SCI", null);
        updated += setClosureReq("CM-057", "Verify and update recipient DOB from SCI", null);
        updated += setClosureReq("CM-058", "Verify and update recipient gender from SCI", null);
        updated += setClosureReq("CM-059", "Verify and update recipient SSN from SCI", null);
        // CM 060 — Correct recipient authorization number
        updated += setClosureReq("CM-060", "Correct recipient authorization number", null);
        // CM 061 — Terminate WPCS hours after case rescission
        updated += setClosureReq("CM-061", "Terminate WPCS hours after case rescission", null);
        // CM 062 — Terminate WPCS hours after SAWS discontinuance
        updated += setClosureReq("CM-062", "Terminate WPCS hours after SAWS discontinuance", null);
        // CM 063 — Terminate WPCS hours after Medi-Cal termination
        updated += setClosureReq("CM-063", "Terminate WPCS hours after Medi-Cal termination", null);
        // CM 070 — Complete UHV follow-up
        updated += setClosureReq("CM-070", "Complete UHV non-compliance follow-up", null);
        // CM 072 — Process IHSS ESP inactivation
        updated += setClosureReq("CM-072", "Process IHSS ESP inactivation", null);
        // CM 076 — Investigate SSN match
        updated += setClosureReq("CM-076", "Investigate SAWS SSN match with another person", null);
        // CM 077 — Investigate CIN match
        updated += setClosureReq("CM-077", "Investigate SAWS CIN match with another person", null);
        // CM 079 — Investigate address match
        updated += setClosureReq("CM-079", "Investigate recipient/provider address match", null);
        // CM 080/081 — Process electronic form
        updated += setClosureReq("CM-080", "Process electronic form from CSP", null);
        updated += setClosureReq("CM-081", "Process electronic form from ESP", null);

        // CI-770242 — Health care cert due in 10 days (auto-close when submitted)
        updated += setClosureReq("CI-770242", "Submit health care certification", "health_care_cert.submitted");
        // CI-488670 — Companion case rescission review
        updated += setClosureReq("CI-488670", "Review companion case rescission", null);
        // CI-488671 — Funding source update review
        updated += setClosureReq("CI-488671", "Review funding source update in companion collection", null);

        log.info("Updated {} task types with closure requirements/auto-close events", updated);
    }

    /**
     * Helper: set requiredActionForClosure and/or autoCloseEvent on a TaskType (if it exists).
     * Returns 1 if updated, 0 if task type not found.
     */
    private int setClosureReq(String code, String closureAction, String autoCloseEvent) {
        return taskTypeRepo.findByTaskTypeCode(code).map(tt -> {
            boolean changed = false;
            if (closureAction != null && (tt.getRequiredActionForClosure() == null || !closureAction.equals(tt.getRequiredActionForClosure()))) {
                tt.setRequiredActionForClosure(closureAction);
                changed = true;
            }
            if (autoCloseEvent != null && (tt.getAutoCloseEvent() == null || !autoCloseEvent.equals(tt.getAutoCloseEvent()))) {
                tt.setAutoCloseEvent(autoCloseEvent);
                changed = true;
            }
            if (changed) {
                taskTypeRepo.save(tt);
                return 1;
            }
            return 0;
        }).orElse(0);
    }

    // --- Idempotent helpers ---

    /** Idempotent standard task type (MEDIUM priority default) */
    private void ttIfAbsent(String code, String name, String desc, String targetQueue,
                            boolean isNotification, int deadlineDays, boolean escalation,
                            String escalationTarget, String area) {
        ttIfAbsent(code, name, desc, targetQueue, isNotification, deadlineDays, escalation, escalationTarget, area,
                isNotification ? Task.TaskPriority.LOW : Task.TaskPriority.MEDIUM);
    }

    /** Idempotent standard task type with explicit priority */
    private void ttIfAbsent(String code, String name, String desc, String targetQueue,
                            boolean isNotification, int deadlineDays, boolean escalation,
                            String escalationTarget, String area, Task.TaskPriority priority) {
        if (taskTypeRepo.existsByTaskTypeCode(code)) return;
        tt(code, name, desc, targetQueue, isNotification, deadlineDays, escalation, escalationTarget, area, priority);
    }

    /** Idempotent payment task type */
    private void ttIfAbsentPayment(String code, String name, String desc, String targetQueue,
                                    int deadlineDays, TaskType.EscalationCheckType checkType,
                                    Integer reserveDeadline, Integer completionDeadline, String escalationTarget) {
        if (taskTypeRepo.existsByTaskTypeCode(code)) return;
        taskTypePayment(code, name, desc, targetQueue, deadlineDays, checkType, reserveDeadline, completionDeadline, escalationTarget);
    }

    /** Idempotent auto-close task type */
    private void ttIfAbsentAutoClose(String code, String name, String desc, String targetQueue, int autoCloseDays) {
        if (taskTypeRepo.existsByTaskTypeCode(code)) return;
        taskTypeAutoClose(code, name, desc, targetQueue, autoCloseDays);
    }

    // --- Core seed methods ---

    /** Standard task type */
    private void tt(String code, String name, String desc, String targetQueue,
                    boolean isNotification, int deadlineDays, boolean escalation,
                    String escalationTarget, String area, Task.TaskPriority priority) {
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
        t.setPriorityDefault(priority);
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
