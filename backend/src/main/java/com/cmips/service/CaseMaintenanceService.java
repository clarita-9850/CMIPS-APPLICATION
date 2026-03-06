package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.CountyContractorInvoiceEntity.InvoiceStatus;
import com.cmips.entity.ElectronicFormEntity.*;
import com.cmips.entity.OvertimeAgreementEntity.AgreementStatus;
import com.cmips.entity.OvertimeAgreementEntity.AgreementType;
import com.cmips.entity.WPCSHoursEntity.FundingSource;
import com.cmips.entity.WPCSHoursEntity.HoursStatus;
import com.cmips.entity.WorkweekAgreementEntity;
import com.cmips.entity.ESPRegistrationEntity;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Case Maintenance Service — DSD Section 25
 *
 * Handles all case maintenance features:
 * - Workweek Agreements (case view — CI-480925/916/917/927/928/924)
 * - Overtime Agreements (CI-480922/921/926/918)
 * - WPCS Hours (CI-67731)
 * - Workplace Hours (CI-67727)
 * - County Contractor Invoices / SOC 432 (CI-67732)
 * - Electronic Forms (CI-71055 etc.)
 * - E-Timesheet (ESP) Enrollment Management (CI-795491)
 * - Reassessment workflow
 * - Medi-Cal SOC Point of Service (CI-67574 etc.)
 */
@Service
public class CaseMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(CaseMaintenanceService.class);

    private final WorkweekAgreementRepository workweekAgreementRepository;
    private final OvertimeAgreementRepository overtimeAgreementRepository;
    private final WPCSHoursRepository wpcsHoursRepository;
    private final WorkplaceHoursRepository workplaceHoursRepository;
    private final CountyContractorRepository contractorRepository;
    private final ElectronicFormRepository formRepository;
    private final ESPRegistrationRepository espRegistrationRepository;
    private final CaseRepository caseRepository;
    private final ProviderRepository providerRepository;

    public CaseMaintenanceService(WorkweekAgreementRepository workweekAgreementRepository,
                                   OvertimeAgreementRepository overtimeAgreementRepository,
                                   WPCSHoursRepository wpcsHoursRepository,
                                   WorkplaceHoursRepository workplaceHoursRepository,
                                   CountyContractorRepository contractorRepository,
                                   ElectronicFormRepository formRepository,
                                   ESPRegistrationRepository espRegistrationRepository,
                                   CaseRepository caseRepository,
                                   ProviderRepository providerRepository) {
        this.workweekAgreementRepository = workweekAgreementRepository;
        this.overtimeAgreementRepository = overtimeAgreementRepository;
        this.wpcsHoursRepository = wpcsHoursRepository;
        this.workplaceHoursRepository = workplaceHoursRepository;
        this.contractorRepository = contractorRepository;
        this.formRepository = formRepository;
        this.espRegistrationRepository = espRegistrationRepository;
        this.caseRepository = caseRepository;
        this.providerRepository = providerRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // Workweek Agreements (case view)
    // ─────────────────────────────────────────────────────────────

    /** Get all active workweek agreements for a case's recipient */
    public List<WorkweekAgreementEntity> getWorkweekAgreementsForCase(Long caseId) {
        CaseEntity c = getCaseOrThrow(caseId);
        return workweekAgreementRepository.findByRecipientId(c.getRecipientId());
    }

    /** Get inactive (history) workweek agreements for a case */
    public List<WorkweekAgreementEntity> getWorkweekAgreementHistoryForCase(Long caseId) {
        CaseEntity c = getCaseOrThrow(caseId);
        return workweekAgreementRepository.findInactiveHistoryForRecipient(c.getRecipientId());
    }

    @Transactional
    public WorkweekAgreementEntity createWorkweekAgreement(Long caseId, Map<String, Object> request,
                                                            String createdBy) {
        if (request.get("beginDate") == null) throw new IllegalArgumentException("Begin Date is required.");
        if (request.get("agreedHoursWeekly") == null) throw new IllegalArgumentException("Agreed Weekly Hours is required.");

        LocalDate beginDate = LocalDate.parse((String) request.get("beginDate"));
        LocalDate endDate = request.get("endDate") != null
                ? LocalDate.parse((String) request.get("endDate"))
                : LocalDate.of(9999, 12, 31);

        if (endDate.isBefore(beginDate)) {
            throw new IllegalArgumentException("End Date must be on or after Begin Date.");
        }

        Long providerId = request.get("providerId") != null
                ? ((Number) request.get("providerId")).longValue() : null;
        // Fall back: look up provider by providerNumber if providerId not given
        if (providerId == null && request.get("providerNumber") != null) {
            String providerNumber = (String) request.get("providerNumber");
            providerId = providerRepository.findByProviderNumber(providerNumber)
                    .map(p -> p.getId()).orElse(null);
        }
        if (providerId == null) {
            throw new IllegalArgumentException("Provider not found. Ensure the provider is assigned to this case.");
        }
        double agreedHoursWeekly = ((Number) request.get("agreedHoursWeekly")).doubleValue();

        // Inactivate any existing active agreement for this provider
        if (providerId != null) {
            workweekAgreementRepository.findActiveAgreementForProvider(providerId).ifPresent(existing -> {
                existing.setStatus(WorkweekAgreementEntity.AgreementStatus.INACTIVE);
                existing.setInactivatedDate(LocalDate.now());
                existing.setInactivatedBy(createdBy);
                existing.setInactivationReason("Superseded by new agreement");
                workweekAgreementRepository.save(existing);
            });
        }

        CaseEntity c = getCaseOrThrow(caseId);

        WorkweekAgreementEntity agreement = new WorkweekAgreementEntity();
        agreement.setProviderId(providerId);
        agreement.setRecipientId(c.getRecipientId());
        agreement.setCaseNumber(c.getCaseNumber());
        agreement.setBeginDate(beginDate);
        agreement.setEndDate(endDate);
        agreement.setAgreedHoursWeekly(agreedHoursWeekly);
        agreement.setStatus(WorkweekAgreementEntity.AgreementStatus.ACTIVE);
        agreement.setCreatedBy(createdBy);
        agreement.setUpdatedBy(createdBy);

        if (request.get("workweekStartDay") != null) {
            agreement.setWorkweekStartDay((String) request.get("workweekStartDay"));
        }
        if (request.get("includesTravelTime") != null) {
            agreement.setIncludesTravelTime((Boolean) request.get("includesTravelTime"));
        }
        if (request.get("travelHoursWeekly") != null) {
            agreement.setTravelHoursWeekly(((Number) request.get("travelHoursWeekly")).doubleValue());
        }

        WorkweekAgreementEntity saved = workweekAgreementRepository.save(agreement);
        log.info("[CaseMaint] Workweek agreement created: caseId={}, id={}", caseId, saved.getId());
        return saved;
    }

    @Transactional
    public WorkweekAgreementEntity updateWorkweekAgreement(Long id, Map<String, Object> request,
                                                            String updatedBy) {
        WorkweekAgreementEntity agreement = workweekAgreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workweek agreement not found: " + id));

        if (agreement.getStatus() != WorkweekAgreementEntity.AgreementStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE workweek agreements can be edited.");
        }

        if (request.get("beginDate") != null) {
            agreement.setBeginDate(LocalDate.parse((String) request.get("beginDate")));
        }
        if (request.get("endDate") != null) {
            agreement.setEndDate(LocalDate.parse((String) request.get("endDate")));
        }
        if (request.get("agreedHoursWeekly") != null) {
            agreement.setAgreedHoursWeekly(((Number) request.get("agreedHoursWeekly")).doubleValue());
        }

        if (agreement.getEndDate() != null && agreement.getEndDate().isBefore(agreement.getBeginDate())) {
            throw new IllegalArgumentException("End Date must be on or after Begin Date.");
        }

        agreement.setUpdatedBy(updatedBy);
        return workweekAgreementRepository.save(agreement);
    }

    @Transactional
    public WorkweekAgreementEntity inactivateWorkweekAgreement(Long id, String reason, String inactivatedBy) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Inactivation Reason is required.");
        }
        WorkweekAgreementEntity agreement = workweekAgreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workweek agreement not found: " + id));

        if (agreement.getStatus() != WorkweekAgreementEntity.AgreementStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE workweek agreements can be inactivated.");
        }

        agreement.setStatus(WorkweekAgreementEntity.AgreementStatus.INACTIVE);
        agreement.setInactivatedDate(LocalDate.now());
        agreement.setInactivatedBy(inactivatedBy);
        agreement.setInactivationReason(reason);
        agreement.setUpdatedBy(inactivatedBy);

        log.info("[CaseMaint] Workweek agreement inactivated: id={}", id);
        return workweekAgreementRepository.save(agreement);
    }

    // ─────────────────────────────────────────────────────────────
    // Overtime Agreements
    // ─────────────────────────────────────────────────────────────

    public List<OvertimeAgreementEntity> getOvertimeAgreements(Long caseId) {
        return overtimeAgreementRepository.findByCaseIdOrderByDateReceivedDesc(caseId);
    }

    @Transactional
    public OvertimeAgreementEntity createOvertimeAgreement(Long caseId, Map<String, Object> request,
                                                            String createdBy) {
        if (request.get("dateReceived") == null) throw new IllegalArgumentException("Date Received is required.");
        if (request.get("agreementType") == null) throw new IllegalArgumentException("Agreement Type is required.");

        LocalDate dateReceived = LocalDate.parse((String) request.get("dateReceived"));
        LocalDate minDate = LocalDate.of(2014, 11, 1);

        if (dateReceived.isBefore(minDate)) {
            throw new IllegalArgumentException("Date Received cannot be before 11/01/2014.");
        }
        if (dateReceived.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date Received cannot be in the future.");
        }

        OvertimeAgreementEntity agreement = new OvertimeAgreementEntity();
        agreement.setCaseId(caseId);

        try {
            agreement.setAgreementType(AgreementType.valueOf((String) request.get("agreementType")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Agreement Type: " + request.get("agreementType"));
        }

        if (request.get("providerId") != null) {
            agreement.setProviderId(((Number) request.get("providerId")).longValue());
        }
        if (request.get("providerNumber") != null) {
            agreement.setProviderNumber((String) request.get("providerNumber"));
        }

        agreement.setDateReceived(dateReceived);
        agreement.setStatus(AgreementStatus.ACTIVE);
        agreement.setCreatedBy(createdBy);
        agreement.setUpdatedBy(createdBy);

        OvertimeAgreementEntity saved = overtimeAgreementRepository.save(agreement);
        log.info("[CaseMaint] Overtime agreement created: caseId={}, id={}, type={}",
                caseId, saved.getId(), agreement.getAgreementType());
        return saved;
    }

    @Transactional
    public OvertimeAgreementEntity inactivateOvertimeAgreement(Long id, String inactivatedBy) {
        OvertimeAgreementEntity agreement = overtimeAgreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Overtime agreement not found: " + id));

        if (agreement.getStatus() != AgreementStatus.ACTIVE) {
            throw new RuntimeException("Agreement is already inactive.");
        }

        agreement.setStatus(AgreementStatus.INACTIVE);
        agreement.setInactivatedDate(LocalDate.now());
        agreement.setInactivatedBy(inactivatedBy);
        agreement.setUpdatedBy(inactivatedBy);

        log.info("[CaseMaint] Overtime agreement inactivated: id={}", id);
        return overtimeAgreementRepository.save(agreement);
    }

    // ─────────────────────────────────────────────────────────────
    // WPCS Hours
    // ─────────────────────────────────────────────────────────────

    public List<WPCSHoursEntity> getWpcsHours(Long caseId) {
        return wpcsHoursRepository.findByCaseIdOrderByBeginDateDesc(caseId);
    }

    @Transactional
    public WPCSHoursEntity createWpcsHours(Long caseId, Map<String, Object> request, String createdBy) {
        if (request.get("beginDate") == null) throw new IllegalArgumentException("Begin Date is required.");
        if (request.get("authorizedHours") == null) throw new IllegalArgumentException("Authorized Hours is required.");

        LocalDate beginDate = LocalDate.parse((String) request.get("beginDate"));
        LocalDate endDate = request.get("endDate") != null
                ? LocalDate.parse((String) request.get("endDate")) : null;

        if (endDate != null && endDate.isBefore(beginDate)) {
            throw new IllegalArgumentException("End Date must be on or after Begin Date.");
        }

        WPCSHoursEntity wpcs = new WPCSHoursEntity();
        wpcs.setCaseId(caseId);
        wpcs.setBeginDate(beginDate);
        wpcs.setEndDate(endDate);
        wpcs.setAuthorizedHours(((Number) request.get("authorizedHours")).intValue());

        if (request.get("fundingSource") != null) {
            try { wpcs.setFundingSource(FundingSource.valueOf((String) request.get("fundingSource"))); }
            catch (Exception e) { throw new IllegalArgumentException("Invalid Funding Source: " + request.get("fundingSource")); }
        }

        wpcs.setStatus(HoursStatus.ACTIVE);
        wpcs.setCreatedBy(createdBy);
        wpcs.setUpdatedBy(createdBy);

        WPCSHoursEntity saved = wpcsHoursRepository.save(wpcs);
        log.info("[CaseMaint] WPCS hours created: caseId={}, id={}", caseId, saved.getId());
        return saved;
    }

    @Transactional
    public WPCSHoursEntity inactivateWpcsHours(Long id, String inactivatedBy) {
        WPCSHoursEntity wpcs = wpcsHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WPCS hours record not found: " + id));
        if (wpcs.getStatus() != HoursStatus.ACTIVE) {
            throw new RuntimeException("WPCS hours record is already inactive.");
        }
        wpcs.setStatus(HoursStatus.INACTIVE);
        wpcs.setInactivatedDate(LocalDate.now());
        wpcs.setInactivatedBy(inactivatedBy);
        wpcs.setUpdatedBy(inactivatedBy);
        return wpcsHoursRepository.save(wpcs);
    }

    // ─────────────────────────────────────────────────────────────
    // Workplace Hours
    // ─────────────────────────────────────────────────────────────

    public List<WorkplaceHoursEntity> getWorkplaceHours(Long caseId) {
        return workplaceHoursRepository.findByCaseIdOrderByBeginDateDesc(caseId);
    }

    @Transactional
    public WorkplaceHoursEntity createWorkplaceHours(Long caseId, Map<String, Object> request,
                                                      String createdBy) {
        if (request.get("beginDate") == null) throw new IllegalArgumentException("Begin Date is required.");
        if (request.get("workplaceHours") == null) throw new IllegalArgumentException("Workplace Hours is required.");

        LocalDate beginDate = LocalDate.parse((String) request.get("beginDate"));
        LocalDate endDate = request.get("endDate") != null
                ? LocalDate.parse((String) request.get("endDate")) : null;

        if (endDate != null && endDate.isBefore(beginDate)) {
            throw new IllegalArgumentException("End Date must be on or after Begin Date.");
        }

        WorkplaceHoursEntity wph = new WorkplaceHoursEntity();
        wph.setCaseId(caseId);
        wph.setBeginDate(beginDate);
        wph.setEndDate(endDate);
        wph.setWorkplaceHours(((Number) request.get("workplaceHours")).intValue());
        wph.setNotes((String) request.get("notes"));
        wph.setStatus(WorkplaceHoursEntity.HoursStatus.ACTIVE);
        wph.setCreatedBy(createdBy);
        wph.setUpdatedBy(createdBy);

        WorkplaceHoursEntity saved = workplaceHoursRepository.save(wph);
        log.info("[CaseMaint] Workplace hours created: caseId={}, id={}", caseId, saved.getId());
        return saved;
    }

    @Transactional
    public WorkplaceHoursEntity inactivateWorkplaceHours(Long id, String inactivatedBy) {
        WorkplaceHoursEntity wph = workplaceHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workplace hours record not found: " + id));
        if (wph.getStatus() != WorkplaceHoursEntity.HoursStatus.ACTIVE) {
            throw new RuntimeException("Workplace hours record is already inactive.");
        }
        wph.setStatus(WorkplaceHoursEntity.HoursStatus.INACTIVE);
        wph.setInactivatedDate(LocalDate.now());
        wph.setInactivatedBy(inactivatedBy);
        wph.setUpdatedBy(inactivatedBy);
        return workplaceHoursRepository.save(wph);
    }

    // ─────────────────────────────────────────────────────────────
    // County Contractor Invoices / SOC 432
    // ─────────────────────────────────────────────────────────────

    public List<CountyContractorInvoiceEntity> getContractorInvoices(Long caseId) {
        return contractorRepository.findByCaseIdOrderByInvoiceDateDesc(caseId);
    }

    @Transactional
    public CountyContractorInvoiceEntity createContractorInvoice(Long caseId, Map<String, Object> request,
                                                                   String createdBy) {
        if (request.get("contractorName") == null) throw new IllegalArgumentException("Contractor Name is required.");
        if (request.get("invoiceDate") == null) throw new IllegalArgumentException("Invoice Date is required.");

        CountyContractorInvoiceEntity invoice = new CountyContractorInvoiceEntity();
        invoice.setCaseId(caseId);
        invoice.setContractorName((String) request.get("contractorName"));
        invoice.setInvoiceDate(LocalDate.parse((String) request.get("invoiceDate")));
        invoice.setInvoiceNumber((String) request.get("invoiceNumber"));

        if (request.get("servicePeriodFrom") != null) {
            invoice.setServicePeriodFrom(LocalDate.parse((String) request.get("servicePeriodFrom")));
        }
        if (request.get("servicePeriodTo") != null) {
            invoice.setServicePeriodTo(LocalDate.parse((String) request.get("servicePeriodTo")));
        }
        if (request.get("invoiceAmount") != null) {
            invoice.setInvoiceAmount(new java.math.BigDecimal(request.get("invoiceAmount").toString()));
        }

        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setCreatedBy(createdBy);
        invoice.setUpdatedBy(createdBy);

        CountyContractorInvoiceEntity saved = contractorRepository.save(invoice);
        log.info("[CaseMaint] Contractor invoice created: caseId={}, id={}", caseId, saved.getId());
        return saved;
    }

    @Transactional
    public CountyContractorInvoiceEntity authorizeContractorInvoice(Long id, Map<String, Object> request,
                                                                     String updatedBy) {
        CountyContractorInvoiceEntity invoice = getContractorOrThrow(id);
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new RuntimeException("Only PENDING invoices can be authorized.");
        }

        if (request.get("warrantNumber") != null) invoice.setWarrantNumber((String) request.get("warrantNumber"));
        if (request.get("warrantDate") != null) {
            invoice.setWarrantDate(LocalDate.parse((String) request.get("warrantDate")));
        }
        invoice.setStatus(InvoiceStatus.AUTHORIZED);
        invoice.setUpdatedBy(updatedBy);

        log.info("[CaseMaint] Invoice authorized: id={}", id);
        return contractorRepository.save(invoice);
    }

    @Transactional
    public CountyContractorInvoiceEntity rejectContractorInvoice(Long id, String reason, String updatedBy) {
        CountyContractorInvoiceEntity invoice = getContractorOrThrow(id);
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new RuntimeException("Only PENDING invoices can be rejected.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection Reason is required.");
        }

        invoice.setStatus(InvoiceStatus.REJECTED);
        invoice.setRejectionReason(reason);
        invoice.setUpdatedBy(updatedBy);

        log.info("[CaseMaint] Invoice rejected: id={}", id);
        return contractorRepository.save(invoice);
    }

    private CountyContractorInvoiceEntity getContractorOrThrow(Long id) {
        return contractorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contractor invoice not found: " + id));
    }

    // ─────────────────────────────────────────────────────────────
    // Electronic Forms
    // ─────────────────────────────────────────────────────────────

    public List<ElectronicFormEntity> getElectronicForms(Long caseId) {
        return formRepository.findByCaseIdOrderByRequestDateDesc(caseId);
    }

    @Transactional
    public ElectronicFormEntity requestForm(Long caseId, Map<String, Object> request, String createdBy) {
        if (request.get("formType") == null) throw new IllegalArgumentException("Form Type is required.");

        CaseEntity c = getCaseOrThrow(caseId);

        ElectronicFormEntity form = new ElectronicFormEntity();
        form.setCaseId(caseId);
        form.setRecipientId(c.getRecipientId());

        try {
            form.setFormType(FormType.valueOf((String) request.get("formType")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Form Type: " + request.get("formType"));
        }

        if (request.get("language") != null) {
            try { form.setLanguage(Language.valueOf((String) request.get("language"))); }
            catch (Exception ignored) {}
        }
        if (request.get("bviFormat") != null) {
            try { form.setBviFormat(BviFormat.valueOf((String) request.get("bviFormat"))); }
            catch (Exception ignored) {}
        }
        if (request.get("printMethod") != null) {
            try { form.setPrintMethod(PrintMethod.valueOf((String) request.get("printMethod"))); }
            catch (Exception ignored) {}
        }
        if (request.get("notes") != null) {
            form.setNotes((String) request.get("notes"));
        }

        form.setStatus(FormStatus.PENDING);
        form.setCreatedBy(createdBy);
        form.setUpdatedBy(createdBy);

        // PRINT_NOW: immediately mark as PRINTED with today's date
        if (form.getPrintMethod() == PrintMethod.PRINT_NOW) {
            form.setStatus(FormStatus.PRINTED);
            form.setPrintDate(LocalDate.now());
        }

        ElectronicFormEntity saved = formRepository.save(form);
        log.info("[CaseMaint] Form requested: caseId={}, formType={}, id={}",
                caseId, form.getFormType(), saved.getId());
        return saved;
    }

    @Transactional
    public ElectronicFormEntity inactivateForm(Long id, String inactivatedBy) {
        ElectronicFormEntity form = getFormOrThrow(id);
        form.setStatus(FormStatus.INACTIVATED);
        form.setInactivatedDate(LocalDate.now());
        form.setInactivatedBy(inactivatedBy);
        form.setUpdatedBy(inactivatedBy);
        return formRepository.save(form);
    }

    @Transactional
    public ElectronicFormEntity suppressForm(Long id, String suppressedBy) {
        ElectronicFormEntity form = getFormOrThrow(id);
        form.setStatus(FormStatus.SUPPRESSED);
        form.setUpdatedBy(suppressedBy);
        return formRepository.save(form);
    }

    @Transactional
    public ElectronicFormEntity markFormPrinted(Long id, String updatedBy) {
        ElectronicFormEntity form = getFormOrThrow(id);
        form.setStatus(FormStatus.PRINTED);
        form.setPrintDate(LocalDate.now());
        form.setUpdatedBy(updatedBy);
        return formRepository.save(form);
    }

    private ElectronicFormEntity getFormOrThrow(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form record not found: " + id));
    }

    // ─────────────────────────────────────────────────────────────
    // E-Timesheet (ESP) Enrollment Management
    // ─────────────────────────────────────────────────────────────

    public List<ESPRegistrationEntity> getEspRegistrations(Long caseId) {
        CaseEntity c = getCaseOrThrow(caseId);
        return espRegistrationRepository.findByRecipientId(c.getRecipientId());
    }

    @Transactional
    public ESPRegistrationEntity inactivateEspRegistration(String espId, String reason, String inactivatedBy) {
        ESPRegistrationEntity esp = espRegistrationRepository.findById(espId)
                .orElseThrow(() -> new RuntimeException("ESP registration not found: " + espId));

        if (esp.getStatus() == ESPRegistrationEntity.RegistrationStatus.CANCELLED) {
            throw new RuntimeException("ESP registration is already inactivated.");
        }

        esp.setStatus(ESPRegistrationEntity.RegistrationStatus.CANCELLED);
        esp.setLastError("Inactivated by: " + inactivatedBy + (reason != null ? " — " + reason : ""));

        log.info("[CaseMaint] ESP registration inactivated: id={}", espId);
        return espRegistrationRepository.save(esp);
    }

    @Transactional
    public ESPRegistrationEntity reactivateEspRegistration(String espId, String reactivatedBy) {
        ESPRegistrationEntity esp = espRegistrationRepository.findById(espId)
                .orElseThrow(() -> new RuntimeException("ESP registration not found: " + espId));

        esp.setStatus(ESPRegistrationEntity.RegistrationStatus.COMPLETED);
        esp.setLastError(null);

        log.info("[CaseMaint] ESP registration reactivated: id={}", espId);
        return espRegistrationRepository.save(esp);
    }

    // ─────────────────────────────────────────────────────────────
    // Reassessment
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public CaseEntity scheduleReassessment(Long caseId, LocalDate reassessmentDueDate, String updatedBy) {
        if (reassessmentDueDate == null) {
            throw new IllegalArgumentException("Reassessment Due Date is required.");
        }
        if (reassessmentDueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reassessment Due Date cannot be in the past.");
        }

        CaseEntity c = getCaseOrThrow(caseId);
        c.setReassessmentDueDate(reassessmentDueDate);
        c.setUpdatedBy(updatedBy);

        CaseEntity saved = caseRepository.save(c);
        log.info("[CaseMaint] Reassessment scheduled: caseId={}, dueDate={}", caseId, reassessmentDueDate);
        return saved;
    }

    // ─────────────────────────────────────────────────────────────
    // Medi-Cal SOC
    // ─────────────────────────────────────────────────────────────

    public Map<String, Object> getMediCalSoc(Long caseId) {
        CaseEntity c = getCaseOrThrow(caseId);
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("caseId", c.getId());
        result.put("caseNumber", c.getCaseNumber() != null ? c.getCaseNumber() : "");
        result.put("shareOfCostAmount", c.getShareOfCostAmount() != null ? c.getShareOfCostAmount() : 0);
        result.put("countableIncome", c.getCountableIncome() != null ? c.getCountableIncome() : 0);
        result.put("netIncome", c.getNetIncome() != null ? c.getNetIncome() : 0);
        result.put("reassessmentDueDate", c.getReassessmentDueDate());
        return result;
    }

    @Transactional
    public CaseEntity updateMediCalSoc(Long caseId, Map<String, Object> request, String updatedBy) {
        CaseEntity c = getCaseOrThrow(caseId);

        if (request.get("shareOfCostAmount") != null) {
            c.setShareOfCostAmount(((Number) request.get("shareOfCostAmount")).doubleValue());
        }
        if (request.get("countableIncome") != null) {
            c.setCountableIncome(((Number) request.get("countableIncome")).doubleValue());
        }
        if (request.get("netIncome") != null) {
            c.setNetIncome(((Number) request.get("netIncome")).doubleValue());
        }
        c.setUpdatedBy(updatedBy);

        log.info("[CaseMaint] Medi-Cal SOC updated: caseId={}", caseId);
        return caseRepository.save(c);
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────

    private CaseEntity getCaseOrThrow(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));
    }
}
