package com.cmips.repository;

import com.cmips.entity.OverpaymentEntity;
import com.cmips.entity.OverpaymentEntity.OverpaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverpaymentRepository extends JpaRepository<OverpaymentEntity, Long> {
    List<OverpaymentEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
    List<OverpaymentEntity> findByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, OverpaymentStatus status);
    List<OverpaymentEntity> findByStatusInOrderByCreatedAtDesc(List<OverpaymentStatus> statuses);
    List<OverpaymentEntity> findByOverpaidPayeeIdOrderByCreatedAtDesc(String payeeId);
    List<OverpaymentEntity> findByRecoveryPayeeIdOrderByCreatedAtDesc(String payeeId);
}
