package com.cmips.repository;

import com.cmips.entity.ProviderEntity;
import com.cmips.entity.ProviderEntity.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProviderRepository extends JpaRepository<ProviderEntity, Long> {

    // Find by provider number (per BR OS 20)
    Optional<ProviderEntity> findByProviderNumber(String providerNumber);

    // Find by SSN
    Optional<ProviderEntity> findBySsn(String ssn);

    // Find by County Code
    List<ProviderEntity> findByCountyCode(String countyCode);

    // Find by County Code with pagination
    Page<ProviderEntity> findByCountyCode(String countyCode, Pageable pageable);

    // Find by status
    List<ProviderEntity> findByStatus(ProviderStatus status);

    // Find by status with pagination
    Page<ProviderEntity> findByStatus(ProviderStatus status, Pageable pageable);

    // Find by eligibility
    List<ProviderEntity> findByEligible(String eligible);

    // Find eligible providers by county
    @Query("SELECT p FROM ProviderEntity p WHERE p.countyCode = :countyCode AND p.eligible = 'YES'")
    List<ProviderEntity> findEligibleProvidersByCounty(@Param("countyCode") String countyCode);

    // Search providers (per BR OS 20)
    @Query("SELECT p FROM ProviderEntity p WHERE " +
           "(:providerNumber IS NULL OR p.providerNumber = :providerNumber) AND " +
           "(:ssn IS NULL OR REPLACE(p.ssn, '-', '') = REPLACE(:ssn, '-', '')) AND " +
           "(:lastName IS NULL OR UPPER(p.lastName) LIKE UPPER(CONCAT('%', :lastName, '%'))) AND " +
           "(:firstName IS NULL OR UPPER(p.firstName) LIKE UPPER(CONCAT('%', :firstName, '%'))) AND " +
           "(:countyCode IS NULL OR p.countyCode = :countyCode) AND " +
           "(:dateOfBirth IS NULL OR p.dateOfBirth = :dateOfBirth) AND " +
           "(:gender IS NULL OR UPPER(p.gender) = UPPER(:gender)) AND " +
           "(:city IS NULL OR UPPER(p.city) LIKE UPPER(CONCAT('%', :city, '%'))) AND " +
           "(:phone IS NULL OR REPLACE(p.phone, '-', '') LIKE CONCAT('%', REPLACE(:phone, '-', ''), '%')) AND " +
           "(:email IS NULL OR UPPER(p.email) LIKE UPPER(CONCAT('%', :email, '%')))")
    List<ProviderEntity> searchProviders(
            @Param("providerNumber") String providerNumber,
            @Param("ssn") String ssn,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("countyCode") String countyCode,
            @Param("dateOfBirth") LocalDate dateOfBirth,
            @Param("gender") String gender,
            @Param("city") String city,
            @Param("phone") String phone,
            @Param("email") String email);

    // Find providers with SSN verification pending (per BR PVM 03-05)
    @Query("SELECT p FROM ProviderEntity p WHERE p.ssnVerificationStatus = 'NOT_YET_VERIFIED'")
    List<ProviderEntity> findProvidersWithPendingSsnVerification();

    // Find Medi-Cal suspended providers (per BR PVM 09)
    List<ProviderEntity> findByMediCalSuspendedTrue();

    // Find providers inactive for 1 year (per BR PVM 22)
    @Query("SELECT p FROM ProviderEntity p WHERE " +
           "p.eligible = 'YES' AND " +
           "p.effectiveDate <= :cutoffDate AND " +
           "p.id NOT IN (SELECT DISTINCT pa.providerId FROM ProviderAssignmentEntity pa WHERE pa.status = 'ACTIVE')")
    List<ProviderEntity> findInactiveProvidersForOneYear(@Param("cutoffDate") LocalDate cutoffDate);

    // Find providers who can be reinstated (within 30 days per BR PVM 25)
    @Query("SELECT p FROM ProviderEntity p WHERE " +
           "p.eligible = 'NO' AND " +
           "p.effectiveDate >= :cutoffDate AND " +
           "p.ineligibleReason NOT IN ('THIRD_OVERTIME_VIOLATION', 'FOURTH_OVERTIME_VIOLATION')")
    List<ProviderEntity> findProvidersEligibleForReinstatement(@Param("cutoffDate") LocalDate cutoffDate);

    // Find providers with overtime violations
    @Query("SELECT p FROM ProviderEntity p WHERE p.overtimeViolationCount > 0")
    List<ProviderEntity> findProvidersWithOvertimeViolations();

    // Find providers with active overtime exemption
    List<ProviderEntity> findByHasOvertimeExemptionTrue();

    // Find providers with CORI issues
    @Query("SELECT DISTINCT p FROM ProviderEntity p WHERE " +
           "p.ineligibleReason IN ('TIER_1_CONVICTION', 'TIER_2_CONVICTION', 'SUBSEQUENT_TIER_1_CONVICTION', 'SUBSEQUENT_TIER_2_CONVICTION')")
    List<ProviderEntity> findProvidersWithCoriIssues();

    // Find providers due for sick leave eligibility
    @Query("SELECT p FROM ProviderEntity p WHERE " +
           "p.totalServiceHoursWorked >= 100 AND " +
           "p.sickLeaveAccruedDate IS NULL")
    List<ProviderEntity> findProvidersDueForSickLeaveAccrual();

    // Find ESP registered providers
    List<ProviderEntity> findByEspRegisteredTrue();

    // Count providers by county and status
    @Query("SELECT COUNT(p) FROM ProviderEntity p WHERE p.countyCode = :countyCode AND p.status = :status")
    Long countByCountyAndStatus(@Param("countyCode") String countyCode, @Param("status") ProviderStatus status);
}
