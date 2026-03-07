package com.cmips.repository;

import com.cmips.entity.TimesheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IhssTimesheetRepository extends JpaRepository<TimesheetEntity, Long> {

    List<TimesheetEntity> findByCaseIdOrderByPayPeriodStartDesc(Long caseId);

    List<TimesheetEntity> findByRecipientIdOrderByPayPeriodStartDesc(Long recipientId);

    List<TimesheetEntity> findByProviderIdOrderByPayPeriodStartDesc(Long providerId);

    List<TimesheetEntity> findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus status);

    Optional<TimesheetEntity> findByTimesheetNumber(String timesheetNumber);

    @Query("SELECT t FROM TimesheetEntity t WHERE t.caseId = :caseId AND t.providerId = :providerId " +
           "AND t.payPeriodStart = :ppStart AND t.payPeriodEnd = :ppEnd")
    List<TimesheetEntity> findByCaseProviderAndPayPeriod(@Param("caseId") Long caseId,
                                                         @Param("providerId") Long providerId,
                                                         @Param("ppStart") LocalDate ppStart,
                                                         @Param("ppEnd") LocalDate ppEnd);

    @Query("SELECT t FROM TimesheetEntity t WHERE t.recipientId = :recipientId " +
           "AND t.payPeriodStart = :ppStart AND t.payPeriodEnd = :ppEnd")
    List<TimesheetEntity> findByRecipientAndPayPeriod(@Param("recipientId") Long recipientId,
                                                      @Param("ppStart") LocalDate ppStart,
                                                      @Param("ppEnd") LocalDate ppEnd);

    // For FLSA overtime: all timesheets for a provider in a workweek
    @Query("SELECT t FROM TimesheetEntity t WHERE t.providerId = :providerId " +
           "AND t.payPeriodStart <= :weekEnd AND t.payPeriodEnd >= :weekStart " +
           "AND t.status IN ('APPROVED_FOR_PAYROLL', 'SENT_TO_PAYROLL', 'PROCESSED')")
    List<TimesheetEntity> findProviderTimesheetsForWeek(@Param("providerId") Long providerId,
                                                        @Param("weekStart") LocalDate weekStart,
                                                        @Param("weekEnd") LocalDate weekEnd);

    // Held timesheets ready for auto-release
    @Query("SELECT t FROM TimesheetEntity t WHERE t.status IN " +
           "('HOLD_EARLY_SUBMISSION', 'HOLD_LATE_SUBMISSION', 'HOLD_EXCESSIVE_HOURS', " +
           "'HOLD_RANDOM_SAMPLING', 'HOLD_FLAGGED_REVIEW', 'HOLD_USER_REVIEW') " +
           "AND t.holdReleaseDate IS NOT NULL AND t.holdReleaseDate <= CURRENT_TIMESTAMP")
    List<TimesheetEntity> findHeldTimesheetsReadyForRelease();

    // Count for a recipient in a service month
    @Query("SELECT COUNT(t) FROM TimesheetEntity t WHERE t.recipientId = :recipientId " +
           "AND t.serviceMonth = :serviceMonth AND t.status NOT IN ('VOID', 'CANCELLED', 'REJECTED')")
    long countByRecipientAndServiceMonth(@Param("recipientId") Long recipientId,
                                         @Param("serviceMonth") String serviceMonth);

    // Search with filters
    @Query("SELECT t FROM TimesheetEntity t WHERE " +
           "(:caseId IS NULL OR t.caseId = :caseId) AND " +
           "(:recipientId IS NULL OR t.recipientId = :recipientId) AND " +
           "(:providerId IS NULL OR t.providerId = :providerId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:programType IS NULL OR t.programType = :programType) AND " +
           "(:fromDate IS NULL OR t.payPeriodStart >= :fromDate) AND " +
           "(:toDate IS NULL OR t.payPeriodEnd <= :toDate) AND " +
           "(:countyCode IS NULL OR t.countyCode = :countyCode) " +
           "ORDER BY t.payPeriodStart DESC")
    List<TimesheetEntity> searchTimesheets(@Param("caseId") Long caseId,
                                           @Param("recipientId") Long recipientId,
                                           @Param("providerId") Long providerId,
                                           @Param("status") TimesheetEntity.TimesheetStatus status,
                                           @Param("programType") TimesheetEntity.ProgramType programType,
                                           @Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate,
                                           @Param("countyCode") String countyCode);
}
