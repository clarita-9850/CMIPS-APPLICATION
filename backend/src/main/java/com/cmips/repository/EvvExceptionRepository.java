package com.cmips.repository;

import com.cmips.entity.EvvExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvvExceptionRepository extends JpaRepository<EvvExceptionEntity, Long> {

    List<EvvExceptionEntity> findByTimesheetIdOrderByServiceDateAsc(Long timesheetId);

    List<EvvExceptionEntity> findByProviderIdOrderByServiceDateDesc(Long providerId);

    List<EvvExceptionEntity> findByCaseIdOrderByServiceDateDesc(Long caseId);

    List<EvvExceptionEntity> findByStatusOrderByCreatedAtAsc(EvvExceptionEntity.EvvExceptionStatus status);

    List<EvvExceptionEntity> findByCountyCodeAndStatusOrderByCreatedAtAsc(
            String countyCode, EvvExceptionEntity.EvvExceptionStatus status);

    Optional<EvvExceptionEntity> findByExceptionNumber(String exceptionNumber);
}
