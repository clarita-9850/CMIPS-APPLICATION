package com.cmips.repository;

import com.cmips.entity.PayrollBatchRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollBatchRunRepository extends JpaRepository<PayrollBatchRunEntity, Long> {

    List<PayrollBatchRunEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<PayrollBatchRunEntity> findByPayPeriodBeginDateAndCountyCode(
            LocalDate payPeriodBeginDate, String countyCode);

    Optional<PayrollBatchRunEntity> findByBatchNumber(String batchNumber);

    List<PayrollBatchRunEntity> findTop10ByOrderByCreatedAtDesc();
}
