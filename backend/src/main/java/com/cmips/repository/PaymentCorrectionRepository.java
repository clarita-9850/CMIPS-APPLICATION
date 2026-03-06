package com.cmips.repository;

import com.cmips.entity.PaymentCorrectionEntity;
import com.cmips.entity.PaymentCorrectionEntity.CorrectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCorrectionRepository extends JpaRepository<PaymentCorrectionEntity, Long> {
    List<PaymentCorrectionEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
    List<PaymentCorrectionEntity> findByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, CorrectionStatus status);
    List<PaymentCorrectionEntity> findByStatusOrderByCreatedAtDesc(CorrectionStatus status);
    List<PaymentCorrectionEntity> findByProviderIdOrderByCreatedAtDesc(String providerId);
}
