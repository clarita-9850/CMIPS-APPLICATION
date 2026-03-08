package com.cmips.repository;

import com.cmips.entity.DeathMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeathMatchRepository extends JpaRepository<DeathMatchEntity, Long> {

    List<DeathMatchEntity> findByVerificationStatusOrderByMatchDateDesc(String verificationStatus);

    List<DeathMatchEntity> findByPersonIdAndPersonType(Long personId, String personType);

    List<DeathMatchEntity> findByMatchSourceAndVerificationStatus(String matchSource, String verificationStatus);

    List<DeathMatchEntity> findByVerificationStatusAndActionTaken(String verificationStatus, String actionTaken);
}
