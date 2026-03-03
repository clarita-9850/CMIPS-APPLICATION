package com.cmips.service;

import com.cmips.entity.AlternativeIdEntity;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.ProviderEntity;
import com.cmips.entity.RecipientEntity;
import com.cmips.repository.AlternativeIdRepository;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Merge Duplicate SSN Service — DSD CI-446456
 *
 * Two-step Verify → Save workflow for merging duplicate SSN records.
 * Implements all error messages EM-192 through EM-204 and business rules BR-34 through BR-41.
 */
@Service
public class MergeDuplicateSsnService {

    private static final Logger log = LoggerFactory.getLogger(MergeDuplicateSsnService.class);

    private static final Set<CaseEntity.CaseStatus> ALLOWED_CASE_STATUSES = Set.of(
            CaseEntity.CaseStatus.TERMINATED,
            CaseEntity.CaseStatus.DENIED,
            CaseEntity.CaseStatus.APPLICATION_WITHDRAWN
    );

    private final RecipientRepository recipientRepository;
    private final ProviderRepository providerRepository;
    private final CaseRepository caseRepository;
    private final AlternativeIdRepository alternativeIdRepository;

    public MergeDuplicateSsnService(RecipientRepository recipientRepository,
                                     ProviderRepository providerRepository,
                                     CaseRepository caseRepository,
                                     AlternativeIdRepository alternativeIdRepository) {
        this.recipientRepository = recipientRepository;
        this.providerRepository = providerRepository;
        this.caseRepository = caseRepository;
        this.alternativeIdRepository = alternativeIdRepository;
    }

    // ── Record wrapper to unify Recipient and Provider lookups ──

    public static class PersonRecord {
        public final Long id;
        public final String cin;           // CIN for recipients, providerNumber for providers
        public final String type;          // "RECIPIENT" or "PROVIDER"
        public final String firstName;
        public final String middleName;
        public final String lastName;
        public final LocalDate dateOfBirth;
        public final String gender;
        public final String ssn;
        public final String ssnType;       // ssnType for recipients, ssnVerificationStatus for providers
        public final String status;        // personType for recipients, providerStatus for providers

        PersonRecord(RecipientEntity r) {
            this.id = r.getId();
            this.cin = r.getCin();
            this.type = "RECIPIENT";
            this.firstName = r.getFirstName();
            this.middleName = r.getMiddleName();
            this.lastName = r.getLastName();
            this.dateOfBirth = r.getDateOfBirth();
            this.gender = r.getGender();
            this.ssn = r.getSsn();
            this.ssnType = r.getSsnType();
            this.status = r.getPersonType() != null ? r.getPersonType().name() : null;
        }

        PersonRecord(ProviderEntity p) {
            this.id = p.getId();
            this.cin = p.getProviderNumber();
            this.type = "PROVIDER";
            this.firstName = p.getFirstName();
            this.middleName = p.getMiddleName();
            this.lastName = p.getLastName();
            this.dateOfBirth = p.getDateOfBirth();
            this.gender = p.getGender();
            this.ssn = p.getSsn();
            this.ssnType = p.getSsnVerificationStatus();
            this.status = p.getProviderStatus() != null ? p.getProviderStatus().name() : null;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("cin", cin);
            m.put("type", type);
            m.put("firstName", firstName);
            m.put("middleName", middleName);
            m.put("lastName", lastName);
            m.put("dateOfBirth", dateOfBirth != null ? dateOfBirth.toString() : null);
            m.put("gender", gender);
            m.put("ssn", ssn != null && ssn.length() >= 4
                    ? "***-**-" + ssn.substring(ssn.length() - 4) : "N/A");
            m.put("ssnType", ssnType);
            m.put("status", status);
            return m;
        }
    }

    // ── Result DTOs ──

    public static class VerifyResult {
        public boolean success;
        public List<String> errors = new ArrayList<>();
        public Map<String, Object> masterRecord;
        public List<Map<String, Object>> duplicateRecords = new ArrayList<>();
        public String effectiveMasterCin;  // After Make Master swap
    }

    public static class SaveResult {
        public boolean success;
        public String message;
        public List<String> errors = new ArrayList<>();
        public int mergedCount;
    }

    // ── Verify (BR-34 through BR-36, BR-40, BR-41) ──

    public VerifyResult verify(String ssn, String masterCin, List<String> duplicateCins, boolean makeMaster) {
        VerifyResult result = new VerifyResult();
        result.success = false;

        // Normalize SSN
        String normalizedSsn = ssn != null ? ssn.replaceAll("[^0-9]", "") : "";

        // EM-196: SSN must be exactly 9 numeric digits
        if (normalizedSsn.length() != 9) {
            result.errors.add("EM-196: Invalid SSN format. SSN must be 9 numeric digits.");
            return result;
        }

        // EM-197: Master CIN required
        if (masterCin == null || masterCin.trim().isEmpty()) {
            result.errors.add("EM-197: Master Record is required. Please enter a valid CIN or Provider Number.");
            return result;
        }
        masterCin = masterCin.trim();

        // EM-204: At least one duplicate CIN required
        List<String> validDups = duplicateCins != null
                ? duplicateCins.stream()
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList())
                : List.of();
        if (validDups.isEmpty()) {
            result.errors.add("EM-204: At least one Duplicate Record is required.");
            return result;
        }

        // BR-40: Make Master swap — if checked, the first non-empty duplicate becomes the new master
        String effectiveMasterCin = masterCin;
        if (makeMaster && !validDups.isEmpty()) {
            // The first duplicate CIN becomes the new master; old master becomes a duplicate
            String promotedCin = validDups.get(0);
            List<String> newDups = new ArrayList<>(validDups.subList(1, validDups.size()));
            newDups.add(0, masterCin); // Old master becomes first duplicate
            effectiveMasterCin = promotedCin;
            validDups = newDups;
        }
        result.effectiveMasterCin = effectiveMasterCin;

        // Look up master record
        PersonRecord master = lookupByCin(effectiveMasterCin);
        if (master == null) {
            result.errors.add("EM-198: Master Record '" + effectiveMasterCin + "' not found.");
            return result;
        }

        // BR-35: Master SSN must match entered SSN
        if (master.ssn == null || !master.ssn.replaceAll("[^0-9]", "").equals(normalizedSsn)) {
            result.errors.add("EM-202: Master Record SSN does not match the entered SSN.");
            return result;
        }

        // EM-199: Master cannot already be marked as DUPLICATE_SSN
        if ("DUPLICATE_SSN".equalsIgnoreCase(master.ssnType)) {
            result.errors.add("EM-199: Master Record '" + effectiveMasterCin + "' is already marked as a duplicate SSN.");
            return result;
        }

        // EM-201: Master cannot have SUSPECT_SSN
        if ("SUSPECT_SSN".equalsIgnoreCase(master.ssnType)) {
            result.errors.add("EM-201: Master Record '" + effectiveMasterCin + "' has a Suspect SSN and cannot be merged.");
            return result;
        }

        result.masterRecord = master.toMap();
        result.masterRecord.put("role", "MASTER");

        // Validate each duplicate
        boolean masterIsProvider = "PROVIDER".equals(master.type);
        boolean hasCaseDuplicates = false;

        for (String dupCin : validDups) {
            PersonRecord dup = lookupByCin(dupCin);
            if (dup == null) {
                result.errors.add("EM-198: Duplicate Record '" + dupCin + "' not found.");
                continue;
            }

            // BR-36: Duplicate SSN must match entered SSN
            if (dup.ssn == null || !dup.ssn.replaceAll("[^0-9]", "").equals(normalizedSsn)) {
                result.errors.add("EM-202: Duplicate Record '" + dupCin + "' SSN does not match the entered SSN.");
                continue;
            }

            // EM-203: Cannot be already marked as DUPLICATE_SSN
            if ("DUPLICATE_SSN".equalsIgnoreCase(dup.ssnType)) {
                result.errors.add("EM-203: Duplicate Record '" + dupCin + "' is already marked as a duplicate SSN.");
                continue;
            }

            // EM-201: Cannot have SUSPECT_SSN
            if ("SUSPECT_SSN".equalsIgnoreCase(dup.ssnType)) {
                result.errors.add("EM-201: Duplicate Record '" + dupCin + "' has a Suspect SSN and cannot be merged.");
                continue;
            }

            // EM-194: Provider duplicates must be TERMINATED
            if ("PROVIDER".equals(dup.type)) {
                if (!"TERMINATED".equalsIgnoreCase(dup.status)) {
                    result.errors.add("EM-194: Provider '" + dupCin + "' is not Terminated. Active providers cannot be marked as duplicates.");
                    continue;
                }
            }

            // EM-195: Recipient/Applicant duplicates must have all cases TERMINATED/DENIED/WITHDRAWN
            if ("RECIPIENT".equals(dup.type)) {
                List<CaseEntity> cases = caseRepository.findByRecipientId(dup.id);
                boolean hasActiveCases = cases.stream()
                        .anyMatch(c -> !ALLOWED_CASE_STATUSES.contains(c.getCaseStatus()));
                if (hasActiveCases) {
                    result.errors.add("EM-195: Recipient '" + dupCin + "' has active cases. All cases must be Terminated, Denied, or Withdrawn.");
                    continue;
                }
                if (!cases.isEmpty()) {
                    hasCaseDuplicates = true;
                }
            }

            // BR-41: Demographics must match (case-insensitive)
            List<String> mismatches = checkDemographics(master, dup);
            if (!mismatches.isEmpty()) {
                result.errors.add("EM-193: Duplicate Record '" + dupCin + "' demographics do not match Master. Mismatched fields: " + String.join(", ", mismatches));
                continue;
            }

            Map<String, Object> dupMap = dup.toMap();
            dupMap.put("role", "DUPLICATE");
            result.duplicateRecords.add(dupMap);
        }

        // EM-200: Provider as master with case duplicates
        if (masterIsProvider && hasCaseDuplicates) {
            result.errors.add("EM-200: Provider cannot be the Master Record when duplicate records have associated cases.");
            return result;
        }

        if (!result.errors.isEmpty()) {
            return result;
        }

        if (result.duplicateRecords.isEmpty()) {
            result.errors.add("No valid duplicate records to merge after validation.");
            return result;
        }

        result.success = true;
        return result;
    }

    // ── Save (BR-37 through BR-39) ──

    @Transactional
    public SaveResult save(String ssn, String masterCin, List<String> duplicateCins, boolean makeMaster, String userId) {
        SaveResult saveResult = new SaveResult();
        saveResult.success = false;

        // Re-run verify as idempotency guard
        VerifyResult verification = verify(ssn, masterCin, duplicateCins, makeMaster);
        if (!verification.success) {
            saveResult.errors = verification.errors;
            return saveResult;
        }

        String effectiveMasterCin = verification.effectiveMasterCin;
        int mergedCount = 0;

        for (Map<String, Object> dupRecord : verification.duplicateRecords) {
            String dupCin = (String) dupRecord.get("cin");
            String dupType = (String) dupRecord.get("type");
            Long dupId = dupRecord.get("id") instanceof Long
                    ? (Long) dupRecord.get("id")
                    : Long.valueOf(dupRecord.get("id").toString());

            String originalSsn;

            if ("RECIPIENT".equals(dupType)) {
                RecipientEntity recipient = recipientRepository.findById(dupId).orElse(null);
                if (recipient == null) continue;

                originalSsn = recipient.getSsn();

                // BR-37: Set ssnType to DUPLICATE_SSN
                recipient.setSsnType("DUPLICATE_SSN");
                // BR-39: Clear SSN
                recipient.setSsn(null);
                recipient.setUpdatedBy(userId);
                recipientRepository.save(recipient);

            } else {
                ProviderEntity provider = providerRepository.findById(dupId).orElse(null);
                if (provider == null) continue;

                originalSsn = provider.getSsn();

                // BR-37: Set ssnVerificationStatus to DUPLICATE_SSN
                provider.setSsnVerificationStatus("DUPLICATE_SSN");
                // BR-39: Clear SSN
                provider.setSsn(null);
                provider.setUpdatedBy(userId);
                providerRepository.save(provider);
            }

            // BR-38: Create AlternativeId audit record
            AlternativeIdEntity altId = new AlternativeIdEntity();
            altId.setPersonId(dupId);
            altId.setPersonType(dupType);
            altId.setAlternativeIdType("DUPLICATE_SSN");
            altId.setOriginalSsn(originalSsn);
            altId.setMasterCin(effectiveMasterCin);
            altId.setComment("Merged via Merge Duplicate SSN. Master: " + effectiveMasterCin);
            altId.setCreatedBy(userId);
            alternativeIdRepository.save(altId);

            mergedCount++;
            log.info("[MergeDuplicateSSN] Merged {} {} (ID={}) as duplicate of master {}",
                    dupType, dupCin, dupId, effectiveMasterCin);
        }

        saveResult.success = true;
        saveResult.mergedCount = mergedCount;
        saveResult.message = "Successfully merged " + mergedCount + " duplicate record(s) under master " + effectiveMasterCin + ".";
        return saveResult;
    }

    // ── Helpers ──

    /**
     * Look up a person by CIN (for recipients) or provider number (for providers).
     */
    private PersonRecord lookupByCin(String cin) {
        // Try recipient CIN first
        Optional<RecipientEntity> recipientOpt = recipientRepository.findByCin(cin);
        if (recipientOpt.isPresent()) {
            return new PersonRecord(recipientOpt.get());
        }

        // Fallback: try provider number
        Optional<ProviderEntity> providerOpt = providerRepository.findByProviderNumber(cin);
        if (providerOpt.isPresent()) {
            return new PersonRecord(providerOpt.get());
        }

        return null;
    }

    /**
     * Compare demographics between master and duplicate (BR-41).
     * Returns list of mismatched field names. Empty = all match.
     */
    private List<String> checkDemographics(PersonRecord master, PersonRecord dup) {
        List<String> mismatches = new ArrayList<>();

        if (!equalsIgnoreCaseNullSafe(master.firstName, dup.firstName)) {
            mismatches.add("First Name");
        }
        if (!equalsIgnoreCaseNullSafe(master.middleName, dup.middleName)) {
            mismatches.add("Middle Name");
        }
        if (!equalsIgnoreCaseNullSafe(master.lastName, dup.lastName)) {
            mismatches.add("Last Name");
        }
        if (!Objects.equals(master.dateOfBirth, dup.dateOfBirth)) {
            mismatches.add("Date of Birth");
        }
        if (!equalsIgnoreCaseNullSafe(master.gender, dup.gender)) {
            mismatches.add("Gender");
        }

        return mismatches;
    }

    private boolean equalsIgnoreCaseNullSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
