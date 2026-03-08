package com.cmips.repository;

import com.cmips.entity.IHSSAuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IHSSAuthorizationRepository extends JpaRepository<IHSSAuthorizationEntity, Long> {
    List<IHSSAuthorizationEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
    List<IHSSAuthorizationEntity> findByAssessmentId(Long assessmentId);
    List<IHSSAuthorizationEntity> findByCaseIdAndActiveAuthorizationInd(Long caseId, Boolean active);
    Optional<IHSSAuthorizationEntity> findByCaseIdAndStatusCode(Long caseId, String statusCode);
}
