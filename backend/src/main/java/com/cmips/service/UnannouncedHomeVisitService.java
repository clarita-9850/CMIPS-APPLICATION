package com.cmips.service;

import com.cmips.entity.UnannouncedHomeVisitEntity;
import com.cmips.entity.UnannouncedHomeVisitEntity.VisitOutcome;
import com.cmips.entity.UnannouncedHomeVisitEntity.VisitType;
import com.cmips.repository.UnannouncedHomeVisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Unannounced Home Visit Service — DSD Section 25, CI-718079
 *
 * AB 19 Section 12305.7(A) — Records and tracks unannounced home visits.
 * Follow-up required when initial visit is unsuccessful.
 * Final unsuccessful outcome may trigger case termination.
 */
@Service
public class UnannouncedHomeVisitService {

    private static final Logger log = LoggerFactory.getLogger(UnannouncedHomeVisitService.class);

    private final UnannouncedHomeVisitRepository visitRepository;

    public UnannouncedHomeVisitService(UnannouncedHomeVisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public List<UnannouncedHomeVisitEntity> getVisits(Long caseId) {
        return visitRepository.findByCaseIdOrderByVisitDateDesc(caseId);
    }

    public UnannouncedHomeVisitEntity getVisit(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Home visit not found: " + id));
    }

    @Transactional
    public UnannouncedHomeVisitEntity createVisit(Long caseId, Map<String, Object> request,
                                                   String createdBy) {
        if (request.get("visitDate") == null) {
            throw new IllegalArgumentException("Visit Date is required.");
        }
        if (request.get("visitType") == null) {
            throw new IllegalArgumentException("Visit Type is required.");
        }

        LocalDate visitDate = LocalDate.parse((String) request.get("visitDate"));
        if (visitDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Visit Date cannot be in the future.");
        }

        UnannouncedHomeVisitEntity visit = new UnannouncedHomeVisitEntity();
        visit.setCaseId(caseId);
        visit.setVisitDate(visitDate);
        visit.setVisitTime((String) request.get("visitTime"));

        try {
            visit.setVisitType(VisitType.valueOf((String) request.get("visitType")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Visit Type: " + request.get("visitType"));
        }

        if (request.get("outcome") != null) {
            try {
                visit.setOutcome(VisitOutcome.valueOf((String) request.get("outcome")));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid Outcome: " + request.get("outcome"));
            }
        }

        visit.setReasonForVisit((String) request.get("reasonForVisit"));
        visit.setFindings((String) request.get("findings"));

        if (request.get("followUpRequired") != null) {
            visit.setFollowUpRequired((Boolean) request.get("followUpRequired"));
        }

        visit.setTerminationTriggered(false);
        visit.setCreatedBy(createdBy);
        visit.setUpdatedBy(createdBy);

        UnannouncedHomeVisitEntity saved = visitRepository.save(visit);
        log.info("[UHV] Recorded visit: caseId={}, date={}, type={}, outcome={}",
                caseId, visitDate, visit.getVisitType(), visit.getOutcome());
        return saved;
    }
}
