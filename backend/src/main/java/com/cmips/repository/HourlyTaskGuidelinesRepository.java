package com.cmips.repository;

import com.cmips.entity.HourlyTaskGuidelinesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HourlyTaskGuidelinesRepository extends JpaRepository<HourlyTaskGuidelinesEntity, Long> {
    List<HourlyTaskGuidelinesEntity> findByServiceTypeCode(String serviceTypeCode);
    List<HourlyTaskGuidelinesEntity> findByServiceTypeCodeAndFuncAreaCode(String serviceTypeCode, String funcAreaCode);
    Optional<HourlyTaskGuidelinesEntity> findByServiceTypeCodeAndFuncAreaCodeAndFuncRank(String serviceTypeCode, String funcAreaCode, String funcRank);
}
