package com.cmips.repository;

import com.cmips.entity.HouseholdEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HouseholdEvidenceRepository extends JpaRepository<HouseholdEvidenceEntity, Long> {
    List<HouseholdEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
    List<HouseholdEvidenceEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
    Optional<HouseholdEvidenceEntity> findFirstByAssessmentEvidenceId(Long assessmentEvidenceId);
}
