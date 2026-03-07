package com.cmips.repository;

import com.cmips.entity.TravelClaimTimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelClaimTimeEntryRepository extends JpaRepository<TravelClaimTimeEntryEntity, Long> {

    List<TravelClaimTimeEntryEntity> findByTravelClaimIdOrderByEntryDateAsc(Long travelClaimId);

    @Query("SELECT SUM(e.travelHoursClaimed) FROM TravelClaimTimeEntryEntity e WHERE e.travelClaimId = :claimId")
    Double sumTravelHoursClaimedByClaimId(@Param("claimId") Long claimId);

    @Query("SELECT e.workWeekNumber, SUM(e.travelHoursClaimed) FROM TravelClaimTimeEntryEntity e " +
           "WHERE e.travelClaimId = :claimId GROUP BY e.workWeekNumber ORDER BY e.workWeekNumber")
    List<Object[]> sumTravelHoursByWorkWeek(@Param("claimId") Long claimId);

    void deleteByTravelClaimId(Long travelClaimId);
}
