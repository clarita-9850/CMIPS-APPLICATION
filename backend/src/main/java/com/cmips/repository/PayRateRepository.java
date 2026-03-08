package com.cmips.repository;

import com.cmips.entity.PayRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayRateRepository extends JpaRepository<PayRateEntity, Long> {

    List<PayRateEntity> findByCountyCodeAndStatusOrderByEffectiveDateDesc(String countyCode, String status);

    List<PayRateEntity> findByCountyCodeAndRateTypeAndStatus(String countyCode, String rateType, String status);

    Optional<PayRateEntity> findFirstByCountyCodeAndRateTypeAndStatusOrderByEffectiveDateDesc(
            String countyCode, String rateType, String status);
}
