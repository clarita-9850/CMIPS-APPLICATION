package com.cmips.repository;

import com.cmips.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {

    Optional<TaskType> findByTaskTypeCode(String taskTypeCode);

    List<TaskType> findByActiveTrue();

    List<TaskType> findByTargetQueue(String targetQueue);

    List<TaskType> findByFunctionalArea(String functionalArea);

    List<TaskType> findByIsNotificationTrue();

    List<TaskType> findByIsNotificationFalse();

    List<TaskType> findByEscalationEnabledTrue();

    List<TaskType> findByAutoCloseEnabledTrue();

    boolean existsByTaskTypeCode(String taskTypeCode);
}
