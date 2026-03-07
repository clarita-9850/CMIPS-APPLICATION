package com.cmips.repository;

import com.cmips.entity.TimesheetCodeTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetCodeTableRepository extends JpaRepository<TimesheetCodeTableEntity, Long> {

    List<TimesheetCodeTableEntity> findByTableTypeAndActiveTrueOrderByDisplayOrderAsc(
            TimesheetCodeTableEntity.TableType tableType);

    List<TimesheetCodeTableEntity> findByTableTypeOrderByDisplayOrderAsc(
            TimesheetCodeTableEntity.TableType tableType);

    TimesheetCodeTableEntity findByTableTypeAndCode(
            TimesheetCodeTableEntity.TableType tableType, String code);
}
