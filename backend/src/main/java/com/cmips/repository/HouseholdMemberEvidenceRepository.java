package com.cmips.repository;

import com.cmips.entity.HouseholdMemberEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseholdMemberEvidenceRepository extends JpaRepository<HouseholdMemberEvidenceEntity, Long> {
    List<HouseholdMemberEvidenceEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
    List<HouseholdMemberEvidenceEntity> findByCompanionCaseId(Long companionCaseId);
}
