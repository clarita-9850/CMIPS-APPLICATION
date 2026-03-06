package com.cmips.repository;

import com.cmips.entity.NoticeOfActionEntity;
import com.cmips.entity.NoticeOfActionEntity.NoaStatus;
import com.cmips.entity.NoticeOfActionEntity.NoaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeOfActionRepository extends JpaRepository<NoticeOfActionEntity, Long> {

    // All NOAs for a case, newest first
    @Query("SELECT n FROM NoticeOfActionEntity n WHERE n.caseId = :caseId ORDER BY n.requestDate DESC")
    List<NoticeOfActionEntity> findByCaseIdOrderByRequestDateDesc(@Param("caseId") Long caseId);

    // NOAs by type for a case
    List<NoticeOfActionEntity> findByCaseIdAndNoaType(Long caseId, NoaType noaType);

    // Pending NOAs for batch print
    @Query("SELECT n FROM NoticeOfActionEntity n WHERE n.status = 'PENDING' ORDER BY n.requestDate ASC")
    List<NoticeOfActionEntity> findPendingForBatchPrint();

    // NOAs for a recipient
    List<NoticeOfActionEntity> findByRecipientId(Long recipientId);

    // Count by status for a case
    long countByCaseIdAndStatus(Long caseId, NoaStatus status);
}
