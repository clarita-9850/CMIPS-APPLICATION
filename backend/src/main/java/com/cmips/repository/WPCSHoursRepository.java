package com.cmips.repository;

import com.cmips.entity.WPCSHoursEntity;
import com.cmips.entity.WPCSHoursEntity.HoursStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WPCSHoursRepository extends JpaRepository<WPCSHoursEntity, Long> {

    List<WPCSHoursEntity> findByCaseIdOrderByBeginDateDesc(Long caseId);

    List<WPCSHoursEntity> findByCaseIdAndStatusOrderByBeginDateDesc(Long caseId, HoursStatus status);
}
