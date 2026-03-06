package com.cmips.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Career Pathway Claim (DSD Section 27 CI-823458)
 *
 * Under California Senate Bill No. 172, CDSS implemented the Career Pathways Program.
 * IHSS/WPCS providers submit claims via ESP for training time and incentive payments.
 *
 * Four claim types:
 *  1. TRAINING_TIME      - Paid per hour at county default rate; requires 15+ hr completion
 *  2. TRAINING_INCENTIVE - $500 flat; requires 15+ hours in at least 1 of 5 CP categories
 *  3. ONE_MONTH_INCENTIVE - $500 flat; 15+ hours + 1 month with recipient in specialized pathway
 *  4. SIX_MONTH_INCENTIVE - $2000 flat; 15+ hours + 6 months with recipient in specialized pathway
 *
 * Five Career Pathway Categories:
 *  - ADULT_EDUCATION
 *  - GENERAL_HEALTH_SAFETY
 *  - COGNITIVE_IMPAIRMENTS_BEHAVIORAL_HEALTH
 *  - COMPLEX_PHYSICAL_CARE_NEEDS
 *  - TRANSITION_TO_HOME_COMMUNITY_LIVING
 *
 * Two-step CDSS approval process:
 *  1. Initial review: open task → review → submit for approval (or reject)
 *  2. Final review: CDSS Payments Pending Approval work queue → approve (or reject)
 *
 * On approval: status = PENDING_PAYROLL → sent to MAS nightly batch
 */
@Entity
@Table(name = "career_pathway_claims", indexes = {
    @Index(name = "idx_cpc_provider", columnList = "provider_id"),
    @Index(name = "idx_cpc_status", columnList = "status"),
    @Index(name = "idx_cpc_claim_type", columnList = "claim_type"),
    @Index(name = "idx_cpc_service_period", columnList = "service_period_from")
})
@Data
@NoArgsConstructor
public class CareerPathwayClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Provider who submitted the claim via ESP */
    @Column(name = "provider_id", nullable = false, length = 50)
    private String providerId;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "claim_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ClaimType claimType;

    @Column(name = "career_pathway_category", nullable = false, length = 60)
    @Enumerated(EnumType.STRING)
    private CareerPathwayCategory careerPathwayCategory;

    @Column(name = "service_period_from", nullable = false)
    private LocalDate servicePeriodFrom;

    @Column(name = "service_period_to", nullable = false)
    private LocalDate servicePeriodTo;

    /** Training hours claimed (in minutes) — for TRAINING_TIME claims */
    @Column(name = "training_hours_claimed_minutes")
    private Integer trainingHoursClaimedMinutes;

    /** Hours reduced by CDSS during review (Training Hours Not Paid) */
    @Column(name = "training_hours_not_paid_minutes")
    private Integer trainingHoursNotPaidMinutes;

    /** Fixed incentive amount ($500 or $2000 depending on type) */
    @Column(name = "incentive_amount", precision = 12, scale = 2)
    private BigDecimal incentiveAmount;

    /** Class name (for TRAINING_TIME) */
    @Column(name = "class_name", length = 200)
    private String className;

    /** Class number (for TRAINING_TIME) */
    @Column(name = "class_number", length = 50)
    private String classNumber;

    /** Training date range */
    @Column(name = "training_date_from")
    private LocalDate trainingDateFrom;

    @Column(name = "training_date_to")
    private LocalDate trainingDateTo;

    /** CDSS reviewer comments */
    @Column(name = "reviewer_comments", length = 2000)
    private String reviewerComments;

    @Column(name = "rejection_reason", length = 100)
    @Enumerated(EnumType.STRING)
    private RejectionReason rejectionReason;

    @Column(name = "rejection_notes", length = 1000)
    private String rejectionNotes;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ClaimStatus status = ClaimStatus.PENDING_REVIEW;

    /** Initial reviewer (first CDSS worker) */
    @Column(name = "initial_reviewed_by", length = 100)
    private String initialReviewedBy;

    @Column(name = "initial_reviewed_at")
    private LocalDateTime initialReviewedAt;

    /** Final approver (second CDSS worker) */
    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    /** Payroll payment record ID once processed */
    @Column(name = "payroll_payment_id", length = 50)
    private String payrollPaymentId;

    /** Indicates this is a reissued claim (original voided) */
    @Column(name = "is_reissued")
    private Boolean isReissued = false;

    @Column(name = "original_claim_id")
    private Long originalClaimId;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ClaimType {
        TRAINING_TIME,
        TRAINING_INCENTIVE,
        ONE_MONTH_INCENTIVE,
        SIX_MONTH_INCENTIVE
    }

    public enum CareerPathwayCategory {
        ADULT_EDUCATION,
        GENERAL_HEALTH_SAFETY,
        COGNITIVE_IMPAIRMENTS_BEHAVIORAL_HEALTH,
        COMPLEX_PHYSICAL_CARE_NEEDS,
        TRANSITION_TO_HOME_COMMUNITY_LIVING
    }

    public enum ClaimStatus {
        PENDING_REVIEW,    // Submitted by provider via ESP, awaiting initial CDSS review
        PENDING_APPROVAL,  // Initial review submitted, awaiting CDSS Payments Pending Approval
        APPROVED,          // Final CDSS approval — claim ready for payment
        PENDING_PAYROLL,   // Approved, queued for MAS nightly batch
        REJECTED,          // Rejected by initial or final reviewer
        PAID               // Payment generated by MAS
    }

    public enum RejectionReason {
        INSUFFICIENT_TRAINING_HOURS,
        INVALID_CAREER_PATHWAY,
        DUPLICATE_CLAIM,
        PROVIDER_NOT_ELIGIBLE,
        RECIPIENT_NOT_ELIGIBLE,
        OTHER
    }
}
