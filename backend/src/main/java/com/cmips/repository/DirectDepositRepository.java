package com.cmips.repository;

import com.cmips.entity.DirectDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectDepositRepository extends JpaRepository<DirectDepositEntity, Long> {

    List<DirectDepositEntity> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<DirectDepositEntity> findByProviderIdAndStatus(Long providerId, String status);

    List<DirectDepositEntity> findByPrenoteStatus(String prenoteStatus);
}
