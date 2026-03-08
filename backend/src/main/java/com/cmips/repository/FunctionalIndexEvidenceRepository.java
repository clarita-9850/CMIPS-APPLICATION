package com.cmips.repository;

import com.cmips.entity.FunctionalIndexEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FunctionalIndexEvidenceRepository extends JpaRepository<FunctionalIndexEvidenceEntity, Long> {
    List<FunctionalIndexEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
    Optional<FunctionalIndexEvidenceEntity> findFirstByAssessmentEvidenceId(Long assessmentEvidenceId);
}
