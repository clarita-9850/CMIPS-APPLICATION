package com.cmips.repository;

import com.cmips.entity.SickLeaveClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SickLeaveClaimRepository extends JpaRepository<SickLeaveClaimEntity, Long> {

    List<SickLeaveClaimEntity> findByProviderIdAndStatusOrderByClaimEnteredDateDesc(Long providerId, String status);

    List<SickLeaveClaimEntity> findByProviderIdOrderByClaimEnteredDateDesc(Long providerId);

    Optional<SickLeaveClaimEntity> findByClaimNumber(String claimNumber);
}
