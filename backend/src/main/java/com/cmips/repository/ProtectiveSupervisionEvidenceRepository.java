package com.cmips.repository;

import com.cmips.entity.ProtectiveSupervisionEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProtectiveSupervisionEvidenceRepository extends JpaRepository<ProtectiveSupervisionEvidenceEntity, Long> {
    Optional<ProtectiveSupervisionEvidenceEntity> findByServiceTypeEvidenceId(Long serviceTypeEvidenceId);
    List<ProtectiveSupervisionEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
}
