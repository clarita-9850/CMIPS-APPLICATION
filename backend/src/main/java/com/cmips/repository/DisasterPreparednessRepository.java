package com.cmips.repository;

import com.cmips.entity.DisasterPreparednessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisasterPreparednessRepository extends JpaRepository<DisasterPreparednessEntity, Long> {
    List<DisasterPreparednessEntity> findByCaseId(Long caseId);
    List<DisasterPreparednessEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
    Optional<DisasterPreparednessEntity> findByCaseIdAndStatus(Long caseId, String status);
}
