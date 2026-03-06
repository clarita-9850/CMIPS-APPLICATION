package com.cmips.repository;

import com.cmips.entity.DisasterPreparednessContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisasterPreparednessContactRepository extends JpaRepository<DisasterPreparednessContactEntity, Long> {

    List<DisasterPreparednessContactEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    List<DisasterPreparednessContactEntity> findByCaseIdAndStatusOrderByCreatedAtDesc(Long caseId, String status);
}
