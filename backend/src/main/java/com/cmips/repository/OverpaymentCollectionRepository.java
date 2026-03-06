package com.cmips.repository;

import com.cmips.entity.OverpaymentCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OverpaymentCollectionRepository extends JpaRepository<OverpaymentCollectionEntity, Long> {
    List<OverpaymentCollectionEntity> findByOverpaymentIdOrderByCollectionDateDesc(Long overpaymentId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM OverpaymentCollectionEntity c WHERE c.overpaymentId = :overpaymentId")
    BigDecimal sumAmountByOverpaymentId(Long overpaymentId);
}
