package com.cmips.repository;

import com.cmips.entity.TravelClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelClaimRepository extends JpaRepository<TravelClaimEntity, Long> {

    List<TravelClaimEntity> findByCaseIdOrderByPayPeriodStartDesc(Long caseId);

    List<TravelClaimEntity> findByProviderIdOrderByPayPeriodStartDesc(Long providerId);

    Optional<TravelClaimEntity> findByTravelClaimNumber(String travelClaimNumber);

    List<TravelClaimEntity> findByStatusOrderByCreatedAtDesc(TravelClaimEntity.TravelClaimStatus status);

    // Held travel claims waiting for timesheet processing
    List<TravelClaimEntity> findByStatus(TravelClaimEntity.TravelClaimStatus status);

    @Query("SELECT tc FROM TravelClaimEntity tc WHERE tc.providerId = :providerId " +
           "AND tc.payPeriodStart = :ppStart AND tc.payPeriodEnd = :ppEnd")
    List<TravelClaimEntity> findByProviderAndPayPeriod(@Param("providerId") Long providerId,
                                                       @Param("ppStart") LocalDate ppStart,
                                                       @Param("ppEnd") LocalDate ppEnd);

    // Search
    @Query("SELECT tc FROM TravelClaimEntity tc WHERE " +
           "(:caseId IS NULL OR tc.caseId = :caseId) AND " +
           "(:providerId IS NULL OR tc.providerId = :providerId) AND " +
           "(:status IS NULL OR tc.status = :status) AND " +
           "(:fromDate IS NULL OR tc.payPeriodStart >= :fromDate) AND " +
           "(:toDate IS NULL OR tc.payPeriodEnd <= :toDate) " +
           "ORDER BY tc.payPeriodStart DESC")
    List<TravelClaimEntity> searchTravelClaims(@Param("caseId") Long caseId,
                                                @Param("providerId") Long providerId,
                                                @Param("status") TravelClaimEntity.TravelClaimStatus status,
                                                @Param("fromDate") LocalDate fromDate,
                                                @Param("toDate") LocalDate toDate);
}
