package com.cmips.repository;

import com.cmips.entity.SAWSNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SAWSNotificationRepository extends JpaRepository<SAWSNotificationEntity, Long> {
    List<SAWSNotificationEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
    List<SAWSNotificationEntity> findByAssessmentEvidenceId(Long assessmentEvidenceId);
}
