package com.cmips.repository;

import com.cmips.entity.ModeOfServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModeOfServiceRepository extends JpaRepository<ModeOfServiceEntity, Long> {
    List<ModeOfServiceEntity> findByCaseIdOrderByModeOfServiceStartDateDesc(Long caseId);
    List<ModeOfServiceEntity> findByIhssAuthorizationId(Long authId);
    List<ModeOfServiceEntity> findByCaseIdAndStatusCode(Long caseId, String statusCode);
}
