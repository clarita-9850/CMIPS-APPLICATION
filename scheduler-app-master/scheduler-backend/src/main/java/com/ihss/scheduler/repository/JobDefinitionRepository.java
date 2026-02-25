package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.JobDefinition;
import com.ihss.scheduler.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDefinitionRepository extends JpaRepository<JobDefinition, Long> {

    Optional<JobDefinition> findByJobNameAndDeletedAtIsNull(String jobName);

    Optional<JobDefinition> findByIdAndDeletedAtIsNull(Long id);

    List<JobDefinition> findAllByDeletedAtIsNull();

    Page<JobDefinition> findAllByDeletedAtIsNull(Pageable pageable);

    List<JobDefinition> findByStatusAndDeletedAtIsNull(JobStatus status);

    List<JobDefinition> findByJobTypeAndDeletedAtIsNull(String jobType);

    List<JobDefinition> findByEnabledAndDeletedAtIsNull(Boolean enabled);

    @Query("SELECT j FROM JobDefinition j WHERE j.deletedAt IS NULL AND j.enabled = true " +
           "AND j.status = 'ACTIVE' AND j.cronExpression IS NOT NULL")
    List<JobDefinition> findSchedulableJobs();

    @Query("SELECT j FROM JobDefinition j WHERE j.deletedAt IS NULL " +
           "AND (LOWER(j.jobName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<JobDefinition> searchJobs(@Param("search") String search, Pageable pageable);

    @Query("SELECT j FROM JobDefinition j WHERE j.deletedAt IS NULL " +
           "AND (:status IS NULL OR j.status = :status) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:enabled IS NULL OR j.enabled = :enabled)")
    Page<JobDefinition> findByFilters(
        @Param("status") JobStatus status,
        @Param("jobType") String jobType,
        @Param("enabled") Boolean enabled,
        Pageable pageable
    );

    @Query("SELECT DISTINCT j.jobType FROM JobDefinition j WHERE j.deletedAt IS NULL")
    List<String> findDistinctJobTypes();

    boolean existsByJobNameAndDeletedAtIsNull(String jobName);

    @Query("SELECT j FROM JobDefinition j LEFT JOIN FETCH j.dependencies d " +
           "WHERE j.id = :id AND j.deletedAt IS NULL")
    Optional<JobDefinition> findByIdWithDependencies(@Param("id") Long id);

    @Query("SELECT j FROM JobDefinition j LEFT JOIN FETCH j.dependents d " +
           "WHERE j.id = :id AND j.deletedAt IS NULL")
    Optional<JobDefinition> findByIdWithDependents(@Param("id") Long id);
}
