package com.cmips.repository;

import com.cmips.entity.TravelTimeEntity;
import com.cmips.entity.TravelTimeEntity.TravelTimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelTimeRepository extends JpaRepository<TravelTimeEntity, Long> {

    List<TravelTimeEntity> findByProviderId(Long providerId);

    List<TravelTimeEntity> findByProviderIdAndStatus(Long providerId, TravelTimeStatus status);

    // Active travel time records for provider
    @Query("SELECT t FROM TravelTimeEntity t WHERE t.providerId = :providerId AND t.status = 'ACTIVE'")
    List<TravelTimeEntity> findActiveByProviderId(@Param("providerId") Long providerId);

    // History ordered by begin date (for Travel Time History view)
    @Query("SELECT t FROM TravelTimeEntity t WHERE t.providerId = :providerId " +
           "ORDER BY t.beginDate DESC")
    List<TravelTimeEntity> findHistoryForProvider(@Param("providerId") Long providerId);

    // Travel times for a specific recipient pair (from → to)
    @Query("SELECT t FROM TravelTimeEntity t WHERE t.providerId = :providerId " +
           "AND t.fromRecipientId = :fromRecipientId AND t.toRecipientId = :toRecipientId " +
           "AND t.status = 'ACTIVE'")
    List<TravelTimeEntity> findActiveForRecipientPair(
            @Param("providerId") Long providerId,
            @Param("fromRecipientId") Long fromRecipientId,
            @Param("toRecipientId") Long toRecipientId);

    // Total weekly travel hours for provider (7-hour rule calculation)
    @Query("SELECT COALESCE(SUM(t.travelHoursWeekly), 0) FROM TravelTimeEntity t " +
           "WHERE t.providerId = :providerId AND t.status = 'ACTIVE'")
    Double getTotalWeeklyTravelHours(@Param("providerId") Long providerId);

    // Travel times linked to a specific recipient as destination
    List<TravelTimeEntity> findByToRecipientIdAndStatus(Long toRecipientId, TravelTimeStatus status);
}
