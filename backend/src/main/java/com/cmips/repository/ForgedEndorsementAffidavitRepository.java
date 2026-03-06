package com.cmips.repository;

import com.cmips.entity.ForgedEndorsementAffidavitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForgedEndorsementAffidavitRepository extends JpaRepository<ForgedEndorsementAffidavitEntity, Long> {
    List<ForgedEndorsementAffidavitEntity> findByWarrantIdOrderByCreatedAtDesc(Long warrantId);
    List<ForgedEndorsementAffidavitEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}
