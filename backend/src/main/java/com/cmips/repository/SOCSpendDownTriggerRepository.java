package com.cmips.repository;

import com.cmips.entity.SOCSpendDownTriggerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SOCSpendDownTriggerRepository extends JpaRepository<SOCSpendDownTriggerEntity, Long> {
    List<SOCSpendDownTriggerEntity> findByCaseIdOrderByTriggerDateDesc(Long caseId);
    List<SOCSpendDownTriggerEntity> findByCaseIdAndServiceMonth(Long caseId, LocalDate serviceMonth);
    List<SOCSpendDownTriggerEntity> findByStatusCode(String statusCode);
    List<SOCSpendDownTriggerEntity> findByCaseIdAndStatusCode(Long caseId, String statusCode);
}
