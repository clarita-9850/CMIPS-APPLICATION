package com.cmips.repository;

import com.cmips.entity.CaseStatusRescind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseStatusRescindRepository extends JpaRepository<CaseStatusRescind, Long> {
    List<CaseStatusRescind> findByCaseIdOrderByRescindDateDesc(Long caseId);
}
