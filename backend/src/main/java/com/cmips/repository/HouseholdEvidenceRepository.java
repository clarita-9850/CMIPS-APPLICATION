package com.cmips.repository;

import com.cmips.entity.HouseholdEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdEvidenceRepository extends JpaRepository<HouseholdEvidenceEntity, Long> {

    List<HouseholdEvidenceEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    Optional<HouseholdEvidenceEntity> findFirstByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, String status);
}
