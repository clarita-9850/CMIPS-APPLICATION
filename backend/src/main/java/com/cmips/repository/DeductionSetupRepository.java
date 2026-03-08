package com.cmips.repository;

import com.cmips.entity.DeductionSetupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeductionSetupRepository extends JpaRepository<DeductionSetupEntity, Long> {

    List<DeductionSetupEntity> findByProviderIdOrderByPriorityAsc(Long providerId);

    List<DeductionSetupEntity> findByProviderIdAndStatus(Long providerId, String status);

    List<DeductionSetupEntity> findByProviderIdAndDeductionType(Long providerId, String deductionType);

    List<DeductionSetupEntity> findByDeductionTypeAndStatus(String deductionType, String status);
}
