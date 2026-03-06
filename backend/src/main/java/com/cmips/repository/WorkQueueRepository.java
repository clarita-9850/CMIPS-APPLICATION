package com.cmips.repository;

import com.cmips.entity.WorkQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkQueueRepository extends JpaRepository<WorkQueue, Long> {

    Optional<WorkQueue> findByName(String name);

    List<WorkQueue> findByActiveTrue();

    List<WorkQueue> findByQueueCategory(WorkQueue.QueueCategory category);

    List<WorkQueue> findByActiveTrueAndSupervisorOnlyFalse();

    List<WorkQueue> findByActiveTrueAndSupervisorOnlyTrue();

    List<WorkQueue> findByActiveTrueAndCounty(String county);

    List<WorkQueue> findByActiveTrueAndQueueCategory(WorkQueue.QueueCategory category);

    boolean existsByName(String name);
}
