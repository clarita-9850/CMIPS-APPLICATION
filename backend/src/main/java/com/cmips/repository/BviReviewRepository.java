package com.cmips.repository;

import com.cmips.entity.BviReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BviReviewRepository extends JpaRepository<BviReviewEntity, Long> {

    List<BviReviewEntity> findByTimesheetId(Long timesheetId);

    List<BviReviewEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<BviReviewEntity> findByStatusOrderByCreatedAtAsc(BviReviewEntity.BviReviewStatus status);

    List<BviReviewEntity> findByCountyCodeAndStatusOrderByCreatedAtAsc(
            String countyCode, BviReviewEntity.BviReviewStatus status);

    Optional<BviReviewEntity> findByReviewNumber(String reviewNumber);

    @Query("SELECT b FROM BviReviewEntity b WHERE b.status = 'PENDING_RECIPIENT_REVIEW' " +
            "AND b.reviewDeadline < :today")
    List<BviReviewEntity> findExpiredPendingReviews(@Param("today") LocalDate today);
}
