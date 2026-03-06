package com.cmips.repository;

import com.cmips.entity.FPOEligibilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FPOEligibilityRepository extends JpaRepository<FPOEligibilityEntity, Long> {

    List<FPOEligibilityEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    @Query("SELECT f FROM FPOEligibilityEntity f WHERE f.caseId = :caseId AND f.status = 'ACTIVE' ORDER BY f.createdAt DESC")
    Optional<FPOEligibilityEntity> findActiveByCaseId(@Param("caseId") Long caseId);

    @Query("SELECT f FROM FPOEligibilityEntity f WHERE f.caseId = :caseId ORDER BY f.createdAt DESC")
    List<FPOEligibilityEntity> findHistoryByCaseId(@Param("caseId") Long caseId);
}
