package com.cmips.repository;

import com.cmips.entity.FormHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormHistoryRepository extends JpaRepository<FormHistoryEntity, Long> {

    List<FormHistoryEntity> findByNoaIdOrderByCreatedAtDesc(Long noaId);

    List<FormHistoryEntity> findByFormIdOrderByCreatedAtDesc(Long formId);

    List<FormHistoryEntity> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}
