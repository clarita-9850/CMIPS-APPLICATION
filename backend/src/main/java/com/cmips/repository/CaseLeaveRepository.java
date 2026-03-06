package com.cmips.repository;

import com.cmips.entity.CaseLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseLeaveRepository extends JpaRepository<CaseLeave, Long> {
    List<CaseLeave> findByCaseIdOrderByLeaveDateDesc(Long caseId);
}
