package com.cmips.repository;

import com.cmips.entity.IncomeEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeEvidenceRepository extends JpaRepository<IncomeEvidenceEntity, Long> {
    List<IncomeEvidenceEntity> findByShareOfCostEvidenceId(Long shareOfCostEvidenceId);
}
