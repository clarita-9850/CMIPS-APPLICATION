package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.AssignmentType;
import com.ihss.scheduler.entity.JobCalendarAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobCalendarAssignmentRepository extends JpaRepository<JobCalendarAssignment, Long> {

    List<JobCalendarAssignment> findByJobId(Long jobId);

    List<JobCalendarAssignment> findByCalendarId(Long calendarId);

    Optional<JobCalendarAssignment> findByJobIdAndCalendarId(Long jobId, Long calendarId);

    boolean existsByJobIdAndCalendarId(Long jobId, Long calendarId);

    @Query("SELECT a FROM JobCalendarAssignment a " +
           "JOIN FETCH a.calendar c " +
           "WHERE a.job.id = :jobId AND c.isActive = true")
    List<JobCalendarAssignment> findActiveAssignmentsForJob(@Param("jobId") Long jobId);

    @Query("SELECT a FROM JobCalendarAssignment a " +
           "JOIN FETCH a.job j " +
           "WHERE a.calendar.id = :calendarId AND j.deletedAt IS NULL")
    List<JobCalendarAssignment> findAssignmentsForCalendar(@Param("calendarId") Long calendarId);

    List<JobCalendarAssignment> findByJobIdAndAssignmentType(Long jobId, AssignmentType assignmentType);

    void deleteByJobIdAndCalendarId(Long jobId, Long calendarId);

    void deleteByJobId(Long jobId);
}
