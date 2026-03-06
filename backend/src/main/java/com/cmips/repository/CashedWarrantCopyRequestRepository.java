package com.cmips.repository;

import com.cmips.entity.CashedWarrantCopyRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashedWarrantCopyRequestRepository extends JpaRepository<CashedWarrantCopyRequestEntity, Long> {
    List<CashedWarrantCopyRequestEntity> findByWarrantIdOrderByCreatedAtDesc(Long warrantId);
    List<CashedWarrantCopyRequestEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}
