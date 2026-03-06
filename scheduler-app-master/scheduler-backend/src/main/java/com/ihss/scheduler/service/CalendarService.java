package com.ihss.scheduler.service;

import com.ihss.scheduler.entity.*;
import com.ihss.scheduler.exception.CalendarNotFoundException;
import com.ihss.scheduler.exception.DuplicateCalendarException;
import com.ihss.scheduler.repository.JobCalendarAssignmentRepository;
import com.ihss.scheduler.repository.JobCalendarDateRepository;
import com.ihss.scheduler.repository.JobCalendarRepository;
import com.ihss.scheduler.repository.JobDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);

    private final JobCalendarRepository calendarRepository;
    private final JobCalendarDateRepository dateRepository;
    private final JobCalendarAssignmentRepository assignmentRepository;
    private final JobDefinitionRepository jobRepository;
    private final AuditService auditService;

    public CalendarService(
            JobCalendarRepository calendarRepository,
            JobCalendarDateRepository dateRepository,
            JobCalendarAssignmentRepository assignmentRepository,
            JobDefinitionRepository jobRepository,
            AuditService auditService) {
        this.calendarRepository = calendarRepository;
        this.dateRepository = dateRepository;
        this.assignmentRepository = assignmentRepository;
        this.jobRepository = jobRepository;
        this.auditService = auditService;
    }

    public JobCalendar createCalendar(String name, String description, CalendarType type, String createdBy) {
        log.info("Creating calendar: {} by user: {}", name, createdBy);

        if (calendarRepository.existsByCalendarName(name)) {
            throw new DuplicateCalendarException("Calendar with name '" + name + "' already exists");
        }

        JobCalendar calendar = new JobCalendar();
        calendar.setCalendarName(name);
        calendar.setDescription(description);
        calendar.setCalendarType(type);
        calendar.setIsActive(true);
        calendar.setCreatedBy(createdBy);

        return calendarRepository.save(calendar);
    }

    public JobCalendar updateCalendar(Long id, String description, CalendarType type, Boolean isActive, String updatedBy) {
        JobCalendar calendar = calendarRepository.findById(id)
            .orElseThrow(() -> new CalendarNotFoundException("Calendar not found: " + id));

        if (description != null) calendar.setDescription(description);
        if (type != null) calendar.setCalendarType(type);
        if (isActive != null) calendar.setIsActive(isActive);
        calendar.setUpdatedBy(updatedBy);

        return calendarRepository.save(calendar);
    }

    @Transactional(readOnly = true)
    public JobCalendar getCalendar(Long id) {
        return calendarRepository.findById(id)
            .orElseThrow(() -> new CalendarNotFoundException("Calendar not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<JobCalendar> getAllCalendars() {
        return calendarRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<JobCalendar> getCalendarsByType(CalendarType type) {
        return calendarRepository.findByCalendarTypeAndIsActiveTrue(type);
    }

    public void deleteCalendar(Long id) {
        JobCalendar calendar = calendarRepository.findById(id)
            .orElseThrow(() -> new CalendarNotFoundException("Calendar not found: " + id));

        calendarRepository.delete(calendar);
    }

    // Calendar Date Management
    public void addDate(Long calendarId, LocalDate date, String description) {
        JobCalendar calendar = calendarRepository.findById(calendarId)
            .orElseThrow(() -> new CalendarNotFoundException("Calendar not found: " + calendarId));

        if (dateRepository.existsByCalendarIdAndCalendarDate(calendarId, date)) {
            throw new IllegalStateException("Date already exists in calendar");
        }

        JobCalendarDate calendarDate = new JobCalendarDate();
        calendarDate.setCalendar(calendar);
        calendarDate.setCalendarDate(date);
        calendarDate.setDescription(description);

        dateRepository.save(calendarDate);
    }

    public void addDates(Long calendarId, List<LocalDate> dates) {
        JobCalendar calendar = calendarRepository.findById(calendarId)
            .orElseThrow(() -> new CalendarNotFoundException("Calendar not found: " + calendarId));

        for (LocalDate date : dates) {
            if (!dateRepository.existsByCalendarIdAndCalendarDate(calendarId, date)) {
                JobCalendarDate calendarDate = new JobCalendarDate();
                calendarDate.setCalendar(calendar);
                calendarDate.setCalendarDate(date);
                dateRepository.save(calendarDate);
            }
        }
    }

    public void removeDate(Long calendarId, LocalDate date) {
        dateRepository.deleteByCalendarIdAndCalendarDate(calendarId, date);
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getDatesInRange(Long calendarId, LocalDate start, LocalDate end) {
        return dateRepository.findByCalendarIdAndDateRange(calendarId, start, end)
            .stream()
            .map(JobCalendarDate::getCalendarDate)
            .toList();
    }

    // Calendar Assignment Management
    public void assignCalendarToJob(Long jobId, Long calendarId, AssignmentType type, String assignedBy) {
        log.info("Assigning calendar {} to job {} by user: {}", calendarId, jobId, assignedBy);

        JobDefinition job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        JobCalendar calendar = calendarRepository.findById(calendarId)
            .orElseThrow(() -> new CalendarNotFoundException("Calendar not found: " + calendarId));

        if (assignmentRepository.existsByJobIdAndCalendarId(jobId, calendarId)) {
            throw new IllegalStateException("Assignment already exists");
        }

        JobCalendarAssignment assignment = new JobCalendarAssignment();
        assignment.setJob(job);
        assignment.setCalendar(calendar);
        assignment.setAssignmentType(type);
        assignment.setCreatedBy(assignedBy);

        assignmentRepository.save(assignment);
    }

    public void removeCalendarFromJob(Long jobId, Long calendarId) {
        assignmentRepository.deleteByJobIdAndCalendarId(jobId, calendarId);
    }

    @Transactional(readOnly = true)
    public List<JobCalendar> getCalendarsForJob(Long jobId) {
        return calendarRepository.findCalendarsForJob(jobId);
    }

    @Transactional(readOnly = true)
    public boolean shouldSkipDate(Long jobId, LocalDate date) {
        return calendarRepository.isDateExcludedForJob(jobId, date);
    }
}
