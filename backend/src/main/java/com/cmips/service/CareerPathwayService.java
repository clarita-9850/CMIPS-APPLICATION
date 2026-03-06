package com.cmips.service;

import com.cmips.entity.CareerPathwayClaimEntity;
import com.cmips.entity.CareerPathwayClaimEntity.ClaimStatus;
import com.cmips.entity.CareerPathwayClaimEntity.CareerPathwayCategory;
import com.cmips.repository.CareerPathwayClaimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Career Pathway Service (DSD Section 27 CI-823458)
 *
 * Business rules:
 *  - Providers submit claims via ESP; CMIPS creates record with PENDING_REVIEW status
 *  - Two-step CDSS approval: initial reviewer → CDSS Payments Pending Approval queue → final approval
 *  - Training Incentive: $500, requires 15+ hours in at least 1 of 5 CP categories
 *  - One-Month Incentive: $500, requires 15+ hours + 1 month with recipient in specialized pathway
 *  - Six-Month Incentive: $2000, requires 15+ hours + 6 months with recipient in specialized pathway
 *  - Training Time: paid at county default rate per hour; CDSS can reduce hours during review
 *  - Training hours included in provider's weekly overtime calculation
 *  - On approval: CDSS sends claim to MAS system in nightly batch (PRDR908A)
 *  - If payment voided, CDSS worker can reissue claim (creates duplicate, not counted in cumulative hours)
 *  - Training time claims can be approved per class; incentive claims — comments only before submission
 */
@Service
public class CareerPathwayService {

    private static final Logger log = LoggerFactory.getLogger(CareerPathwayService.class);

    private static final Map<CareerPathwayClaimEntity.ClaimType, java.math.BigDecimal> INCENTIVE_AMOUNTS = Map.of(
            CareerPathwayClaimEntity.ClaimType.TRAINING_INCENTIVE,    new java.math.BigDecimal("500.00"),
            CareerPathwayClaimEntity.ClaimType.ONE_MONTH_INCENTIVE,   new java.math.BigDecimal("500.00"),
            CareerPathwayClaimEntity.ClaimType.SIX_MONTH_INCENTIVE,   new java.math.BigDecimal("2000.00")
    );

    private final CareerPathwayClaimRepository repo;

    public CareerPathwayService(CareerPathwayClaimRepository repo) {
        this.repo = repo;
    }

    public List<CareerPathwayClaimEntity> getByProvider(String providerId) {
        return repo.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    public List<CareerPathwayClaimEntity> getPendingReview() {
        return repo.findByStatusOrderByCreatedAtDesc(ClaimStatus.PENDING_REVIEW);
    }

    public List<CareerPathwayClaimEntity> getPendingApproval() {
        return repo.findByStatusOrderByCreatedAtDesc(ClaimStatus.PENDING_APPROVAL);
    }

    public CareerPathwayClaimEntity getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Career Pathway claim not found: " + id));
    }

    /** Get cumulative training hours per pathway for a provider */
    public Map<String, Integer> getCumulativeHours(String providerId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (CareerPathwayCategory category : CareerPathwayCategory.values()) {
            Integer minutes = repo.sumPaidTrainingMinutesByProviderAndCategory(providerId, category);
            result.put(category.name(), minutes != null ? minutes : 0);
        }
        return result;
    }

    @Transactional
    public CareerPathwayClaimEntity create(CareerPathwayClaimEntity claim, String userId) {
        // Set fixed incentive amount
        if (claim.getClaimType() != CareerPathwayClaimEntity.ClaimType.TRAINING_TIME) {
            claim.setIncentiveAmount(INCENTIVE_AMOUNTS.get(claim.getClaimType()));
        }
        claim.setStatus(ClaimStatus.PENDING_REVIEW);
        claim.setReceivedDate(LocalDate.now());
        CareerPathwayClaimEntity saved = repo.save(claim);
        log.info("[CareerPathway] Created id={} type={} provider={}", saved.getId(), saved.getClaimType(), saved.getProviderId());
        return saved;
    }

    /** Initial reviewer: submit claim for approval (possibly with modifications) */
    @Transactional
    public CareerPathwayClaimEntity submitForApproval(Long id, CareerPathwayClaimEntity updates, String userId) {
        CareerPathwayClaimEntity claim = getById(id);
        if (claim.getStatus() != ClaimStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Only PENDING_REVIEW claims can be submitted for approval");
        }
        // Apply any updates from initial reviewer (e.g., reduce training hours, update class info)
        if (updates.getTrainingHoursClaimedMinutes() != null) {
            claim.setTrainingHoursClaimedMinutes(updates.getTrainingHoursClaimedMinutes());
        }
        if (updates.getTrainingHoursNotPaidMinutes() != null) {
            claim.setTrainingHoursNotPaidMinutes(updates.getTrainingHoursNotPaidMinutes());
        }
        if (updates.getReviewerComments() != null) {
            claim.setReviewerComments(updates.getReviewerComments());
        }
        if (updates.getServicePeriodFrom() != null) claim.setServicePeriodFrom(updates.getServicePeriodFrom());
        if (updates.getServicePeriodTo() != null) claim.setServicePeriodTo(updates.getServicePeriodTo());
        if (updates.getClassName() != null) claim.setClassName(updates.getClassName());
        if (updates.getClassNumber() != null) claim.setClassNumber(updates.getClassNumber());
        if (updates.getTrainingDateFrom() != null) claim.setTrainingDateFrom(updates.getTrainingDateFrom());
        if (updates.getTrainingDateTo() != null) claim.setTrainingDateTo(updates.getTrainingDateTo());

        claim.setStatus(ClaimStatus.PENDING_APPROVAL);
        claim.setInitialReviewedBy(userId);
        claim.setInitialReviewedAt(LocalDateTime.now());
        log.info("[CareerPathway] Submitted for approval id={} by={}", id, userId);
        return repo.save(claim);
    }

    /** Final CDSS approval */
    @Transactional
    public CareerPathwayClaimEntity approve(Long id, String userId) {
        CareerPathwayClaimEntity claim = getById(id);
        if (claim.getStatus() != ClaimStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL claims can be approved");
        }
        claim.setStatus(ClaimStatus.PENDING_PAYROLL);
        claim.setApprovedBy(userId);
        claim.setApprovedAt(LocalDateTime.now());
        log.info("[CareerPathway] Approved id={} by={} → PENDING_PAYROLL", id, userId);
        return repo.save(claim);
    }

    /** Reject at any review stage */
    @Transactional
    public CareerPathwayClaimEntity reject(Long id, String rejectionReason, String notes, String userId) {
        CareerPathwayClaimEntity claim = getById(id);
        if (claim.getStatus() != ClaimStatus.PENDING_REVIEW &&
                claim.getStatus() != ClaimStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Cannot reject claim in status: " + claim.getStatus());
        }
        claim.setStatus(ClaimStatus.REJECTED);
        try {
            claim.setRejectionReason(CareerPathwayClaimEntity.RejectionReason.valueOf(rejectionReason));
        } catch (Exception e) {
            claim.setRejectionReason(CareerPathwayClaimEntity.RejectionReason.OTHER);
        }
        claim.setRejectionNotes(notes);
        claim.setRejectedBy(userId);
        claim.setRejectedAt(LocalDateTime.now());
        log.info("[CareerPathway] Rejected id={} by={}", id, userId);
        return repo.save(claim);
    }

    /** Reissue a claim whose payment was voided */
    @Transactional
    public CareerPathwayClaimEntity reissue(Long originalId, String userId) {
        CareerPathwayClaimEntity original = getById(originalId);
        CareerPathwayClaimEntity reissued = new CareerPathwayClaimEntity();
        reissued.setProviderId(original.getProviderId());
        reissued.setProviderName(original.getProviderName());
        reissued.setCaseId(original.getCaseId());
        reissued.setClaimType(original.getClaimType());
        reissued.setCareerPathwayCategory(original.getCareerPathwayCategory());
        reissued.setServicePeriodFrom(original.getServicePeriodFrom());
        reissued.setServicePeriodTo(original.getServicePeriodTo());
        reissued.setTrainingHoursClaimedMinutes(original.getTrainingHoursClaimedMinutes());
        reissued.setTrainingHoursNotPaidMinutes(original.getTrainingHoursNotPaidMinutes());
        reissued.setIncentiveAmount(original.getIncentiveAmount());
        reissued.setClassName(original.getClassName());
        reissued.setClassNumber(original.getClassNumber());
        reissued.setTrainingDateFrom(original.getTrainingDateFrom());
        reissued.setTrainingDateTo(original.getTrainingDateTo());
        reissued.setIsReissued(true);
        reissued.setOriginalClaimId(originalId);
        reissued.setStatus(ClaimStatus.PENDING_PAYROLL); // Goes directly to nightly batch
        reissued.setReceivedDate(LocalDate.now());
        CareerPathwayClaimEntity saved = repo.save(reissued);
        log.info("[CareerPathway] Reissued claim originalId={} newId={} by={}", originalId, saved.getId(), userId);
        return saved;
    }

    public Map<String, Object> toMap(CareerPathwayClaimEntity c) {
        return Map.ofEntries(
                Map.entry("id", c.getId()),
                Map.entry("providerId", c.getProviderId()),
                Map.entry("providerName", c.getProviderName() != null ? c.getProviderName() : ""),
                Map.entry("claimType", c.getClaimType()),
                Map.entry("careerPathwayCategory", c.getCareerPathwayCategory()),
                Map.entry("servicePeriodFrom", c.getServicePeriodFrom() != null ? c.getServicePeriodFrom().toString() : ""),
                Map.entry("servicePeriodTo", c.getServicePeriodTo() != null ? c.getServicePeriodTo().toString() : ""),
                Map.entry("trainingHoursClaimedMinutes", c.getTrainingHoursClaimedMinutes() != null ? c.getTrainingHoursClaimedMinutes() : 0),
                Map.entry("trainingHoursNotPaidMinutes", c.getTrainingHoursNotPaidMinutes() != null ? c.getTrainingHoursNotPaidMinutes() : 0),
                Map.entry("incentiveAmount", c.getIncentiveAmount() != null ? c.getIncentiveAmount() : 0),
                Map.entry("status", c.getStatus()),
                Map.entry("reviewerComments", c.getReviewerComments() != null ? c.getReviewerComments() : ""),
                Map.entry("receivedDate", c.getReceivedDate() != null ? c.getReceivedDate().toString() : ""),
                Map.entry("approvedBy", c.getApprovedBy() != null ? c.getApprovedBy() : ""),
                Map.entry("approvedAt", c.getApprovedAt() != null ? c.getApprovedAt().toString() : ""),
                Map.entry("isReissued", c.getIsReissued() != null ? c.getIsReissued() : false),
                Map.entry("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : "")
        );
    }
}
