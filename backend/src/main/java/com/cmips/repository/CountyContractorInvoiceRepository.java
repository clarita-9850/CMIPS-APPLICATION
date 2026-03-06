package com.cmips.repository;

import com.cmips.entity.CountyContractorInvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountyContractorInvoiceRepository extends JpaRepository<CountyContractorInvoiceEntity, Long> {

    List<CountyContractorInvoiceEntity> findByCountyContractorIdOrderByBillingMonthDesc(Long countyContractorId);

    List<CountyContractorInvoiceEntity> findByStatus(String status);
}
