package com.cmips.repository;

import com.cmips.entity.AuthorizationSegmentEntity;
import com.cmips.entity.AuthorizationSegmentEntity.SegmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorizationSegmentRepository extends JpaRepository<AuthorizationSegmentEntity, Long> {

    List<AuthorizationSegmentEntity> findByCaseIdOrderBySegmentStartDateDesc(Long caseId);

    List<AuthorizationSegmentEntity> findByCaseIdAndStatusOrderBySegmentStartDateDesc(Long caseId, SegmentStatus status);

    Optional<AuthorizationSegmentEntity> findFirstByCaseIdAndStatusOrderBySegmentStartDateDesc(Long caseId, SegmentStatus status);

    List<AuthorizationSegmentEntity> findByAssessmentIdOrderBySegmentStartDateDesc(Long assessmentId);
}
