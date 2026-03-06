package com.cmips.repository;

import com.cmips.entity.CCInvoiceDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CCInvoiceDetailsRepository extends JpaRepository<CCInvoiceDetailsEntity, Long> {

    List<CCInvoiceDetailsEntity> findByCountyContractorInvoiceId(Long countyContractorInvoiceId);
}
