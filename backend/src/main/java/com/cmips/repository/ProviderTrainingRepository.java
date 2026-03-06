package com.cmips.repository;

import com.cmips.entity.ProviderTrainingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderTrainingRepository extends JpaRepository<ProviderTrainingEntity, Long> {

    List<ProviderTrainingEntity> findByProviderIdOrderByCompletionDateDesc(Long providerId);

    @Query("SELECT t FROM ProviderTrainingEntity t WHERE t.providerId = :providerId AND t.trainingType = :type ORDER BY t.completionDate DESC")
    List<ProviderTrainingEntity> findByProviderIdAndType(
            @Param("providerId") Long providerId,
            @Param("type") String type);

    @Query("SELECT t FROM ProviderTrainingEntity t WHERE t.providerId = :providerId AND t.fiscalYear = :fiscalYear AND t.status = 'ACTIVE'")
    Optional<ProviderTrainingEntity> findActiveByProviderAndFiscalYear(
            @Param("providerId") Long providerId,
            @Param("fiscalYear") String fiscalYear);

    @Query("SELECT t FROM ProviderTrainingEntity t WHERE t.providerId = :providerId AND t.trainingType = 'INITIAL_ORIENTATION' AND t.status = 'ACTIVE'")
    Optional<ProviderTrainingEntity> findActiveOrientation(@Param("providerId") Long providerId);
}
