package com.cmips.repository;

import com.cmips.entity.SpecialTransactionEntity;
import com.cmips.entity.SpecialTransactionEntity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialTransactionRepository extends JpaRepository<SpecialTransactionEntity, Long> {
    List<SpecialTransactionEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
    List<SpecialTransactionEntity> findByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, TransactionStatus status);
    List<SpecialTransactionEntity> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    List<SpecialTransactionEntity> findByPayeeIdOrderByCreatedAtDesc(String payeeId);
}
