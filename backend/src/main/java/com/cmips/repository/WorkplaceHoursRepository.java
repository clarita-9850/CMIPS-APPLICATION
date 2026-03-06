package com.cmips.repository;

import com.cmips.entity.WorkplaceHoursEntity;
import com.cmips.entity.WorkplaceHoursEntity.HoursStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkplaceHoursRepository extends JpaRepository<WorkplaceHoursEntity, Long> {

    List<WorkplaceHoursEntity> findByCaseIdOrderByBeginDateDesc(Long caseId);

    List<WorkplaceHoursEntity> findByCaseIdAndStatusOrderByBeginDateDesc(Long caseId, HoursStatus status);
}
