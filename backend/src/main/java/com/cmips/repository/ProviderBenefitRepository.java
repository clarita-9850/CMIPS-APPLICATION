package com.cmips.repository;

import com.cmips.entity.ProviderBenefitEntity;
import com.cmips.entity.ProviderBenefitEntity.BenefitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderBenefitRepository extends JpaRepository<ProviderBenefitEntity, Long> {

    List<ProviderBenefitEntity> findByProviderId(Long providerId);

    List<ProviderBenefitEntity> findByProviderIdAndStatus(Long providerId, BenefitStatus status);

    // Active benefits for provider
    @Query("SELECT b FROM ProviderBenefitEntity b WHERE b.providerId = :providerId AND b.status = 'ACTIVE'")
    List<ProviderBenefitEntity> findActiveByProviderId(@Param("providerId") Long providerId);

    // Benefits of a specific type for provider
    List<ProviderBenefitEntity> findByProviderIdAndBenefitTypeAndStatus(
            Long providerId, String benefitType, BenefitStatus status);

    // Benefits pending payroll update (for PROO906A interface)
    @Query("SELECT b FROM ProviderBenefitEntity b WHERE " +
           "(b.payrollUpdated = false OR b.payrollUpdated IS NULL)")
    List<ProviderBenefitEntity> findBenefitsPendingPayrollUpdate();

    // SDI-specific query
    @Query("SELECT b FROM ProviderBenefitEntity b WHERE b.providerId = :providerId " +
           "AND b.electiveSdi = true AND b.status = 'ACTIVE'")
    List<ProviderBenefitEntity> findActiveSdiForProvider(@Param("providerId") Long providerId);

    // History (all statuses, ordered by begin date)
    @Query("SELECT b FROM ProviderBenefitEntity b WHERE b.providerId = :providerId " +
           "ORDER BY b.beginDate DESC")
    List<ProviderBenefitEntity> findHistoryForProvider(@Param("providerId") Long providerId);
}
