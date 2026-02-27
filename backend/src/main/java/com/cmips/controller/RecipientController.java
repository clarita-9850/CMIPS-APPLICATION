package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.entity.RecipientEntity.PersonType;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.service.CaseManagementService;
import com.cmips.service.MEDSService;
import com.cmips.service.PayrollIntegrationService;
import com.cmips.service.SCIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recipient/Person Management REST Controller
 * Implements DSD Section 20 - Online Search Business Rules
 * All endpoints are protected by configurable Keycloak permissions
 */
@RestController
@RequestMapping("/api/recipients")
@CrossOrigin(origins = "*")
public class RecipientController {

    private static final Logger log = LoggerFactory.getLogger(RecipientController.class);

    private final RecipientRepository recipientRepository;
    private final ProviderRepository providerRepository;
    private final CaseManagementService caseManagementService;
    private final SCIService sciService;
    private final MEDSService medsService;
    private final PayrollIntegrationService payrollIntegrationService;

    public RecipientController(RecipientRepository recipientRepository,
                               ProviderRepository providerRepository,
                               CaseManagementService caseManagementService,
                               SCIService sciService,
                               MEDSService medsService,
                               PayrollIntegrationService payrollIntegrationService) {
        this.recipientRepository = recipientRepository;
        this.providerRepository = providerRepository;
        this.caseManagementService = caseManagementService;
        this.sciService = sciService;
        this.medsService = medsService;
        this.payrollIntegrationService = payrollIntegrationService;
    }

    // ==================== PERSON SEARCH (BR OS 01-10, BR-20) ====================

    /**
     * Search persons by multiple criteria.
     *
     * Search hierarchy (BR OS 01-10, BR-20):
     *  1. SSN exact match          — strongest signal, returns at most 1 result
     *  2. CIN exact match          — second strongest
     *  3. Provider Number exact    — BR-20
     *  4. Last Name + optional First/DOB/Gender/County — Soundex + exact DOB filter
     *  5. Phone number             — EM-251 eligible search
     *  6. Email address            — EM-252 eligible search
     *
     * Validation rules (DSD EM-251, EM-252, EM-254):
     *  EM-251/252: Phone must be 10 digits (3-digit area code + 7-digit number) if provided
     *  EM-254: Valid email format if provided
     *
     * Minimum search requirement (DSD BR-4, BR-5): At least one of SSN, CIN, Provider Number,
     * Phone, Email, or partial lastName (min 3 chars) with optional address/other criteria.
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Recipient Resource", scope = "view")
    public ResponseEntity<?> searchPersons(
            @RequestParam(required = false) String ssn,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String providerNumber,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String dob,          // yyyy-MM-dd
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String streetNumber,
            @RequestParam(required = false) String streetName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String personType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // ── Input validation (EM-251, EM-252, EM-254) ────────────────────────
        if (phone != null && !phone.isBlank()) {
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() != 10) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "EM-251: Area Code must be three numeric digits. " +
                        "EM-252: Phone Number must be seven numeric digits. Please enter a valid 10-digit phone number."));
            }
        }
        if (email != null && !email.isBlank()) {
            if (!email.contains("@") || !email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "EM-254: Not a valid email address. Please enter valid email address."));
            }
        }

        // ── Minimum search requirement (DSD BR-4, BR-5) ──────────────────────
        // At least one of: SSN, CIN, Provider#, Phone, Email, or lastName (min 3 chars for partial)
        // Complete address (streetNumber + streetName + city) also qualifies
        boolean hasSsn = ssn != null && !ssn.isBlank();
        boolean hasCin = cin != null && !cin.isBlank();
        boolean hasProviderNum = providerNumber != null && !providerNumber.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasCompleteAddress = (streetNumber != null && !streetNumber.isBlank())
                && (streetName != null && !streetName.isBlank())
                && (city != null && !city.isBlank());
        boolean hasValidLastName = lastName != null && lastName.trim().length() >= 3;

        if (!hasSsn && !hasCin && !hasProviderNum && !hasPhone && !hasEmail && !hasCompleteAddress && !hasValidLastName) {
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "At least one search criterion is required: SSN, CIN, Provider Number, " +
                    "Phone, Email, complete address (Street Number + Street Name + City), or Last Name (minimum 3 characters)."));
        }

        List<RecipientEntity> recipients;

        // ── Search hierarchy ────────────────────────────────────────────────
        if (ssn != null && !ssn.isBlank()) {
            // 1) BR OS 02: SSN match — use findAllBySsn to handle duplicate SSN cases (BR-1)
            String normalizedSsn = ssn.replaceAll("[^0-9]", "");
            recipients = recipientRepository.findAllBySsn(normalizedSsn);

        } else if (cin != null && !cin.isBlank()) {
            // 2) BR OS 03: CIN exact match
            recipients = recipientRepository.findByCin(cin)
                    .map(List::of)
                    .orElse(List.of());

        } else if (providerNumber != null && !providerNumber.isBlank()) {
            // 3) BR-20: Provider Number search — searches ProviderEntity, not RecipientEntity
            java.util.Optional<ProviderEntity> providerOpt = providerRepository.findByProviderNumber(providerNumber);
            List<java.util.Map<String, Object>> providerResults = providerOpt
                    .map(p -> {
                        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("id", p.getId());
                        m.put("providerNumber", p.getProviderNumber());
                        m.put("firstName", p.getFirstName());
                        m.put("lastName", p.getLastName());
                        m.put("dateOfBirth", p.getDateOfBirth());
                        m.put("gender", p.getGender());
                        m.put("ssn", p.getSsn());
                        m.put("primaryPhone", p.getPrimaryPhone());
                        m.put("email", p.getEmail());
                        m.put("streetAddress", p.getStreetAddress());
                        m.put("city", p.getCity());
                        m.put("state", p.getState());
                        m.put("zipCode", p.getZipCode());
                        m.put("providerStatus", p.getProviderStatus() != null ? p.getProviderStatus().name() : null);
                        m.put("resultType", "PROVIDER");
                        return List.of(m);
                    })
                    .orElse(List.of());
            java.util.Map<String, Object> providerResponse = new java.util.HashMap<>();
            providerResponse.put("searchResultType", "PROVIDER");
            providerResponse.put("content", providerResults);
            providerResponse.put("totalElements", providerResults.size());
            providerResponse.put("totalPages", providerResults.isEmpty() ? 0 : 1);
            providerResponse.put("currentPage", 0);
            providerResponse.put("pageSize", size);
            return ResponseEntity.ok(providerResponse);

        } else if (phone != null && !phone.isBlank()) {
            // 4) Phone search (EM-251) — strip formatting from both input and stored phone
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            recipients = recipientRepository.findByPrimaryPhoneDigits(digitsOnly);

        } else if (email != null && !email.isBlank()) {
            // 5) Email search — use findAllByEmail to handle duplicate email cases
            recipients = recipientRepository.findAllByEmail(email);

        } else if (hasCompleteAddress) {
            // 6) Address search (DSD: complete address = Street Number + Street Name + City)
            recipients = recipientRepository.searchByAddress(streetNumber, streetName, city);

        } else {
            // 7) Name-based search (BR OS 05): expanded with DOB, gender, county, personType
            // lastName min 3 chars enforced by validation above
            recipients = recipientRepository.searchRecipientsExpanded(
                    lastName, firstName, dob, gender, countyCode, personType);

            // Soundex near-match augmentation: add phonetic results for lastName
            if (lastName != null && !lastName.isBlank() && firstName != null && !firstName.isBlank() && lastName.trim().length() >= 3) {
                List<RecipientEntity> soundexMatches = recipientRepository.findBySoundex(lastName, firstName);
                // Merge without duplicates (keep soundex matches not already in exact results)
                java.util.Set<Long> exactIds = recipients.stream()
                        .map(RecipientEntity::getId)
                        .collect(java.util.stream.Collectors.toSet());
                for (RecipientEntity sr : soundexMatches) {
                    if (!exactIds.contains(sr.getId())) {
                        recipients = new java.util.ArrayList<>(recipients);
                        ((java.util.ArrayList<RecipientEntity>) recipients).add(sr);
                    }
                }
            }
        }

        // ── Pagination (client-side after search) ───────────────────────────
        int total = recipients.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex   = Math.min(fromIndex + size, total);
        List<RecipientEntity> paged = recipients.subList(fromIndex, toIndex);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("searchResultType", "RECIPIENT");
        response.put("content", paged);
        response.put("totalElements", total);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Recipient Resource", scope = "view")
    public ResponseEntity<RecipientEntity> getRecipientById(@PathVariable Long id) {
        RecipientEntity recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        return ResponseEntity.ok(recipient);
    }

    @GetMapping
    @RequirePermission(resource = "Recipient Resource", scope = "view")
    public ResponseEntity<java.util.Map<String, Object>> getAllRecipients(
            @RequestParam(required = false) String personType,
            @RequestParam(required = false) String countyCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<RecipientEntity> recipientPage;

        if (personType != null && !personType.isEmpty() && countyCode != null && !countyCode.isEmpty()) {
            recipientPage = recipientRepository.findByResidenceCountyAndPersonType(countyCode, PersonType.valueOf(personType), pageable);
        } else if (personType != null && !personType.isEmpty()) {
            recipientPage = recipientRepository.findByPersonType(PersonType.valueOf(personType), pageable);
        } else if (countyCode != null && !countyCode.isEmpty()) {
            recipientPage = recipientRepository.findByResidenceCounty(countyCode, pageable);
        } else {
            recipientPage = recipientRepository.findAll(pageable);
        }

        // Return paginated response with metadata
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", recipientPage.getContent());
        response.put("totalElements", recipientPage.getTotalElements());
        response.put("totalPages", recipientPage.getTotalPages());
        response.put("currentPage", recipientPage.getNumber());
        response.put("pageSize", recipientPage.getSize());

        return ResponseEntity.ok(response);
    }

    // ==================== REFERRAL MANAGEMENT (BR OS 11-19, 42) ====================

    @PostMapping("/referrals")
    @RequirePermission(resource = "Referral Resource", scope = "create")
    public ResponseEntity<?> createReferral(
            @RequestBody RecipientEntity recipient,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            // BR OS 11: Create new referral with OPEN_REFERRAL status
            // EM-201/205/206/208: Required-field validation enforced in service
            RecipientEntity created = caseManagementService.createReferral(recipient, userId);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.warn("[createReferral] Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/referrals/{id}/close")
    @RequirePermission(resource = "Referral Resource", scope = "close")
    public ResponseEntity<RecipientEntity> closeReferral(
            @PathVariable Long id,
            @RequestBody CloseReferralRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR OS 14: Close referral with reason
        RecipientEntity recipient = caseManagementService.closeReferral(id, request.getReason(), userId);
        return ResponseEntity.ok(recipient);
    }

    @PutMapping("/referrals/{id}/reopen")
    @RequirePermission(resource = "Referral Resource", scope = "reopen")
    public ResponseEntity<RecipientEntity> reopenReferral(
            @PathVariable Long id,
            @RequestBody ReopenReferralRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // BR OS 42: Reopen closed referral (within 30 days)
        RecipientEntity recipient = caseManagementService.reopenReferral(
                id, request.getReferralSource(), request.getCountyCode(), userId);
        return ResponseEntity.ok(recipient);
    }

    // ==================== RECIPIENT CRUD ====================

    @PostMapping
    @RequirePermission(resource = "Recipient Resource", scope = "create")
    public ResponseEntity<RecipientEntity> createRecipient(
            @RequestBody RecipientEntity recipient,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        recipient.setCreatedBy(userId);
        RecipientEntity saved = recipientRepository.save(recipient);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Recipient Resource", scope = "edit")
    public ResponseEntity<RecipientEntity> updateRecipient(
            @PathVariable Long id,
            @RequestBody RecipientEntity updates,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        RecipientEntity existing = recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // ── BR-14: capture old Name/DOB/Gender BEFORE update ────────────────
        String oldLastName  = existing.getLastName();
        String oldFirstName = existing.getFirstName();
        LocalDate oldDob    = existing.getDateOfBirth();
        String oldGender    = existing.getGender();

        // Update allowed fields
        if (updates.getFirstName() != null) existing.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) existing.setLastName(updates.getLastName());
        if (updates.getMiddleName() != null) existing.setMiddleName(updates.getMiddleName());
        if (updates.getDateOfBirth() != null) existing.setDateOfBirth(updates.getDateOfBirth());
        if (updates.getGender() != null) existing.setGender(updates.getGender());
        if (updates.getResidenceCity() != null) existing.setResidenceCity(updates.getResidenceCity());
        if (updates.getResidenceState() != null) existing.setResidenceState(updates.getResidenceState());
        if (updates.getResidenceZip() != null) existing.setResidenceZip(updates.getResidenceZip());
        if (updates.getMailingCity() != null) existing.setMailingCity(updates.getMailingCity());
        if (updates.getMailingState() != null) existing.setMailingState(updates.getMailingState());
        if (updates.getMailingZip() != null) existing.setMailingZip(updates.getMailingZip());
        if (updates.getPrimaryPhone() != null) existing.setPrimaryPhone(updates.getPrimaryPhone());
        if (updates.getSecondaryPhone() != null) existing.setSecondaryPhone(updates.getSecondaryPhone());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getSpokenLanguage() != null) existing.setSpokenLanguage(updates.getSpokenLanguage());
        if (updates.getWrittenLanguage() != null) existing.setWrittenLanguage(updates.getWrittenLanguage());

        existing.setUpdatedBy(userId);
        RecipientEntity saved = recipientRepository.save(existing);

        // ── BR-14: OU transaction to SCI when Name/DOB/Gender changed ────────
        // Only send OU if the recipient has a cleared CIN — no CIN means SCI
        // has no record to update.
        boolean nameChanged   = !Objects.equals(oldLastName, saved.getLastName())
                             || !Objects.equals(oldFirstName, saved.getFirstName());
        boolean dobChanged    = !Objects.equals(oldDob, saved.getDateOfBirth());
        boolean genderChanged = !Objects.equals(oldGender, saved.getGender());

        if ((nameChanged || dobChanged || genderChanged) &&
                saved.getCin() != null && !saved.getCin().isBlank()) {
            String dobStr = saved.getDateOfBirth() != null
                    ? saved.getDateOfBirth().toString() : "";
            String mi = saved.getMiddleName() != null
                    ? saved.getMiddleName().substring(0, 1) : "";
            log.info("[BR-14] Demographic change detected for recipient {} (CIN={}). " +
                     "nameChanged={} dobChanged={} genderChanged={}. Sending OU to SCI.",
                     id, saved.getCin(), nameChanged, dobChanged, genderChanged);
            sciService.sendDemographicUpdate(
                    saved.getCin(),
                    saved.getLastName(),
                    saved.getFirstName(),
                    mi,
                    dobStr,
                    saved.getGender());

            // BR-11: Send IH12 to MEDS when demographics change (mirrors SCI OU)
            medsService.sendIH12UpdateClientInfo(
                    saved.getCin(),
                    saved.getLastName(),
                    saved.getFirstName(),
                    dobStr,
                    saved.getGender());
        }

        // BR-22: Send PR00922A to payroll on any demographic change (with or without CIN)
        if (nameChanged || dobChanged || genderChanged) {
            payrollIntegrationService.sendPR00922A(String.valueOf(id));
        }

        return ResponseEntity.ok(saved);
    }

    // ==================== ADDRESS MANAGEMENT ====================

    @PutMapping("/{id}/address")
    @RequirePermission(resource = "Recipient Resource", scope = "edit")
    public ResponseEntity<RecipientEntity> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        RecipientEntity recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if ("RESIDENCE".equals(request.getAddressType())) {
            recipient.setResidenceStreetName(request.getStreetName());
            recipient.setResidenceStreetNumber(request.getStreetNumber());
            recipient.setResidenceCity(request.getCity());
            recipient.setResidenceState(request.getState());
            recipient.setResidenceZip(request.getZipCode());
        } else if ("MAILING".equals(request.getAddressType())) {
            recipient.setMailingStreetName(request.getStreetName());
            recipient.setMailingCity(request.getCity());
            recipient.setMailingState(request.getState());
            recipient.setMailingZip(request.getZipCode());
        }

        recipient.setUpdatedBy(userId);
        RecipientEntity saved = recipientRepository.save(recipient);

        // BR-24: Send PR00924A to payroll on address change
        payrollIntegrationService.sendPR00924A(String.valueOf(id),
                request.getStreetNumber() + " " + request.getStreetName() + ", " +
                request.getCity() + ", " + request.getState() + " " + request.getZipCode());

        return ResponseEntity.ok(saved);
    }

    // ==================== ESP REGISTRATION (BR OS 35-38) ====================

    @PutMapping("/{id}/esp-registration")
    @RequirePermission(resource = "Recipient Resource", scope = "edit")
    public ResponseEntity<RecipientEntity> updateEspRegistration(
            @PathVariable Long id,
            @RequestBody EspRegistrationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        RecipientEntity recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // BR OS 35-38: ESP registration for electronic timesheet submission
        recipient.setEspRegistered(request.getEspRegistered());
        recipient.setUpdatedBy(userId);

        RecipientEntity saved = recipientRepository.save(recipient);
        return ResponseEntity.ok(saved);
    }

    // ==================== ACCESSIBILITY OPTIONS ====================

    @PutMapping("/{id}/accessibility")
    @RequirePermission(resource = "Recipient Resource", scope = "edit")
    public ResponseEntity<RecipientEntity> updateAccessibilityOptions(
            @PathVariable Long id,
            @RequestBody AccessibilityRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        RecipientEntity recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Accessibility options - using NOA option
        if (request.getLargeFont() != null && request.getLargeFont()) {
            recipient.setNoaOption("LARGE_FONT");
        } else {
            recipient.setNoaOption("STANDARD");
        }
        if (request.getScreenReader() != null) {
            recipient.setBlindVisuallyImpaired(request.getScreenReader());
        }
        recipient.setUpdatedBy(userId);

        RecipientEntity saved = recipientRepository.save(recipient);
        return ResponseEntity.ok(saved);
    }

    // ==================== COMPANION CASES (BR SE 26-27) ====================

    @GetMapping("/{id}/companion-cases")
    @RequirePermission(resource = "Recipient Resource", scope = "view")
    public ResponseEntity<List<RecipientEntity>> findCompanionCases(@PathVariable Long id) {
        List<RecipientEntity> companions = caseManagementService.findCompanionCases(id);
        return ResponseEntity.ok(companions);
    }

    // ==================== PERSON TYPE LIFECYCLE (BR-19) ====================

    @PatchMapping("/{id}/person-type")
    @RequirePermission(resource = "Recipient Resource", scope = "edit")
    public ResponseEntity<?> updatePersonType(
            @PathVariable Long id,
            @RequestBody PersonTypeUpdateRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        RecipientEntity recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        PersonType current = recipient.getPersonType();
        PersonType target;
        try {
            target = PersonType.valueOf(req.getPersonType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Invalid personType: " + req.getPersonType()));
        }

        // BR-19: Cannot transition RECIPIENT → OPEN_REFERRAL
        if (current == PersonType.RECIPIENT && target == PersonType.OPEN_REFERRAL) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Cannot revert a RECIPIENT to OPEN_REFERRAL. Create a new referral instead."));
        }

        recipient.setPersonType(target);
        if (target == PersonType.CLOSED_REFERRAL) {
            recipient.setReferralClosedDate(java.time.LocalDate.now());
            recipient.setReferralClosedReason(req.getReason());
        } else if (target == PersonType.OPEN_REFERRAL) {
            recipient.setReferralDate(java.time.LocalDate.now());
            recipient.setReferralClosedDate(null);
        }
        recipient.setUpdatedBy(userId);

        RecipientEntity saved = recipientRepository.save(recipient);
        log.info("[person-type] Recipient {} transitioned {} → {} by {}", id, current, target, userId);
        return ResponseEntity.ok(saved);
    }

    // ==================== OPEN/CLOSED REFERRALS BY COUNTY ====================

    @GetMapping("/referrals/open")
    @RequirePermission(resource = "Referral Resource", scope = "view")
    public ResponseEntity<List<RecipientEntity>> getOpenReferrals(
            @RequestParam(required = false) String countyCode) {

        List<RecipientEntity> referrals;
        if (countyCode != null) {
            referrals = recipientRepository.findByCountyCodeAndPersonType(countyCode, PersonType.OPEN_REFERRAL);
        } else {
            referrals = recipientRepository.findByPersonType(PersonType.OPEN_REFERRAL);
        }
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/closed")
    @RequirePermission(resource = "Referral Resource", scope = "view")
    public ResponseEntity<List<RecipientEntity>> getClosedReferrals(
            @RequestParam(required = false) String countyCode) {

        List<RecipientEntity> referrals;
        if (countyCode != null) {
            referrals = recipientRepository.findByCountyCodeAndPersonType(countyCode, PersonType.CLOSED_REFERRAL);
        } else {
            referrals = recipientRepository.findByPersonType(PersonType.CLOSED_REFERRAL);
        }
        return ResponseEntity.ok(referrals);
    }

    // ==================== REQUEST DTOs ====================

    public static class CloseReferralRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ReopenReferralRequest {
        private String referralSource;
        private String countyCode;

        public String getReferralSource() { return referralSource; }
        public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

        public String getCountyCode() { return countyCode; }
        public void setCountyCode(String countyCode) { this.countyCode = countyCode; }
    }

    public static class AddressUpdateRequest {
        private String addressType; // RESIDENCE or MAILING
        private String streetNumber;
        private String streetName;
        private String city;
        private String state;
        private String zipCode;

        public String getAddressType() { return addressType; }
        public void setAddressType(String addressType) { this.addressType = addressType; }

        public String getStreetNumber() { return streetNumber; }
        public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

        public String getStreetName() { return streetName; }
        public void setStreetName(String streetName) { this.streetName = streetName; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }

    public static class EspRegistrationRequest {
        private Boolean espRegistered;

        public Boolean getEspRegistered() { return espRegistered; }
        public void setEspRegistered(Boolean espRegistered) { this.espRegistered = espRegistered; }
    }

    public static class AccessibilityRequest {
        private Boolean largeFont;
        private Boolean highContrast;
        private Boolean screenReader;

        public Boolean getLargeFont() { return largeFont; }
        public void setLargeFont(Boolean largeFont) { this.largeFont = largeFont; }

        public Boolean getHighContrast() { return highContrast; }
        public void setHighContrast(Boolean highContrast) { this.highContrast = highContrast; }

        public Boolean getScreenReader() { return screenReader; }
        public void setScreenReader(Boolean screenReader) { this.screenReader = screenReader; }
    }

    public static class PersonTypeUpdateRequest {
        private String personType; // OPEN_REFERRAL | CLOSED_REFERRAL | APPLICANT | RECIPIENT
        private String reason;

        public String getPersonType() { return personType; }
        public void setPersonType(String personType) { this.personType = personType; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
