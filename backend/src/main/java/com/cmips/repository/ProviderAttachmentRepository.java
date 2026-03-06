package com.cmips.repository;

import com.cmips.entity.ProviderAttachmentEntity;
import com.cmips.entity.ProviderAttachmentEntity.AttachmentStatus;
import com.cmips.entity.ProviderAttachmentEntity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProviderAttachmentRepository extends JpaRepository<ProviderAttachmentEntity, Long> {

    List<ProviderAttachmentEntity> findByProviderIdOrderByUploadDateDesc(Long providerId);

    List<ProviderAttachmentEntity> findByProviderIdAndStatus(Long providerId, AttachmentStatus status);

    List<ProviderAttachmentEntity> findByProviderIdAndDocumentType(Long providerId, DocumentType documentType);

    // Active attachments (not archived or deleted)
    @Query("SELECT a FROM ProviderAttachmentEntity a WHERE a.providerId = :providerId AND a.status = 'ACTIVE'")
    List<ProviderAttachmentEntity> findActiveByProviderId(@Param("providerId") Long providerId);

    // Attachments uploaded today that can still be restored (same-day rule)
    @Query("SELECT a FROM ProviderAttachmentEntity a WHERE a.providerId = :providerId " +
           "AND a.status = 'ARCHIVED' AND a.archivedDate = :today")
    List<ProviderAttachmentEntity> findArchivedTodayByProviderId(
            @Param("providerId") Long providerId,
            @Param("today") LocalDate today);

    // For nightly batch archival — all ACTIVE attachments not uploaded today
    @Query("SELECT a FROM ProviderAttachmentEntity a WHERE a.status = 'ACTIVE' AND a.uploadDate < :today")
    List<ProviderAttachmentEntity> findActiveForNightlyArchival(@Param("today") LocalDate today);
}
