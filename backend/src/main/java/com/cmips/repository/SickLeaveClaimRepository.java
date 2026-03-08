package com.cmips.repository;

import com.cmips.entity.SickLeaveClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SickLeaveClaimRepository extends JpaRepository<SickLeaveClaimEntity, Long> {

    List<SickLeaveClaimEntity> findByProviderIdAndStatusOrderByClaimEnteredDateDesc(Long providerId, String status);

    List<SickLeaveClaimEntity> findByProviderIdOrderByClaimEnteredDateDesc(Long providerId);

    List<SickLeaveClaimEntity> findByProviderIdOrderByPayPeriodBeginDateDesc(Long providerId);

    List<SickLeaveClaimEntity> findByCaseIdOrderByClaimEnteredDateDesc(Long caseId);

    Optional<SickLeaveClaimEntity> findByClaimNumber(String claimNumber);

    /**
     * July 31 forfeiture — all ACTIVE claims not yet issued, entered before the given cutoff date.
     * Per DSD SB-3: the claim submission window for the prior FY closes on July 31.
     * Any claim still ACTIVE and unissued (issuedDate IS NULL) as of July 31 is forfeited.
     */
    @Query("SELECT c FROM SickLeaveClaimEntity c WHERE c.status = 'ACTIVE' AND c.issuedDate IS NULL AND c.claimEnteredDate < :cutoffDate")
    List<SickLeaveClaimEntity> findActiveUnissuedClaimsBefore(@Param("cutoffDate") LocalDate cutoffDate);
}
