package com.cmips.repository;

import com.cmips.entity.FraudCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudCaseRepository extends JpaRepository<FraudCaseEntity, Long> {

    List<FraudCaseEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    List<FraudCaseEntity> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<FraudCaseEntity> findByInvestigationStatus(String investigationStatus);

    List<FraudCaseEntity> findByInvestigatorIdAndInvestigationStatusNot(String investigatorId, String excludeStatus);

    List<FraudCaseEntity> findByFraudTypeAndInvestigationStatus(String fraudType, String investigationStatus);
}
