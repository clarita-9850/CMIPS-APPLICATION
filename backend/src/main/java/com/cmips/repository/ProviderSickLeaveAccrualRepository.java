package com.cmips.repository;

import com.cmips.entity.ProviderSickLeaveAccrualEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderSickLeaveAccrualRepository extends JpaRepository<ProviderSickLeaveAccrualEntity, Long> {
    List<ProviderSickLeaveAccrualEntity> findByProviderIdOrderByAccrualYearDesc(Long providerId);
    Optional<ProviderSickLeaveAccrualEntity> findByProviderIdAndAccrualYear(Long providerId, Integer accrualYear);
    List<ProviderSickLeaveAccrualEntity> findByStatus(String status);
}
