package com.cmips.repository;

import com.cmips.entity.WorkweekAgreementEntity;
import com.cmips.entity.WorkweekAgreementEntity.AgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkweekAgreementRepository extends JpaRepository<WorkweekAgreementEntity, Long> {

    List<WorkweekAgreementEntity> findByProviderId(Long providerId);

    List<WorkweekAgreementEntity> findByProviderIdAndStatus(Long providerId, AgreementStatus status);

    // Active agreements for provider (only one active at a time per DSD BR PVM 73)
    @Query("SELECT w FROM WorkweekAgreementEntity w WHERE w.providerId = :providerId " +
           "AND w.status = 'ACTIVE'")
    Optional<WorkweekAgreementEntity> findActiveAgreementForProvider(@Param("providerId") Long providerId);

    // All history (active + inactive) ordered by begin date
    @Query("SELECT w FROM WorkweekAgreementEntity w WHERE w.providerId = :providerId " +
           "ORDER BY w.beginDate DESC")
    List<WorkweekAgreementEntity> findAgreementHistoryForProvider(@Param("providerId") Long providerId);

    // Inactive history only (for "View Inactive Provider Workweek Agreement History" screen)
    @Query("SELECT w FROM WorkweekAgreementEntity w WHERE w.providerId = :providerId " +
           "AND w.status = 'INACTIVE' ORDER BY w.inactivatedDate DESC")
    List<WorkweekAgreementEntity> findInactiveHistoryForProvider(@Param("providerId") Long providerId);

    // Agreements for a specific recipient link
    List<WorkweekAgreementEntity> findByProviderIdAndRecipientId(Long providerId, Long recipientId);

    // Providers with active workweek agreements (for overtime calculation)
    @Query("SELECT DISTINCT w.providerId FROM WorkweekAgreementEntity w WHERE w.status = 'ACTIVE'")
    List<Long> findProviderIdsWithActiveAgreements();
}
