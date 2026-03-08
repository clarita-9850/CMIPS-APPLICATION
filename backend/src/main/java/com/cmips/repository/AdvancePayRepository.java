package com.cmips.repository;

import com.cmips.entity.AdvancePayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvancePayRepository extends JpaRepository<AdvancePayEntity, Long> {

    List<AdvancePayEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    List<AdvancePayEntity> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<AdvancePayEntity> findByStatus(String status);

    List<AdvancePayEntity> findByCaseIdAndStatus(Long caseId, String status);
}
