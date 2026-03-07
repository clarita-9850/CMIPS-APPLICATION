package com.cmips.repository;

import com.cmips.entity.TimesheetExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetExceptionRepository extends JpaRepository<TimesheetExceptionEntity, Long> {

    List<TimesheetExceptionEntity> findByTimesheetIdOrderByRuleNumberAsc(Long timesheetId);

    List<TimesheetExceptionEntity> findByTravelClaimIdOrderByRuleNumberAsc(Long travelClaimId);

    List<TimesheetExceptionEntity> findByTimesheetIdAndExceptionType(Long timesheetId,
            TimesheetExceptionEntity.ExceptionType exceptionType);

    List<TimesheetExceptionEntity> findByTimesheetIdAndResolvedFalse(Long timesheetId);

    long countByTimesheetIdAndExceptionType(Long timesheetId,
            TimesheetExceptionEntity.ExceptionType exceptionType);

    void deleteByTimesheetId(Long timesheetId);
}
