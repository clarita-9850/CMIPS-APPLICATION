package com.cmips.repository;

import com.cmips.entity.TimesheetIssuanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetIssuanceRepository extends JpaRepository<TimesheetIssuanceEntity, Long> {

    List<TimesheetIssuanceEntity> findByCaseIdOrderByPayPeriodStartDesc(Long caseId);

    List<TimesheetIssuanceEntity> findByProviderIdOrderByPayPeriodStartDesc(Long providerId);

    List<TimesheetIssuanceEntity> findByStatusOrderByCreatedAtAsc(TimesheetIssuanceEntity.IssuanceStatus status);

    Optional<TimesheetIssuanceEntity> findByIssuanceNumber(String issuanceNumber);

    Optional<TimesheetIssuanceEntity> findByTimesheetId(Long timesheetId);

    @Query("SELECT i FROM TimesheetIssuanceEntity i WHERE i.caseId = :caseId AND i.providerId = :providerId " +
            "AND i.payPeriodStart = :ppStart AND i.payPeriodEnd = :ppEnd AND i.status != 'CANCELLED'")
    List<TimesheetIssuanceEntity> findActiveIssuance(@Param("caseId") Long caseId,
            @Param("providerId") Long providerId, @Param("ppStart") LocalDate ppStart,
            @Param("ppEnd") LocalDate ppEnd);

    @Query("SELECT i FROM TimesheetIssuanceEntity i WHERE i.status = 'PENDING_GENERATION' " +
            "AND i.payPeriodStart <= :date")
    List<TimesheetIssuanceEntity> findPendingForGeneration(@Param("date") LocalDate date);

    @Query("SELECT i FROM TimesheetIssuanceEntity i WHERE i.status = 'GENERATED' " +
            "AND i.issuanceMethod = 'BATCH_PRINT'")
    List<TimesheetIssuanceEntity> findPendingForBatchPrint();
}
