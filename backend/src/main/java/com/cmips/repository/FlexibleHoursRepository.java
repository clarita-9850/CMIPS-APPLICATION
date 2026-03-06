package com.cmips.repository;

import com.cmips.entity.FlexibleHoursEntity;
import com.cmips.entity.FlexibleHoursEntity.FlexStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlexibleHoursRepository extends JpaRepository<FlexibleHoursEntity, Long> {

    List<FlexibleHoursEntity> findByCaseIdOrderByServiceMonthDesc(Long caseId);

    List<FlexibleHoursEntity> findByCaseIdAndStatusOrderByServiceMonthDesc(Long caseId, FlexStatus status);

    /**
     * Sum of approved hours for a case in a given service month (for 80-hr cap validation).
     * Returns total approved minutes.
     */
    @Query("SELECT COALESCE(SUM(f.approvedHours), 0) FROM FlexibleHoursEntity f " +
           "WHERE f.caseId = :caseId AND f.serviceMonth = :serviceMonth AND f.status = 'APPROVED'")
    int sumApprovedMinutesByMonth(@Param("caseId") Long caseId,
                                  @Param("serviceMonth") LocalDate serviceMonth);
}
