package com.cmips.repository;

import com.cmips.entity.ProviderEnrollmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderEnrollmentHistoryRepository extends JpaRepository<ProviderEnrollmentHistory, Long> {

    List<ProviderEnrollmentHistory> findByProviderIdOrderByChangedAtDesc(Long providerId);

    Optional<ProviderEnrollmentHistory> findTopByProviderIdAndEligibleOrderByChangedAtDesc(Long providerId, String eligible);
}
