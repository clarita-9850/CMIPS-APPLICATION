package com.cmips.repository;

import com.cmips.entity.TimesheetTimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetTimeEntryRepository extends JpaRepository<TimesheetTimeEntryEntity, Long> {

    List<TimesheetTimeEntryEntity> findByTimesheetIdOrderByEntryDateAsc(Long timesheetId);

    @Query("SELECT SUM(e.hoursClaimed) FROM TimesheetTimeEntryEntity e WHERE e.timesheetId = :timesheetId")
    Double sumHoursClaimedByTimesheetId(@Param("timesheetId") Long timesheetId);

    @Query("SELECT SUM(e.hoursApproved) FROM TimesheetTimeEntryEntity e WHERE e.timesheetId = :timesheetId")
    Double sumHoursApprovedByTimesheetId(@Param("timesheetId") Long timesheetId);

    // Sum hours by work week for FLSA
    @Query("SELECT e.workWeekNumber, SUM(e.hoursClaimed) FROM TimesheetTimeEntryEntity e " +
           "WHERE e.timesheetId = :timesheetId GROUP BY e.workWeekNumber ORDER BY e.workWeekNumber")
    List<Object[]> sumHoursByWorkWeek(@Param("timesheetId") Long timesheetId);

    void deleteByTimesheetId(Long timesheetId);
}
