package com.cmips.repository;

import com.cmips.entity.CaseParticipantServiceMonthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CaseParticipantServiceMonthRepository extends JpaRepository<CaseParticipantServiceMonthEntity, Long> {
    List<CaseParticipantServiceMonthEntity> findByCaseServiceMonthId(Long caseServiceMonthId);
    List<CaseParticipantServiceMonthEntity> findByCaseServiceMonthIdAndEmployeeId(Long csmId, Long employeeId);
}
