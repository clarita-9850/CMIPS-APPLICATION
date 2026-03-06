package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.JobCalendarDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobCalendarDateRepository extends JpaRepository<JobCalendarDate, Long> {

    List<JobCalendarDate> findByCalendarId(Long calendarId);

    Optional<JobCalendarDate> findByCalendarIdAndCalendarDate(Long calendarId, LocalDate date);

    boolean existsByCalendarIdAndCalendarDate(Long calendarId, LocalDate date);

    @Query("SELECT d FROM JobCalendarDate d " +
           "WHERE d.calendar.id = :calendarId " +
           "AND d.calendarDate BETWEEN :startDate AND :endDate " +
           "ORDER BY d.calendarDate")
    List<JobCalendarDate> findByCalendarIdAndDateRange(
        @Param("calendarId") Long calendarId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT d.calendarDate FROM JobCalendarDate d " +
           "WHERE d.calendar.id = :calendarId " +
           "ORDER BY d.calendarDate")
    List<LocalDate> findDatesByCalendarId(@Param("calendarId") Long calendarId);

    void deleteByCalendarIdAndCalendarDate(Long calendarId, LocalDate date);

    @Query("DELETE FROM JobCalendarDate d WHERE d.calendar.id = :calendarId")
    void deleteAllByCalendarId(@Param("calendarId") Long calendarId);
}
