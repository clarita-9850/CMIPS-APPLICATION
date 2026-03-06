package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.CalendarType;
import com.ihss.scheduler.entity.JobCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobCalendarRepository extends JpaRepository<JobCalendar, Long> {

    Optional<JobCalendar> findByCalendarName(String calendarName);

    List<JobCalendar> findByIsActiveTrue();

    List<JobCalendar> findByCalendarTypeAndIsActiveTrue(CalendarType calendarType);

    boolean existsByCalendarName(String calendarName);

    @Query("SELECT c FROM JobCalendar c " +
           "JOIN FETCH c.dates d " +
           "WHERE c.isActive = true AND d.calendarDate = :date")
    List<JobCalendar> findCalendarsWithDate(@Param("date") LocalDate date);

    @Query("SELECT c FROM JobCalendar c " +
           "JOIN c.assignments a " +
           "WHERE a.job.id = :jobId AND c.isActive = true")
    List<JobCalendar> findCalendarsForJob(@Param("jobId") Long jobId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM JobCalendarDate d " +
           "JOIN d.calendar c " +
           "JOIN c.assignments a " +
           "WHERE a.job.id = :jobId " +
           "AND d.calendarDate = :date " +
           "AND c.isActive = true " +
           "AND a.assignmentType = 'EXCLUDE'")
    boolean isDateExcludedForJob(@Param("jobId") Long jobId, @Param("date") LocalDate date);
}
