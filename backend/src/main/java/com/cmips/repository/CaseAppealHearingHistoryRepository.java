package com.cmips.repository;

import com.cmips.entity.CaseAppealHearingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CaseAppealHearingHistory — tracks rescheduled hearing dates.
 * Per DSD Section 25: "Previously Scheduled Hearings" section on View screen.
 */
@Repository
public interface CaseAppealHearingHistoryRepository extends JpaRepository<CaseAppealHearingHistory, Long> {

    List<CaseAppealHearingHistory> findByAppealIdOrderByHearingDateDesc(Long appealId);
}
