package com.cmips.repository;

import com.cmips.entity.CaseServiceMonthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseServiceMonthRepository extends JpaRepository<CaseServiceMonthEntity, Long> {
    List<CaseServiceMonthEntity> findByCaseIdOrderByServiceMonthDesc(Long caseId);
    Optional<CaseServiceMonthEntity> findByCaseIdAndServiceMonth(Long caseId, LocalDate serviceMonth);
    List<CaseServiceMonthEntity> findByCaseIdAndStatusCode(Long caseId, String statusCode);
}
