package com.cmips.repository;

import com.cmips.entity.CaseSOCHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseSOCHoursRepository extends JpaRepository<CaseSOCHoursEntity, Long> {
    List<CaseSOCHoursEntity> findByCaseIdOrderByServiceMonthDesc(Long caseId);
    Optional<CaseSOCHoursEntity> findByCaseIdAndServiceMonth(Long caseId, LocalDate serviceMonth);
}
