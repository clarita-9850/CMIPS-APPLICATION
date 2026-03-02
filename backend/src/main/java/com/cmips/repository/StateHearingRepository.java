package com.cmips.repository;

import com.cmips.entity.StateHearingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * State Hearing Repository — per DSD Section 20 CI-67779
 *
 * DSD Search Rules:
 * - Search is limited to a 6-month period from "From Date"
 * - If no "To Date" is indicated, search extends 6 months from "From Date"
 * - Search is limited to the user's county (statewide users can select any county)
 * - State Hearing Status (SSHS) maps to the auto-calculated status field
 */
@Repository
public interface StateHearingRepository extends JpaRepository<StateHearingEntity, Long> {

    List<StateHearingEntity> findByCaseId(Long caseId);

    List<StateHearingEntity> findByCaseNumber(String caseNumber);

    /**
     * DSD State Hearing Search — per CI-67779
     * Searches by status, county, and hearing request date range
     *
     * SSHS status mapping:
     *   SSHS001 (Requested) → status = 'REQUESTED'
     *   SSHS002 (Scheduled) → status = 'SCHEDULED'
     *   SSHS003 (Resolved)  → status = 'RESOLVED'
     *   SSHS004 (Requested And Scheduled) → status IN ('REQUESTED', 'SCHEDULED')
     */
    @Query("SELECT sh FROM StateHearingEntity sh WHERE " +
           "sh.countyCode = :countyCode AND " +
           "sh.hearingRequestDate >= :fromDate AND " +
           "sh.hearingRequestDate <= :toDate AND " +
           "(:status IS NULL OR sh.status = :status)")
    List<StateHearingEntity> searchStateHearings(
            @Param("countyCode") String countyCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status);

    /**
     * Search with SSHS004 (Requested And Scheduled) — returns both REQUESTED and SCHEDULED
     */
    @Query("SELECT sh FROM StateHearingEntity sh WHERE " +
           "sh.countyCode = :countyCode AND " +
           "sh.hearingRequestDate >= :fromDate AND " +
           "sh.hearingRequestDate <= :toDate AND " +
           "sh.status IN ('REQUESTED', 'SCHEDULED')")
    List<StateHearingEntity> searchStateHearingsRequestedAndScheduled(
            @Param("countyCode") String countyCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    List<StateHearingEntity> findByCountyCode(String countyCode);

    List<StateHearingEntity> findByRecipientId(Long recipientId);

    List<StateHearingEntity> findByStatus(String status);
}
