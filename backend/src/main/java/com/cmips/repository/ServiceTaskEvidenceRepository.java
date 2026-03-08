package com.cmips.repository;

import com.cmips.entity.ServiceTaskEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTaskEvidenceRepository extends JpaRepository<ServiceTaskEvidenceEntity, Long> {
    List<ServiceTaskEvidenceEntity> findByServiceTypeEvidenceId(Long serviceTypeEvidenceId);
    void deleteByServiceTypeEvidenceId(Long serviceTypeEvidenceId);
}
