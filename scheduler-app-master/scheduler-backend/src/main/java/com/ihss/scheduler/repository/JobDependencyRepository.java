package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.JobDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDependencyRepository extends JpaRepository<JobDependency, Long> {

    List<JobDependency> findByJobIdAndIsActiveTrue(Long jobId);

    List<JobDependency> findByDependsOnJobIdAndIsActiveTrue(Long dependsOnJobId);

    Optional<JobDependency> findByJobIdAndDependsOnJobId(Long jobId, Long dependsOnJobId);

    boolean existsByJobIdAndDependsOnJobId(Long jobId, Long dependsOnJobId);

    @Query("SELECT d FROM JobDependency d " +
           "JOIN FETCH d.job j " +
           "JOIN FETCH d.dependsOnJob dj " +
           "WHERE d.isActive = true AND j.deletedAt IS NULL AND dj.deletedAt IS NULL")
    List<JobDependency> findAllActiveWithJobs();

    @Query("SELECT d FROM JobDependency d " +
           "JOIN FETCH d.dependsOnJob dj " +
           "WHERE d.job.id = :jobId AND d.isActive = true AND dj.deletedAt IS NULL")
    List<JobDependency> findDependenciesForJob(@Param("jobId") Long jobId);

    @Query("SELECT d FROM JobDependency d " +
           "JOIN FETCH d.job j " +
           "WHERE d.dependsOnJob.id = :jobId AND d.isActive = true AND j.deletedAt IS NULL")
    List<JobDependency> findDependentsOfJob(@Param("jobId") Long jobId);

    @Query("SELECT COUNT(d) FROM JobDependency d " +
           "WHERE d.job.id = :jobId AND d.isActive = true")
    long countDependencies(@Param("jobId") Long jobId);

    @Query("SELECT COUNT(d) FROM JobDependency d " +
           "WHERE d.dependsOnJob.id = :jobId AND d.isActive = true")
    long countDependents(@Param("jobId") Long jobId);

    void deleteByJobIdOrDependsOnJobId(Long jobId, Long dependsOnJobId);
}
