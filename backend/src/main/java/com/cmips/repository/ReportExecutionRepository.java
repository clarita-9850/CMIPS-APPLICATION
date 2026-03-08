package com.cmips.repository;

import com.cmips.entity.ReportExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecutionEntity, Long> {

    List<ReportExecutionEntity> findByReportDefinitionIdOrderByCreatedAtDesc(Long reportDefinitionId);

    List<ReportExecutionEntity> findByRequestedByOrderByCreatedAtDesc(String requestedBy);

    List<ReportExecutionEntity> findByStatus(String status);

    List<ReportExecutionEntity> findTop20ByOrderByCreatedAtDesc();
}
