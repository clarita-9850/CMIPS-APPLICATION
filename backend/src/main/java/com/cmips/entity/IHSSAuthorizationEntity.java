package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IHSS Authorization Entity - DSD Section 22 pages 386-390.
 *
 * Stores the computed authorization resulting from an IHSS assessment:
 * functional index score, total assessed/unmet need, authorized-to-purchase
 * hours, share-of-cost, advance pay, restaurant meals, weekly distribution,
 * and monthly OT cap. Linked to an assessment via assessmentId.
 *
 * Status codes: ACTIVE, SUPERSEDED, VOIDED.
 */
@Entity
@Table(name = "ihss_authorization", indexes = {
        @Index(name = "idx_ihss_auth_assessment", columnList = "assessment_id")
})
public class IHSSAuthorizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "functional_index_score", precision = 5, scale = 2)
    private BigDecimal functionalIndexScore;

    @Column(name = "total_assessed_need_min")
    private Integer totalAssessedNeedMin;

    @Column(name = "total_unmet_need_min")
    private Integer totalUnmetNeedMin;

    @Column(name = "auth_to_purchase_min")
    private Integer authToPurchaseMin;

    @Column(name = "cc_protect_super_adj_min")
    private Integer ccProtectSuperAdjMin;

    @Column(name = "care_plan_need_24_min")
    private Integer carePlanNeed24Min;

    @Column(name = "severely_impaired_ind")
    private Boolean severelyImpairedInd = false;

    @Column(name = "calculated_soc", precision = 10, scale = 2)
    private BigDecimal calculatedSOC;

    @Column(name = "funding_aid_code", length = 10)
    private String fundingAidCode;

    @Column(name = "compare_cost", precision = 10, scale = 2)
    private BigDecimal compareCost;

    @Column(name = "advance_pay_ind")
    private Boolean advancePayInd = false;

    @Column(name = "restaurant_meals_ind")
    private Boolean restaurantMealsInd = false;

    @Column(name = "parent_of_minor_child_ind")
    private Boolean parentOfMinorChildInd = false;

    @Column(name = "spouse_provider_ind")
    private Boolean spouseProviderInd = false;

    @Column(name = "leg_mandate_adj")
    private Integer legMandateAdj;

    @Column(name = "reduced_hrs")
    private Integer reducedHrs;

    @Column(name = "verified_by_case_owner_or_supervisor")
    private Boolean verifiedByCaseOwnerOrSupervisor = false;

    @Column(name = "active_authorization_ind")
    private Boolean activeAuthorizationInd = true;

    @Column(name = "weekly_auth_monday")
    private Integer weeklyAuthMonday;

    @Column(name = "weekly_auth_tuesday")
    private Integer weeklyAuthTuesday;

    @Column(name = "weekly_auth_wednesday")
    private Integer weeklyAuthWednesday;

    @Column(name = "weekly_auth_thursday")
    private Integer weeklyAuthThursday;

    @Column(name = "weekly_auth_friday")
    private Integer weeklyAuthFriday;

    @Column(name = "weekly_auth_saturday")
    private Integer weeklyAuthSaturday;

    @Column(name = "weekly_auth_sunday")
    private Integer weeklyAuthSunday;

    @Column(name = "case_monthly_ot_max")
    private Integer caseMonthlyOTMax;

    @Column(name = "auth_start_date")
    private LocalDate authStartDate;

    @Column(name = "auth_end_date")
    private LocalDate authEndDate;

    @Column(name = "status_code", length = 20)
    private String statusCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public BigDecimal getFunctionalIndexScore() { return functionalIndexScore; }
    public void setFunctionalIndexScore(BigDecimal functionalIndexScore) { this.functionalIndexScore = functionalIndexScore; }

    public Integer getTotalAssessedNeedMin() { return totalAssessedNeedMin; }
    public void setTotalAssessedNeedMin(Integer totalAssessedNeedMin) { this.totalAssessedNeedMin = totalAssessedNeedMin; }

    public Integer getTotalUnmetNeedMin() { return totalUnmetNeedMin; }
    public void setTotalUnmetNeedMin(Integer totalUnmetNeedMin) { this.totalUnmetNeedMin = totalUnmetNeedMin; }

    public Integer getAuthToPurchaseMin() { return authToPurchaseMin; }
    public void setAuthToPurchaseMin(Integer authToPurchaseMin) { this.authToPurchaseMin = authToPurchaseMin; }

    public Integer getCcProtectSuperAdjMin() { return ccProtectSuperAdjMin; }
    public void setCcProtectSuperAdjMin(Integer ccProtectSuperAdjMin) { this.ccProtectSuperAdjMin = ccProtectSuperAdjMin; }

    public Integer getCarePlanNeed24Min() { return carePlanNeed24Min; }
    public void setCarePlanNeed24Min(Integer carePlanNeed24Min) { this.carePlanNeed24Min = carePlanNeed24Min; }

    public Boolean getSeverelyImpairedInd() { return severelyImpairedInd; }
    public void setSeverelyImpairedInd(Boolean severelyImpairedInd) { this.severelyImpairedInd = severelyImpairedInd; }

    public BigDecimal getCalculatedSOC() { return calculatedSOC; }
    public void setCalculatedSOC(BigDecimal calculatedSOC) { this.calculatedSOC = calculatedSOC; }

    public String getFundingAidCode() { return fundingAidCode; }
    public void setFundingAidCode(String fundingAidCode) { this.fundingAidCode = fundingAidCode; }

    public BigDecimal getCompareCost() { return compareCost; }
    public void setCompareCost(BigDecimal compareCost) { this.compareCost = compareCost; }

    public Boolean getAdvancePayInd() { return advancePayInd; }
    public void setAdvancePayInd(Boolean advancePayInd) { this.advancePayInd = advancePayInd; }

    public Boolean getRestaurantMealsInd() { return restaurantMealsInd; }
    public void setRestaurantMealsInd(Boolean restaurantMealsInd) { this.restaurantMealsInd = restaurantMealsInd; }

    public Boolean getParentOfMinorChildInd() { return parentOfMinorChildInd; }
    public void setParentOfMinorChildInd(Boolean parentOfMinorChildInd) { this.parentOfMinorChildInd = parentOfMinorChildInd; }

    public Boolean getSpouseProviderInd() { return spouseProviderInd; }
    public void setSpouseProviderInd(Boolean spouseProviderInd) { this.spouseProviderInd = spouseProviderInd; }

    public Integer getLegMandateAdj() { return legMandateAdj; }
    public void setLegMandateAdj(Integer legMandateAdj) { this.legMandateAdj = legMandateAdj; }

    public Integer getReducedHrs() { return reducedHrs; }
    public void setReducedHrs(Integer reducedHrs) { this.reducedHrs = reducedHrs; }

    public Boolean getVerifiedByCaseOwnerOrSupervisor() { return verifiedByCaseOwnerOrSupervisor; }
    public void setVerifiedByCaseOwnerOrSupervisor(Boolean verifiedByCaseOwnerOrSupervisor) { this.verifiedByCaseOwnerOrSupervisor = verifiedByCaseOwnerOrSupervisor; }

    public Boolean getActiveAuthorizationInd() { return activeAuthorizationInd; }
    public void setActiveAuthorizationInd(Boolean activeAuthorizationInd) { this.activeAuthorizationInd = activeAuthorizationInd; }

    public Integer getWeeklyAuthMonday() { return weeklyAuthMonday; }
    public void setWeeklyAuthMonday(Integer weeklyAuthMonday) { this.weeklyAuthMonday = weeklyAuthMonday; }

    public Integer getWeeklyAuthTuesday() { return weeklyAuthTuesday; }
    public void setWeeklyAuthTuesday(Integer weeklyAuthTuesday) { this.weeklyAuthTuesday = weeklyAuthTuesday; }

    public Integer getWeeklyAuthWednesday() { return weeklyAuthWednesday; }
    public void setWeeklyAuthWednesday(Integer weeklyAuthWednesday) { this.weeklyAuthWednesday = weeklyAuthWednesday; }

    public Integer getWeeklyAuthThursday() { return weeklyAuthThursday; }
    public void setWeeklyAuthThursday(Integer weeklyAuthThursday) { this.weeklyAuthThursday = weeklyAuthThursday; }

    public Integer getWeeklyAuthFriday() { return weeklyAuthFriday; }
    public void setWeeklyAuthFriday(Integer weeklyAuthFriday) { this.weeklyAuthFriday = weeklyAuthFriday; }

    public Integer getWeeklyAuthSaturday() { return weeklyAuthSaturday; }
    public void setWeeklyAuthSaturday(Integer weeklyAuthSaturday) { this.weeklyAuthSaturday = weeklyAuthSaturday; }

    public Integer getWeeklyAuthSunday() { return weeklyAuthSunday; }
    public void setWeeklyAuthSunday(Integer weeklyAuthSunday) { this.weeklyAuthSunday = weeklyAuthSunday; }

    public Integer getCaseMonthlyOTMax() { return caseMonthlyOTMax; }
    public void setCaseMonthlyOTMax(Integer caseMonthlyOTMax) { this.caseMonthlyOTMax = caseMonthlyOTMax; }

    public LocalDate getAuthStartDate() { return authStartDate; }
    public void setAuthStartDate(LocalDate authStartDate) { this.authStartDate = authStartDate; }

    public LocalDate getAuthEndDate() { return authEndDate; }
    public void setAuthEndDate(LocalDate authEndDate) { this.authEndDate = authEndDate; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
