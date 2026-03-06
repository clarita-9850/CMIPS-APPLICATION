package com.cmips.repository;

import com.cmips.entity.UnannouncedHomeVisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnannouncedHomeVisitRepository extends JpaRepository<UnannouncedHomeVisitEntity, Long> {

    List<UnannouncedHomeVisitEntity> findByCaseIdOrderByVisitDateDesc(Long caseId);
}
