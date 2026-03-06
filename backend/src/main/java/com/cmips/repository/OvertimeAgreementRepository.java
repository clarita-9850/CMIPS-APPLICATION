package com.cmips.repository;

import com.cmips.entity.OvertimeAgreementEntity;
import com.cmips.entity.OvertimeAgreementEntity.AgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OvertimeAgreementRepository extends JpaRepository<OvertimeAgreementEntity, Long> {

    List<OvertimeAgreementEntity> findByCaseIdOrderByDateReceivedDesc(Long caseId);

    List<OvertimeAgreementEntity> findByCaseIdAndStatusOrderByDateReceivedDesc(Long caseId, AgreementStatus status);
}
