package com.cmips.repository;

import com.cmips.entity.ProgramEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramEvidenceRepository extends JpaRepository<ProgramEvidenceEntity, Long> {

    List<ProgramEvidenceEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    List<ProgramEvidenceEntity> findByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, String status);

    Optional<ProgramEvidenceEntity> findFirstByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, String status);
}
