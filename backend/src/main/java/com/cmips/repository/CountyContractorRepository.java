package com.cmips.repository;

import com.cmips.entity.CountyContractorInvoiceEntity;
import com.cmips.entity.CountyContractorInvoiceEntity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountyContractorRepository extends JpaRepository<CountyContractorInvoiceEntity, Long> {

    List<CountyContractorInvoiceEntity> findByCaseIdOrderByInvoiceDateDesc(Long caseId);

    List<CountyContractorInvoiceEntity> findByCaseIdAndStatusOrderByInvoiceDateDesc(Long caseId, InvoiceStatus status);
}
