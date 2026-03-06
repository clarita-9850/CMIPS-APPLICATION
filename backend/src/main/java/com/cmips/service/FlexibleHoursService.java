package com.cmips.service;

import com.cmips.entity.FlexibleHoursEntity;
import com.cmips.entity.FlexibleHoursEntity.FlexStatus;
import com.cmips.entity.FlexibleHoursEntity.Frequency;
import com.cmips.entity.FlexibleHoursEntity.Program;
import com.cmips.repository.FlexibleHoursRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Flexible Hours Service — DSD Section 25, CI-67807
 *
 * Allows recipients to flex hours week-to-week beyond authorized maximum.
 * Business rules:
 * - Max 80 hours (4800 minutes) approved per service month.
 * - Service month cannot be more than 3 calendar months prior to today.
 * - Approval sets approvedHours = hoursRequested unless overridden.
 */
@Service
public class FlexibleHoursService {

    private static final Logger log = LoggerFactory.getLogger(FlexibleHoursService.class);
    private static final int MAX_MINUTES_PER_MONTH = 4800; // 80 hours * 60 minutes

    private final FlexibleHoursRepository flexibleHoursRepository;

    public FlexibleHoursService(FlexibleHoursRepository flexibleHoursRepository) {
        this.flexibleHoursRepository = flexibleHoursRepository;
    }

    public List<FlexibleHoursEntity> getFlexibleHours(Long caseId) {
        return flexibleHoursRepository.findByCaseIdOrderByServiceMonthDesc(caseId);
    }

    public FlexibleHoursEntity getFlexibleHours(Long id, boolean byId) {
        return flexibleHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flexible hours record not found: " + id));
    }

    @Transactional
    public FlexibleHoursEntity createFlexibleHours(Long caseId, Map<String, Object> request,
                                                    String createdBy) {
        if (request.get("frequency") == null) {
            throw new IllegalArgumentException("Frequency is required.");
        }
        if (request.get("serviceMonth") == null) {
            throw new IllegalArgumentException("Service Month is required.");
        }
        if (request.get("hoursRequested") == null) {
            throw new IllegalArgumentException("Hours Requested is required.");
        }

        LocalDate serviceMonth = LocalDate.parse((String) request.get("serviceMonth"));

        // Validate: service month not more than 3 calendar months prior
        LocalDate threeMonthsAgo = LocalDate.now().withDayOfMonth(1).minusMonths(3);
        if (serviceMonth.isBefore(threeMonthsAgo)) {
            throw new IllegalArgumentException(
                    "Service Month cannot be more than 3 calendar months prior to the current month.");
        }

        // Validate: service month not in the future (beyond current month)
        LocalDate nextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        if (!serviceMonth.isBefore(nextMonth)) {
            throw new IllegalArgumentException("Service Month cannot be in the future.");
        }

        int hoursRequested = ((Number) request.get("hoursRequested")).intValue();
        if (hoursRequested <= 0) {
            throw new IllegalArgumentException("Hours Requested must be greater than zero.");
        }

        // Validate 80-hr cap: existing approved + requested must not exceed 4800 minutes
        int alreadyApproved = flexibleHoursRepository.sumApprovedMinutesByMonth(caseId, serviceMonth);
        if (alreadyApproved + hoursRequested > MAX_MINUTES_PER_MONTH) {
            int remaining = MAX_MINUTES_PER_MONTH - alreadyApproved;
            throw new RuntimeException(String.format(
                    "Requested hours would exceed the 80-hour monthly limit. " +
                    "Currently approved: %d minutes. Remaining: %d minutes.",
                    alreadyApproved, Math.max(0, remaining)));
        }

        FlexibleHoursEntity fh = new FlexibleHoursEntity();
        fh.setCaseId(caseId);

        try {
            fh.setFrequency(Frequency.valueOf((String) request.get("frequency")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Frequency: " + request.get("frequency"));
        }

        fh.setServiceMonth(serviceMonth);
        fh.setHoursRequested(hoursRequested);
        fh.setReason((String) request.get("reason"));
        fh.setStatus(FlexStatus.PENDING);

        if (request.get("program") != null) {
            try { fh.setProgram(Program.valueOf((String) request.get("program"))); }
            catch (Exception ignored) {}
        }

        if (request.get("endDate") != null) {
            fh.setEndDate(LocalDate.parse((String) request.get("endDate")));
        }

        fh.setCreatedBy(createdBy);
        fh.setUpdatedBy(createdBy);

        FlexibleHoursEntity saved = flexibleHoursRepository.save(fh);
        log.info("[FlexHours] Created: caseId={}, id={}, serviceMonth={}, minutes={}",
                caseId, saved.getId(), serviceMonth, hoursRequested);
        return saved;
    }

    @Transactional
    public FlexibleHoursEntity approveFlexibleHours(Long id, Integer approvedMinutes, String approvedBy) {
        FlexibleHoursEntity fh = getOrThrow(id);
        if (fh.getStatus() != FlexStatus.PENDING) {
            throw new RuntimeException("Only PENDING flexible hours requests can be approved.");
        }

        int minutesToApprove = approvedMinutes != null ? approvedMinutes : fh.getHoursRequested();

        // Validate 80-hr cap on approval too
        int alreadyApproved = flexibleHoursRepository.sumApprovedMinutesByMonth(fh.getCaseId(), fh.getServiceMonth());
        if (alreadyApproved + minutesToApprove > MAX_MINUTES_PER_MONTH) {
            throw new RuntimeException("Approving these hours would exceed the 80-hour monthly limit.");
        }

        fh.setApprovedHours(minutesToApprove);
        fh.setStatus(FlexStatus.APPROVED);
        fh.setApprovedDate(LocalDate.now());
        fh.setApprovedBy(approvedBy);
        fh.setUpdatedBy(approvedBy);

        log.info("[FlexHours] Approved: id={}, minutes={}", id, minutesToApprove);
        return flexibleHoursRepository.save(fh);
    }

    @Transactional
    public FlexibleHoursEntity denyFlexibleHours(Long id, String deniedBy) {
        FlexibleHoursEntity fh = getOrThrow(id);
        if (fh.getStatus() != FlexStatus.PENDING) {
            throw new RuntimeException("Only PENDING flexible hours requests can be denied.");
        }
        fh.setStatus(FlexStatus.DENIED);
        fh.setUpdatedBy(deniedBy);
        log.info("[FlexHours] Denied: id={}", id);
        return flexibleHoursRepository.save(fh);
    }

    @Transactional
    public FlexibleHoursEntity cancelFlexibleHours(Long id, String cancelledBy) {
        FlexibleHoursEntity fh = getOrThrow(id);
        if (fh.getStatus() != FlexStatus.PENDING) {
            throw new RuntimeException("Only PENDING flexible hours requests can be cancelled.");
        }
        fh.setStatus(FlexStatus.CANCELLED);
        fh.setUpdatedBy(cancelledBy);
        log.info("[FlexHours] Cancelled: id={}", id);
        return flexibleHoursRepository.save(fh);
    }

    private FlexibleHoursEntity getOrThrow(Long id) {
        return flexibleHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flexible hours record not found: " + id));
    }
}
