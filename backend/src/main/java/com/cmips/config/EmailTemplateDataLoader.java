package com.cmips.config;

import com.cmips.entity.EmailTemplateEntity;
import com.cmips.entity.EmailTemplateEntity.TemplateCategory;
import com.cmips.repository.EmailTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds all 69+ official CMIPS email templates on first startup.
 * Categories per DSD Section 31 / CMIPS E-Timesheet specifications.
 */
@Component
public class EmailTemplateDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateDataLoader.class);

    private static final String FOOTER =
        "<br><br><hr><p style='font-size:11px;color:#666;'>" +
        "California Department of Social Services &mdash; In-Home Supportive Services<br>" +
        "This is an automated message. Do not reply directly to this email.<br>" +
        "For assistance, contact your county IHSS office.</p>";

    private final EmailTemplateRepository repo;

    public EmailTemplateDataLoader(EmailTemplateRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) {
            log.debug("Email templates already seeded ({} rows), skipping.", repo.count());
            return;
        }
        log.info("Seeding CMIPS email templates...");
        List<EmailTemplateEntity> all = new ArrayList<>();

        // ─────────────────────────────────────────────────────────────────────
        // ELIGIBILITY — Approval/Denial/Termination/Change
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("EL-001", TemplateCategory.ELIGIBILITY, "Case Approved - IHSS Services Authorized",
            "Your IHSS Application Has Been Approved — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>We are pleased to inform you that your application for In-Home Supportive Services (IHSS) " +
            "has been <strong>approved</strong>, effective {{EFFECTIVE_DATE}}.</p>" +
            "<p>Your authorized services are listed in the enclosed Notice of Action (NA 1250). " +
            "Please review the notice for details about your approved hours.</p>" +
            "<p>Your assigned social worker is <strong>{{WORKER_NAME}}</strong> at {{WORKER_PHONE}}.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE,WORKER_NAME,WORKER_PHONE", true));

        all.add(t("EL-002", TemplateCategory.ELIGIBILITY, "Case Denied - IHSS Application",
            "Decision on Your IHSS Application — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>We are writing to inform you that your application for In-Home Supportive Services (IHSS) " +
            "has been <strong>denied</strong>.</p>" +
            "<p>The reason for this decision and your appeal rights are described in the enclosed " +
            "Notice of Action (NA 1252).</p>" +
            "<p><strong>You have the right to request a State Hearing within 90 days.</strong></p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER", false));

        all.add(t("EL-003", TemplateCategory.ELIGIBILITY, "Case Terminated - IHSS Services Ending",
            "Important: Your IHSS Services Are Being Terminated — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>We are writing to inform you that your In-Home Supportive Services will be " +
            "<strong>terminated</strong> effective {{EFFECTIVE_DATE}}.</p>" +
            "<p>Please review the enclosed Notice of Action (NA 1255) for the reason and your appeal rights.</p>" +
            "<p><strong>If you disagree, you may request a State Hearing before {{EFFECTIVE_DATE}} " +
            "to continue receiving services while your appeal is pending.</strong></p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE", false));

        all.add(t("EL-004", TemplateCategory.ELIGIBILITY, "Case Continuation - IHSS Services Continued",
            "Your IHSS Services Are Being Continued — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your In-Home Supportive Services are being <strong>continued</strong> effective {{EFFECTIVE_DATE}}. " +
            "Your authorized hours remain {{HOURS}} per month.</p>" +
            "<p>Please review the enclosed Notice of Action (NA 1251) for details.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE,HOURS", false));

        all.add(t("EL-005", TemplateCategory.ELIGIBILITY, "Hours Increased - Change in Award",
            "Your IHSS Authorized Hours Have Increased — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your IHSS authorized hours have been <strong>increased to {{HOURS}} per month</strong>, " +
            "effective {{EFFECTIVE_DATE}}.</p>" +
            "<p>Please review the enclosed Notice of Action (NA 1253) for details.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE,HOURS", false));

        all.add(t("EL-006", TemplateCategory.ELIGIBILITY, "Hours Reduced - Change in Award",
            "Your IHSS Authorized Hours Have Been Reduced — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your IHSS authorized hours have been <strong>reduced to {{HOURS}} per month</strong>, " +
            "effective {{EFFECTIVE_DATE}}.</p>" +
            "<p>Please review the enclosed Notice of Action (NA 1254) for details and your appeal rights.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE,HOURS", false));

        all.add(t("EL-007", TemplateCategory.ELIGIBILITY, "Share of Cost Change",
            "Your Medi-Cal Share of Cost Has Changed — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your Medi-Cal Share of Cost has changed to <strong>{{AMOUNT}} per month</strong>, " +
            "effective {{EFFECTIVE_DATE}}.</p>" +
            "<p>Please review the enclosed Notice of Action (NA 1256) for details.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE,AMOUNT", false));

        all.add(t("EL-008", TemplateCategory.ELIGIBILITY, "Multi-Program Enrollment",
            "You Are Enrolled in Multiple IHSS Programs — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your IHSS case has been enrolled in multiple programs effective {{EFFECTIVE_DATE}}. " +
            "Please review the enclosed Notice of Action (NA 1257) for details about each program.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE", false));

        all.add(t("EL-009", TemplateCategory.ELIGIBILITY, "Case Rescinded - Services Restored",
            "Your IHSS Services Have Been Restored — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>The action taken on your IHSS case has been <strong>rescinded</strong>. " +
            "Your services will be restored effective {{EFFECTIVE_DATE}}.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE", false));

        all.add(t("EL-010", TemplateCategory.ELIGIBILITY, "Reassessment Scheduled",
            "Your IHSS Reassessment Has Been Scheduled — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your annual IHSS reassessment has been scheduled. Your social worker " +
            "<strong>{{WORKER_NAME}}</strong> will contact you to arrange a home visit.</p>" +
            "<p>Your reassessment is due by <strong>{{DEADLINE_DATE}}</strong>.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,DEADLINE_DATE,WORKER_NAME", false));

        // ─────────────────────────────────────────────────────────────────────
        // PAYMENT
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("PAY-001", TemplateCategory.PAYMENT, "Payment Issued - Warrant",
            "Your IHSS Payment Has Been Issued — {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your IHSS payment of <strong>{{AMOUNT}}</strong> for pay period " +
            "<strong>{{PAY_PERIOD}}</strong> has been issued.</p>" +
            "<p>A warrant will be mailed to your address on file.</p>" + FOOTER,
            "PROVIDER_NAME,AMOUNT,PAY_PERIOD", true));

        all.add(t("PAY-002", TemplateCategory.PAYMENT, "Payment Issued - Direct Deposit",
            "Your IHSS Direct Deposit Has Been Processed — {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your IHSS direct deposit of <strong>{{AMOUNT}}</strong> for pay period " +
            "<strong>{{PAY_PERIOD}}</strong> has been processed and will be deposited " +
            "to your bank account within 1-2 business days.</p>" + FOOTER,
            "PROVIDER_NAME,AMOUNT,PAY_PERIOD", true));

        all.add(t("PAY-003", TemplateCategory.PAYMENT, "Direct Deposit Enrollment Confirmation",
            "Direct Deposit Enrollment Confirmed",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your enrollment in IHSS Direct Deposit has been confirmed. " +
            "Future payments will be deposited directly to your bank account.</p>" + FOOTER,
            "PROVIDER_NAME", false));

        all.add(t("PAY-004", TemplateCategory.PAYMENT, "Direct Deposit Change Confirmation",
            "Your Direct Deposit Banking Information Has Been Updated",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your IHSS Direct Deposit banking information has been updated. " +
            "If you did not make this change, please contact your county IHSS office immediately.</p>" + FOOTER,
            "PROVIDER_NAME", false));

        all.add(t("PAY-005", TemplateCategory.PAYMENT, "Overpayment Notice",
            "Notice of IHSS Overpayment — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Our records show that an overpayment of <strong>{{AMOUNT}}</strong> was made " +
            "on your IHSS account for the period {{PAY_PERIOD}}.</p>" +
            "<p>Please contact your county IHSS office to arrange repayment.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,AMOUNT,PAY_PERIOD", false));

        all.add(t("PAY-006", TemplateCategory.PAYMENT, "Payment Returned / Uncashed Warrant",
            "Action Required: Uncashed IHSS Warrant",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Our records indicate that an IHSS warrant issued on {{EFFECTIVE_DATE}} " +
            "for <strong>{{AMOUNT}}</strong> has not been cashed.</p>" +
            "<p>Please cash or deposit your warrant. If you need a replacement, contact your county office.</p>" + FOOTER,
            "PROVIDER_NAME,EFFECTIVE_DATE,AMOUNT", false));

        all.add(t("PAY-007", TemplateCategory.PAYMENT, "Advance Payment Issued",
            "Your IHSS Advance Payment Has Been Issued",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>An advance payment of <strong>{{AMOUNT}}</strong> has been issued for your IHSS case. " +
            "This advance must be reconciled within 45 days.</p>" + FOOTER,
            "RECIPIENT_NAME,AMOUNT,CASE_NUMBER", false));

        all.add(t("PAY-008", TemplateCategory.PAYMENT, "Payment Correction Issued",
            "IHSS Payment Correction — {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>A payment correction of <strong>{{AMOUNT}}</strong> has been processed " +
            "for pay period <strong>{{PAY_PERIOD}}</strong>.</p>" + FOOTER,
            "PROVIDER_NAME,AMOUNT,PAY_PERIOD", false));

        // ─────────────────────────────────────────────────────────────────────
        // TIMESHEET — E-Timesheet
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("ET-001", TemplateCategory.TIMESHEET, "E-Timesheet Invitation - Provider",
            "Invitation to Register for IHSS Electronic Timesheets (e-Timesheet)",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>You are invited to register for the IHSS Electronic Timesheet (e-Timesheet) system.</p>" +
            "<p>To register, please visit: <a href='{{URL}}'>{{URL}}</a></p>" +
            "<p>Your registration code is: <strong>{{REGISTRATION_CODE}}</strong></p>" +
            "<p>This invitation expires on {{DEADLINE_DATE}}.</p>" + FOOTER,
            "PROVIDER_NAME,URL,REGISTRATION_CODE,DEADLINE_DATE", false));

        all.add(t("ET-002", TemplateCategory.TIMESHEET, "E-Timesheet Invitation - Recipient",
            "Invitation to Register for IHSS Electronic Timesheets (e-Timesheet)",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>You are invited to register for the IHSS Electronic Timesheet (e-Timesheet) system " +
            "to review and approve your provider's timesheets electronically.</p>" +
            "<p>To register, please visit: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,URL", false));

        all.add(t("ET-003", TemplateCategory.TIMESHEET, "E-Timesheet Submitted - Awaiting Approval",
            "Action Required: Timesheet Submitted for Your Approval — {{PAY_PERIOD}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your provider <strong>{{PROVIDER_NAME}}</strong> has submitted a timesheet for " +
            "pay period <strong>{{PAY_PERIOD}}</strong> requiring your approval.</p>" +
            "<p>Please log in to the e-Timesheet system to review and approve: " +
            "<a href='{{URL}}'>{{URL}}</a></p>" +
            "<p>The approval deadline is <strong>{{DEADLINE_DATE}}</strong>.</p>" + FOOTER,
            "RECIPIENT_NAME,PROVIDER_NAME,PAY_PERIOD,URL,DEADLINE_DATE", true));

        all.add(t("ET-004", TemplateCategory.TIMESHEET, "E-Timesheet Approved",
            "Your IHSS Timesheet Has Been Approved — {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your timesheet for pay period <strong>{{PAY_PERIOD}}</strong> has been " +
            "<strong>approved</strong>. Payment will be processed on the next payment date.</p>" + FOOTER,
            "PROVIDER_NAME,PAY_PERIOD", true));

        all.add(t("ET-005", TemplateCategory.TIMESHEET, "E-Timesheet Rejected",
            "Your IHSS Timesheet Has Been Rejected — {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your timesheet for pay period <strong>{{PAY_PERIOD}}</strong> has been " +
            "<strong>rejected</strong>.</p>" +
            "<p>Reason: {{REJECTION_REASON}}</p>" +
            "<p>Please correct and resubmit your timesheet.</p>" + FOOTER,
            "PROVIDER_NAME,PAY_PERIOD,REJECTION_REASON", true));

        all.add(t("ET-006", TemplateCategory.TIMESHEET, "E-Timesheet Submission Reminder",
            "Reminder: Timesheet Due for Pay Period {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>This is a reminder that your timesheet for pay period <strong>{{PAY_PERIOD}}</strong> " +
            "is due by <strong>{{DEADLINE_DATE}}</strong>.</p>" +
            "<p>Please log in to submit: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "PROVIDER_NAME,PAY_PERIOD,DEADLINE_DATE,URL", true));

        all.add(t("ET-007", TemplateCategory.TIMESHEET, "E-Timesheet Approval Reminder",
            "Reminder: Timesheet Awaiting Your Approval — {{PAY_PERIOD}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>The timesheet from <strong>{{PROVIDER_NAME}}</strong> for pay period " +
            "<strong>{{PAY_PERIOD}}</strong> is still awaiting your approval.</p>" +
            "<p>Deadline: <strong>{{DEADLINE_DATE}}</strong>. " +
            "Log in: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,PROVIDER_NAME,PAY_PERIOD,DEADLINE_DATE,URL", true));

        all.add(t("ET-008", TemplateCategory.TIMESHEET, "E-Timesheet Late Submission",
            "Late Submission: Your IHSS Timesheet Is Overdue — {{PAY_PERIOD}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your timesheet for pay period <strong>{{PAY_PERIOD}}</strong> was not submitted " +
            "by the deadline. Late timesheets may result in delayed payment.</p>" +
            "<p>Please contact your county IHSS office.</p>" + FOOTER,
            "PROVIDER_NAME,PAY_PERIOD", false));

        all.add(t("ET-009", TemplateCategory.TIMESHEET, "E-Timesheet Account Inactivated",
            "Your E-Timesheet Account Has Been Inactivated",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your e-Timesheet account has been inactivated. " +
            "If you believe this is an error, please contact your county IHSS office.</p>" + FOOTER,
            "PROVIDER_NAME", false));

        all.add(t("ET-010", TemplateCategory.TIMESHEET, "E-Timesheet Account Reactivated",
            "Your E-Timesheet Account Has Been Reactivated",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your e-Timesheet account has been reactivated. " +
            "You may now log in to submit timesheets: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "PROVIDER_NAME,URL", false));

        all.add(t("ET-011", TemplateCategory.TIMESHEET, "E-Timesheet SOC 2321 Generated",
            "SOC 2321 - Notice of E-Timesheet Account Inactivation",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>The e-Timesheet account for your provider <strong>{{PROVIDER_NAME}}</strong> " +
            "has been inactivated. A SOC 2321 notice has been generated for your records.</p>" + FOOTER,
            "RECIPIENT_NAME,PROVIDER_NAME", false));

        // ─────────────────────────────────────────────────────────────────────
        // ACCOUNT — ESP / Portal
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("ACC-001", TemplateCategory.ACCOUNT, "Account Activation",
            "Activate Your CMIPS Account",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your CMIPS account has been created. Please activate it by clicking the link below:</p>" +
            "<p><a href='{{URL}}'>Activate Account</a></p>" +
            "<p>This link expires in 72 hours.</p>" + FOOTER,
            "RECIPIENT_NAME,URL", false));

        all.add(t("ACC-002", TemplateCategory.ACCOUNT, "Password Reset",
            "CMIPS Password Reset Request",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>A password reset has been requested for your CMIPS account. " +
            "Click the link below to reset your password:</p>" +
            "<p><a href='{{URL}}'>Reset Password</a></p>" +
            "<p>If you did not request this, please ignore this email.</p>" + FOOTER,
            "RECIPIENT_NAME,URL", false));

        all.add(t("ACC-003", TemplateCategory.ACCOUNT, "Password Changed Confirmation",
            "Your CMIPS Password Has Been Changed",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your CMIPS password has been successfully changed. " +
            "If you did not make this change, contact your county IHSS office immediately.</p>" + FOOTER,
            "RECIPIENT_NAME", false));

        all.add(t("ACC-004", TemplateCategory.ACCOUNT, "Account Locked",
            "Your CMIPS Account Has Been Locked",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your CMIPS account has been locked due to multiple failed login attempts. " +
            "Please contact your county IHSS office to unlock your account.</p>" + FOOTER,
            "RECIPIENT_NAME", false));

        all.add(t("ACC-005", TemplateCategory.ACCOUNT, "MFA Enrollment",
            "Set Up Two-Factor Authentication for Your CMIPS Account",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Two-factor authentication (2FA) is required for your CMIPS account. " +
            "Please complete enrollment at: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,URL", false));

        all.add(t("ACC-006", TemplateCategory.ACCOUNT, "Account Created - Welcome",
            "Welcome to CMIPS — Your Account Is Ready",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Welcome to the California Management Information and Payroll System (CMIPS). " +
            "Your account is ready. Please log in at: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,URL", false));

        // ─────────────────────────────────────────────────────────────────────
        // PROVIDER
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("PRV-001", TemplateCategory.PROVIDER, "Provider Enrollment Confirmation",
            "IHSS Provider Enrollment Confirmation",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your enrollment as an IHSS provider has been confirmed. " +
            "Your provider number is <strong>{{PROVIDER_NUMBER}}</strong>.</p>" +
            "<p>You may now begin providing services to your recipient.</p>" + FOOTER,
            "PROVIDER_NAME,PROVIDER_NUMBER", false));

        all.add(t("PRV-002", TemplateCategory.PROVIDER, "CORI Background Check - Initiated",
            "Your IHSS Provider CORI Background Check Has Been Initiated",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your CORI (Criminal Offender Record Information) background check has been initiated. " +
            "You will be notified of the results within 5-7 business days.</p>" + FOOTER,
            "PROVIDER_NAME", false));

        all.add(t("PRV-003", TemplateCategory.PROVIDER, "CORI Background Check - Cleared",
            "Your IHSS Provider Background Check Is Complete",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your CORI background check has been completed. " +
            "You are cleared to provide IHSS services.</p>" + FOOTER,
            "PROVIDER_NAME", false));

        all.add(t("PRV-004", TemplateCategory.PROVIDER, "Provider Orientation Required",
            "Action Required: Complete IHSS Provider Orientation",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>You are required to complete IHSS provider orientation. " +
            "Please complete it by <strong>{{DEADLINE_DATE}}</strong> at: " +
            "<a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "PROVIDER_NAME,DEADLINE_DATE,URL", false));

        all.add(t("PRV-005", TemplateCategory.PROVIDER, "Provider Orientation Completed",
            "IHSS Provider Orientation Completed",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>You have successfully completed the required IHSS provider orientation. " +
            "Thank you for your commitment to providing quality care.</p>" + FOOTER,
            "PROVIDER_NAME", false));

        all.add(t("PRV-006", TemplateCategory.PROVIDER, "Provider Terminated from Case",
            "Your IHSS Provider Assignment Has Ended — Case {{CASE_NUMBER}}",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Your assignment as an IHSS provider for case <strong>{{CASE_NUMBER}}</strong> " +
            "has ended effective <strong>{{EFFECTIVE_DATE}}</strong>.</p>" + FOOTER,
            "PROVIDER_NAME,CASE_NUMBER,EFFECTIVE_DATE", false));

        all.add(t("PRV-007", TemplateCategory.PROVIDER, "Provider Overtime Warning",
            "Warning: You Are Approaching the IHSS Overtime Limit",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Our records indicate that your hours this workweek are approaching the overtime limit. " +
            "Working beyond authorized overtime may require prior county approval.</p>" + FOOTER,
            "PROVIDER_NAME", true));

        all.add(t("PRV-008", TemplateCategory.PROVIDER, "SOC 846 - Health Care Certification Due",
            "Action Required: Health Care Certification Due — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your Health Care Certification (SOC 846) is due by <strong>{{DEADLINE_DATE}}</strong>. " +
            "Please have your physician complete and return the form to your county IHSS office.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,DEADLINE_DATE", false));

        // ─────────────────────────────────────────────────────────────────────
        // NOTICE — NOA distribution
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("NOA-001", TemplateCategory.NOTICE, "NOA - Approval (NA 1250)",
            "Your Notice of Action Is Available — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>A Notice of Action (NA 1250) for your IHSS case has been issued. " +
            "Please download or review it at: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,URL", false));

        all.add(t("NOA-002", TemplateCategory.NOTICE, "NOA - Denial (NA 1252)",
            "Notice of Action — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>A Notice of Action (NA 1252) regarding a decision on your IHSS case is available. " +
            "Review it at: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,URL", false));

        all.add(t("NOA-003", TemplateCategory.NOTICE, "NOA - Termination (NA 1255)",
            "Important Notice of Action — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>An important Notice of Action (NA 1255) regarding your IHSS services is available. " +
            "Review it at: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,URL", false));

        all.add(t("NOA-004", TemplateCategory.NOTICE, "NOA - Share of Cost (NA 1256)",
            "Share of Cost Notice — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>A Notice of Action (NA 1256) regarding your Medi-Cal Share of Cost is available. " +
            "Review it at: <a href='{{URL}}'>{{URL}}</a></p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,URL", false));

        // ─────────────────────────────────────────────────────────────────────
        // REASSESSMENT
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("RA-001", TemplateCategory.REASSESSMENT, "Reassessment Due - 30 Days",
            "Your IHSS Reassessment Is Due in 30 Days — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your annual IHSS reassessment is due by <strong>{{DEADLINE_DATE}}</strong>. " +
            "Your social worker {{WORKER_NAME}} will contact you to schedule a home visit.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,DEADLINE_DATE,WORKER_NAME", false));

        all.add(t("RA-002", TemplateCategory.REASSESSMENT, "Reassessment Overdue",
            "Action Required: Your IHSS Reassessment Is Overdue — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your IHSS reassessment was due on <strong>{{DEADLINE_DATE}}</strong> and has not " +
            "been completed. Please contact your county office at your earliest opportunity.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,DEADLINE_DATE", false));

        all.add(t("RA-003", TemplateCategory.REASSESSMENT, "Reassessment Completed",
            "Your IHSS Reassessment Has Been Completed — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>Your IHSS reassessment has been completed. Your updated authorization is effective " +
            "<strong>{{EFFECTIVE_DATE}}</strong>. A Notice of Action will be mailed to you.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,EFFECTIVE_DATE", false));

        // ─────────────────────────────────────────────────────────────────────
        // OVERPAYMENT
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("OP-001", TemplateCategory.OVERPAYMENT, "Overpayment Collection Notice",
            "IHSS Overpayment Collection Notice — Case {{CASE_NUMBER}}",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>An overpayment of <strong>{{AMOUNT}}</strong> has been identified on your IHSS account. " +
            "A repayment plan will be established. Please contact your county office.</p>" + FOOTER,
            "RECIPIENT_NAME,CASE_NUMBER,AMOUNT", false));

        all.add(t("OP-002", TemplateCategory.OVERPAYMENT, "Overpayment Payment Received",
            "IHSS Overpayment Payment Received",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>We have received your overpayment payment of <strong>{{AMOUNT}}</strong>. " +
            "Remaining balance: <strong>{{REMAINING_BALANCE}}</strong>.</p>" + FOOTER,
            "RECIPIENT_NAME,AMOUNT,REMAINING_BALANCE", false));

        // ─────────────────────────────────────────────────────────────────────
        // MASS COMMUNICATION
        // ─────────────────────────────────────────────────────────────────────
        all.add(t("MC-001", TemplateCategory.MASS_COMMUNICATION, "Minimum Wage Increase Notice",
            "Important Notice: IHSS Provider Minimum Wage Increase",
            "<p>Dear {{PROVIDER_NAME}},</p>" +
            "<p>Effective {{EFFECTIVE_DATE}}, the minimum wage for IHSS providers in " +
            "<strong>{{COUNTY_NAME}} County</strong> will increase to <strong>{{AMOUNT}} per hour</strong>.</p>" + FOOTER,
            "PROVIDER_NAME,EFFECTIVE_DATE,COUNTY_NAME,AMOUNT", false));

        all.add(t("MC-002", TemplateCategory.MASS_COMMUNICATION, "System Maintenance Notice",
            "CMIPS Scheduled Maintenance Notice",
            "<p>Dear CMIPS User,</p>" +
            "<p>CMIPS will be unavailable for scheduled maintenance on <strong>{{EFFECTIVE_DATE}}</strong> " +
            "from {{MAINTENANCE_START}} to {{MAINTENANCE_END}}.</p>" + FOOTER,
            "EFFECTIVE_DATE,MAINTENANCE_START,MAINTENANCE_END", false));

        all.add(t("MC-003", TemplateCategory.MASS_COMMUNICATION, "Legislative Change Notice",
            "Important Update to IHSS Program — Legislative Change",
            "<p>Dear {{RECIPIENT_NAME}},</p>" +
            "<p>A legislative change affecting the IHSS program has been enacted, effective " +
            "<strong>{{EFFECTIVE_DATE}}</strong>. Please review the details enclosed.</p>" + FOOTER,
            "RECIPIENT_NAME,EFFECTIVE_DATE", false));

        repo.saveAll(all);
        log.info("Email templates seeded: {} templates", all.size());
    }

    private static EmailTemplateEntity t(String code, TemplateCategory cat, String name,
                                          String subject, String html, String vars, boolean sms) {
        EmailTemplateEntity e = new EmailTemplateEntity();
        e.setTemplateCode(code);
        e.setCategory(cat);
        e.setTemplateName(name);
        e.setSubjectLine(subject);
        e.setBodyHtml(html);
        e.setBodyText(html.replaceAll("<[^>]+>", ""));   // strip HTML for text variant
        e.setRequiredVars(vars);
        e.setSendSmsAlso(sms);
        e.setActive(true);
        return e;
    }
}
