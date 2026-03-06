package com.cmips.repository;

import com.cmips.entity.BackupProviderHoursEntity;
import com.cmips.entity.BackupProviderHoursEntity.BackupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupProviderHoursRepository extends JpaRepository<BackupProviderHoursEntity, Long> {

    List<BackupProviderHoursEntity> findByProviderId(Long providerId);

    List<BackupProviderHoursEntity> findByProviderIdAndStatus(Long providerId, BackupStatus status);

    // Active backup hours for a provider
    @Query("SELECT b FROM BackupProviderHoursEntity b WHERE b.providerId = :providerId AND b.status = 'ACTIVE'")
    List<BackupProviderHoursEntity> findActiveByProviderId(@Param("providerId") Long providerId);

    // All backup records where this provider is the PRIMARY (not backup)
    List<BackupProviderHoursEntity> findByPrimaryProviderIdAndStatus(Long primaryProviderId, BackupStatus status);

    // Active backup hours for a specific case
    @Query("SELECT b FROM BackupProviderHoursEntity b WHERE b.providerId = :providerId " +
           "AND b.caseId = :caseId AND b.status = 'ACTIVE'")
    List<BackupProviderHoursEntity> findActiveByCaseAndProvider(
            @Param("providerId") Long providerId,
            @Param("caseId") Long caseId);

    // All history ordered by begin date descending
    @Query("SELECT b FROM BackupProviderHoursEntity b WHERE b.providerId = :providerId " +
           "ORDER BY b.beginDate DESC")
    List<BackupProviderHoursEntity> findHistoryForProvider(@Param("providerId") Long providerId);
}
