package com.cmips.repository;

import com.cmips.entity.VoidReissueRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoidReissueRequestRepository extends JpaRepository<VoidReissueRequestEntity, Long> {
    List<VoidReissueRequestEntity> findByWarrantIdOrderByCreatedAtDesc(Long warrantId);
    List<VoidReissueRequestEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}
