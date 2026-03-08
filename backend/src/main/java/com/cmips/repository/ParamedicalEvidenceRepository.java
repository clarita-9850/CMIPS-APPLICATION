package com.cmips.repository;

import com.cmips.entity.ParamedicalEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParamedicalEvidenceRepository extends JpaRepository<ParamedicalEvidenceEntity, Long> {
    Optional<ParamedicalEvidenceEntity> findByServiceTypeEvidenceId(Long serviceTypeEvidenceId);
    List<ParamedicalEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
}
