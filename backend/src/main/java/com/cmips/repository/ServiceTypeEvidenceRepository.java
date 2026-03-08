package com.cmips.repository;

import com.cmips.entity.ServiceTypeEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceTypeEvidenceRepository extends JpaRepository<ServiceTypeEvidenceEntity, Long> {
    List<ServiceTypeEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
    Optional<ServiceTypeEvidenceEntity> findByAssessmentEvidenceIdAndServiceTypeCode(Long assessmentEvidenceId, String serviceTypeCode);
}
