package com.cmips.repository;

import com.cmips.entity.CaseAttachmentEntity;
import com.cmips.entity.CaseAttachmentEntity.AttachmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseAttachmentRepository extends JpaRepository<CaseAttachmentEntity, Long> {

    List<CaseAttachmentEntity> findByCaseIdOrderByUploadDateDesc(Long caseId);

    List<CaseAttachmentEntity> findByCaseIdAndStatusOrderByUploadDateDesc(Long caseId, AttachmentStatus status);
}
