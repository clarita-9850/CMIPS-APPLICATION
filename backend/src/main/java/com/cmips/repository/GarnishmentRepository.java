package com.cmips.repository;

import com.cmips.entity.GarnishmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarnishmentRepository extends JpaRepository<GarnishmentEntity, Long> {

    List<GarnishmentEntity> findByProviderIdOrderByPriorityAsc(Long providerId);

    List<GarnishmentEntity> findByProviderIdAndStatus(Long providerId, String status);

    List<GarnishmentEntity> findByStatus(String status);

    List<GarnishmentEntity> findByGarnishmentTypeAndStatus(String garnishmentType, String status);
}
