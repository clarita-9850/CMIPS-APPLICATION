package com.cmips.repository;

import com.cmips.entity.TargetedMailingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetedMailingRepository extends JpaRepository<TargetedMailingEntity, Long> {

    List<TargetedMailingEntity> findByStatusOrderByScheduledDateAsc(String status);

    List<TargetedMailingEntity> findByCountyCodeOrderByCreatedAtDesc(String countyCode);

    List<TargetedMailingEntity> findByStatus(String status);
}
