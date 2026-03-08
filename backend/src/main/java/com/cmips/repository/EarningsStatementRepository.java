package com.cmips.repository;

import com.cmips.entity.EarningsStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EarningsStatementRepository extends JpaRepository<EarningsStatementEntity, Long> {

    List<EarningsStatementEntity> findByProviderIdOrderByPayPeriodBeginDateDesc(Long providerId);

    Optional<EarningsStatementEntity> findByWarrantId(Long warrantId);

    List<EarningsStatementEntity> findByProviderIdAndPayPeriodBeginDate(
            Long providerId, LocalDate payPeriodBeginDate);
}
