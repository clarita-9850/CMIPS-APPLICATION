package com.cmips.repository;

import com.cmips.entity.ShareOfCostEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareOfCostEvidenceRepository extends JpaRepository<ShareOfCostEvidenceEntity, Long> {
    List<ShareOfCostEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
    Optional<ShareOfCostEvidenceEntity> findFirstByAssessmentEvidenceId(Long assessmentEvidenceId);
}
