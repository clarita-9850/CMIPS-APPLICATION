package com.cmips.repository;

import com.cmips.entity.CountyContractorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CountyContractorRepository extends JpaRepository<CountyContractorEntity, Long> {

    List<CountyContractorEntity> findByCountyCodeOrderByFromDateDesc(String countyCode);

    /**
     * Rate overlap check (DSD EM 16).
     * Finds rates for the same county and contractor name that overlap with the given date range.
     */
    @Query("SELECT c FROM CountyContractorEntity c WHERE c.countyCode = :county " +
           "AND c.contractorName = :name AND c.id != :excludeId " +
           "AND c.fromDate <= :toDate AND (c.toDate IS NULL OR c.toDate >= :fromDate)")
    List<CountyContractorEntity> findOverlappingRates(
            @Param("county") String county,
            @Param("name") String name,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("excludeId") Long excludeId);
}
