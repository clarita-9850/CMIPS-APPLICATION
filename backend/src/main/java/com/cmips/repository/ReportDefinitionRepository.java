package com.cmips.repository;

import com.cmips.entity.ReportDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinitionEntity, Long> {

    List<ReportDefinitionEntity> findByReportCategory(String reportCategory);

    List<ReportDefinitionEntity> findByStatusOrderByReportNameAsc(String status);

    Optional<ReportDefinitionEntity> findByReportCode(String reportCode);

    List<ReportDefinitionEntity> findByScheduleEnabledAndStatus(Boolean scheduleEnabled, String status);
}
