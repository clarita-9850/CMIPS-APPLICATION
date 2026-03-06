package com.cmips.repository;

import com.cmips.entity.CountyPayRateEntity;
import com.cmips.entity.CountyPayRateEntity.RateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CountyPayRateRepository extends JpaRepository<CountyPayRateEntity, Long> {

    List<CountyPayRateEntity> findByCountyCodeOrderByEffectiveDateDesc(String countyCode);

    List<CountyPayRateEntity> findByStatusOrderByCountyCodeAscEffectiveDateDesc(String status);

    /** Find the current active rate for a county and rate type as of a given date */
    @Query("SELECT r FROM CountyPayRateEntity r WHERE r.countyCode = :countyCode " +
           "AND r.rateType = :rateType AND r.effectiveDate <= :asOf " +
           "AND (r.endDate IS NULL OR r.endDate >= :asOf) " +
           "AND r.status = 'ACTIVE' ORDER BY r.effectiveDate DESC")
    Optional<CountyPayRateEntity> findCurrentRate(
            @Param("countyCode") String countyCode,
            @Param("rateType") RateType rateType,
            @Param("asOf") LocalDate asOf);

    List<CountyPayRateEntity> findAllByOrderByCountyCodeAscEffectiveDateDesc();
}
