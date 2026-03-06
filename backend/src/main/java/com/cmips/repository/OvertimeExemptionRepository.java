package com.cmips.repository;

import com.cmips.entity.OvertimeExemptionEntity;
import com.cmips.entity.OvertimeExemptionEntity.ExemptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OvertimeExemptionRepository extends JpaRepository<OvertimeExemptionEntity, Long> {

    List<OvertimeExemptionEntity> findByProviderId(Long providerId);

    List<OvertimeExemptionEntity> findByProviderIdAndStatus(Long providerId, ExemptionStatus status);

    // Active exemption for a provider (only one should be active at a time)
    @Query("SELECT e FROM OvertimeExemptionEntity e WHERE e.providerId = :providerId " +
           "AND e.status = 'ACTIVE' AND e.beginDate <= :today " +
           "AND (e.endDate IS NULL OR e.endDate >= :today)")
    Optional<OvertimeExemptionEntity> findActiveExemptionForProvider(
            @Param("providerId") Long providerId,
            @Param("today") LocalDate today);

    // Exemptions expiring soon (for batch inactivation)
    @Query("SELECT e FROM OvertimeExemptionEntity e WHERE e.status = 'ACTIVE' " +
           "AND e.endDate IS NOT NULL AND e.endDate <= :expiryDate")
    List<OvertimeExemptionEntity> findExemptionsExpiringSoon(@Param("expiryDate") LocalDate expiryDate);

    // Unprocessed callback hours
    @Query("SELECT e FROM OvertimeExemptionEntity e WHERE e.callbackHours IS NOT NULL " +
           "AND e.callbackHours > 0 AND (e.callbackProcessed = false OR e.callbackProcessed IS NULL)")
    List<OvertimeExemptionEntity> findUnprocessedCallbacks();

    // History for provider (all statuses)
    @Query("SELECT e FROM OvertimeExemptionEntity e WHERE e.providerId = :providerId " +
           "ORDER BY e.beginDate DESC")
    List<OvertimeExemptionEntity> findExemptionHistoryForProvider(@Param("providerId") Long providerId);

    // Pending CDSS approval
    List<OvertimeExemptionEntity> findByStatusAndCdssApprovedIsNull(ExemptionStatus status);
}
