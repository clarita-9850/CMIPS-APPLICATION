package com.cmips.repository;

import com.cmips.entity.CareerPathwayClaimEntity;
import com.cmips.entity.CareerPathwayClaimEntity.ClaimStatus;
import com.cmips.entity.CareerPathwayClaimEntity.CareerPathwayCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CareerPathwayClaimRepository extends JpaRepository<CareerPathwayClaimEntity, Long> {
    List<CareerPathwayClaimEntity> findByProviderIdOrderByCreatedAtDesc(String providerId);
    List<CareerPathwayClaimEntity> findByProviderIdAndStatusOrderByCreatedAtDesc(String providerId, ClaimStatus status);
    List<CareerPathwayClaimEntity> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
    List<CareerPathwayClaimEntity> findByStatusInOrderByCreatedAtDesc(List<ClaimStatus> statuses);

    /** Cumulative training hours paid per pathway per provider */
    @Query("SELECT COALESCE(SUM(c.trainingHoursClaimedMinutes - COALESCE(c.trainingHoursNotPaidMinutes, 0)), 0) " +
           "FROM CareerPathwayClaimEntity c WHERE c.providerId = :providerId " +
           "AND c.careerPathwayCategory = :category AND c.status IN ('PAID', 'PENDING_PAYROLL', 'APPROVED')")
    Integer sumPaidTrainingMinutesByProviderAndCategory(String providerId, CareerPathwayCategory category);

    List<CareerPathwayClaimEntity> findByProviderIdAndServicePeriodFromBetweenOrderByServicePeriodFromDesc(
            String providerId, LocalDate from, LocalDate to);
}
