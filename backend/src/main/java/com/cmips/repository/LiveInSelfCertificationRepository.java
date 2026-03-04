package com.cmips.repository;

import com.cmips.entity.LiveInSelfCertificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveInSelfCertificationRepository extends JpaRepository<LiveInSelfCertificationEntity, Long> {

    List<LiveInSelfCertificationEntity> findByProviderIdOrderByStatusDateDesc(Long providerId);

    List<LiveInSelfCertificationEntity> findByCaseIdOrderByStatusDateDesc(Long caseId);

    Optional<LiveInSelfCertificationEntity> findTopByProviderIdAndCaseIdOrderByStatusDateDesc(Long providerId, Long caseId);
}
