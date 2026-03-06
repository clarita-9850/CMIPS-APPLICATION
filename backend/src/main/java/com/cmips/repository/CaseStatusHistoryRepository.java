package com.cmips.repository;

import com.cmips.entity.CaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, Long> {
    List<CaseStatusHistory> findByCaseIdOrderByChangedAtDesc(Long caseId);
}
