package com.cmips.repository;

import com.cmips.entity.RecipientEmailEntity;
import com.cmips.entity.RecipientEmailEntity.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientEmailRepository extends JpaRepository<RecipientEmailEntity, Long> {

    // All emails for a recipient, newest first
    List<RecipientEmailEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Active emails only
    List<RecipientEmailEntity> findByRecipientIdAndStatus(Long recipientId, EmailStatus status);

    // Current primary email
    Optional<RecipientEmailEntity> findByRecipientIdAndIsPrimaryTrueAndStatus(Long recipientId, EmailStatus status);

    // Clear primary flag for all emails of a recipient (before setting a new primary)
    @Modifying
    @Transactional
    @Query("UPDATE RecipientEmailEntity e SET e.isPrimary = false WHERE e.recipientId = :recipientId AND e.status = 'ACTIVE'")
    void clearPrimaryForRecipient(@Param("recipientId") Long recipientId);

    // Count active emails
    long countByRecipientIdAndStatus(Long recipientId, EmailStatus status);
}
