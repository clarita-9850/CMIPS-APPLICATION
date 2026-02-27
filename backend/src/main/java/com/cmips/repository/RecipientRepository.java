package com.cmips.repository;

import com.cmips.entity.RecipientEntity;
import com.cmips.entity.RecipientEntity.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RecipientRepository extends JpaRepository<RecipientEntity, Long> {

    // Find by SSN (per BR OS 02)
    Optional<RecipientEntity> findBySsn(String ssn);

    // Find by CIN (per BR OS 03)
    Optional<RecipientEntity> findByCin(String cin);

    // Find by person type
    List<RecipientEntity> findByPersonType(PersonType personType);

    // Find by person type with pagination
    Page<RecipientEntity> findByPersonType(PersonType personType, Pageable pageable);

    // Find by county
    List<RecipientEntity> findByCountyCode(String countyCode);

    // Find by residence county with pagination
    Page<RecipientEntity> findByResidenceCounty(String residenceCounty, Pageable pageable);

    // Find by county and person type with pagination
    Page<RecipientEntity> findByResidenceCountyAndPersonType(String residenceCounty, PersonType personType, Pageable pageable);

    // Search by last name (with Soundex support would need custom implementation)
    List<RecipientEntity> findByLastNameContainingIgnoreCase(String lastName);

    // Search by full or partial last name (per BR OS 05)
    @Query("SELECT r FROM RecipientEntity r WHERE " +
           "UPPER(r.lastName) LIKE UPPER(CONCAT('%', :lastName, '%'))")
    List<RecipientEntity> searchByLastName(@Param("lastName") String lastName);

    // Search by address (per BR OS 04)
    @Query("SELECT r FROM RecipientEntity r WHERE " +
           "(:streetNumber IS NULL OR r.residenceStreetNumber = :streetNumber) AND " +
           "(:streetName IS NULL OR UPPER(r.residenceStreetName) LIKE UPPER(CONCAT('%', :streetName, '%'))) AND " +
           "(:city IS NULL OR UPPER(r.residenceCity) LIKE UPPER(CONCAT('%', :city, '%')))")
    List<RecipientEntity> searchByAddress(
            @Param("streetNumber") String streetNumber,
            @Param("streetName") String streetName,
            @Param("city") String city);

    // Comprehensive search (per BR OS 05)
    @Query(value = "SELECT * FROM recipients r WHERE " +
           "(:ssn IS NULL OR r.ssn = :ssn) AND " +
           "(:cin IS NULL OR r.cin = :cin) AND " +
           "(:lastName IS NULL OR UPPER(CAST(r.last_name AS VARCHAR)) LIKE UPPER('%' || :lastName || '%')) AND " +
           "(:firstName IS NULL OR UPPER(CAST(r.first_name AS VARCHAR)) LIKE UPPER('%' || :firstName || '%')) AND " +
           "(:countyCode IS NULL OR r.county_code = :countyCode) AND " +
           "(:personType IS NULL OR r.person_type = :personType)", nativeQuery = true)
    List<RecipientEntity> searchRecipients(
            @Param("ssn") String ssn,
            @Param("cin") String cin,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("countyCode") String countyCode,
            @Param("personType") String personType);

    // Find open referrals by county
    List<RecipientEntity> findByCountyCodeAndPersonType(String countyCode, PersonType personType);

    // Find by email
    Optional<RecipientEntity> findByEmail(String email);

    // Find by phone
    List<RecipientEntity> findByPrimaryPhone(String phone);

    // Find duplicate SSN cases
    @Query("SELECT r FROM RecipientEntity r WHERE r.ssnType = 'DUPLICATE_SSN'")
    List<RecipientEntity> findDuplicateSsnRecipients();

    // Find suspect SSN cases
    @Query("SELECT r FROM RecipientEntity r WHERE r.ssnType = 'SUSPECT_SSN'")
    List<RecipientEntity> findSuspectSsnRecipients();

    // Find ESP registered recipients
    List<RecipientEntity> findByEspRegisteredTrue();

    // Find by provider number / taxpayer ID (BR-20)
    Optional<RecipientEntity> findByTaxpayerId(String taxpayerId);

    // Soundex phonetic name matching (BR-5: fuzzy duplicate detection)
    // Requires PostgreSQL fuzzystrmatch extension (CREATE EXTENSION IF NOT EXISTS fuzzystrmatch)
    @Query(value = "SELECT * FROM recipients WHERE soundex(CAST(last_name AS VARCHAR)) = soundex(CAST(:lastName AS VARCHAR)) " +
                   "AND soundex(CAST(first_name AS VARCHAR)) = soundex(CAST(:firstName AS VARCHAR))",
           nativeQuery = true)
    List<RecipientEntity> findBySoundex(@Param("lastName") String lastName,
                                        @Param("firstName") String firstName);

    /**
     * Expanded name/demographic search (BR OS 05 + BR-20).
     * All parameters optional; unset params (NULL) are ignored in the WHERE clause.
     * Supports DOB, gender, county, and personType filtering in addition to name.
     */
    @Query(value = "SELECT * FROM recipients r WHERE " +
           "(:lastName IS NULL OR UPPER(CAST(r.last_name AS VARCHAR)) LIKE UPPER('%' || :lastName || '%')) AND " +
           "(:firstName IS NULL OR UPPER(CAST(r.first_name AS VARCHAR)) LIKE UPPER('%' || :firstName || '%')) AND " +
           "(:dob IS NULL OR CAST(r.date_of_birth AS VARCHAR) = :dob) AND " +
           "(:gender IS NULL OR UPPER(CAST(r.gender AS VARCHAR)) = UPPER(:gender)) AND " +
           "(:countyCode IS NULL OR r.county_code = :countyCode) AND " +
           "(:personType IS NULL OR r.person_type = :personType)", nativeQuery = true)
    List<RecipientEntity> searchRecipientsExpanded(
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("dob") String dob,
            @Param("gender") String gender,
            @Param("countyCode") String countyCode,
            @Param("personType") String personType);

    // Companion case search - find recipients with matching address (per BR SE 26, 27)
    @Query("SELECT r FROM RecipientEntity r WHERE " +
           "r.residenceStreetNumber = :streetNumber AND " +
           "UPPER(r.residenceStreetName) = UPPER(:streetName) AND " +
           "UPPER(r.residenceCity) = UPPER(:city) AND " +
           "r.id != :excludeId")
    List<RecipientEntity> findCompanionCasesByAddress(
            @Param("streetNumber") String streetNumber,
            @Param("streetName") String streetName,
            @Param("city") String city,
            @Param("excludeId") Long excludeId);
}
