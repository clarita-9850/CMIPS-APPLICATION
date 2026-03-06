package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Electronic Form Entity — DSD Section 25, CI-71055/67718/67782/67898/507512/67891/507511
 *
 * Tracks form requests for IHSS recipients/cases.
 * BVI format options per recipient blind/visually impaired status.
 * Print method: immediate PDF or nightly batch processing.
 * Paramedical text supported for SOC 873 forms.
 */
@Entity
@Table(name = "electronic_forms", indexes = {
        @Index(name = "idx_ef_case", columnList = "case_id"),
        @Index(name = "idx_ef_status", columnList = "status")
})
public class ElectronicFormEntity {

    public enum FormType {
        SOC_295, SOC_295A, SOC_296, SOC_873, SOC_2303, SOC_2304, SOC_2305, SOC_2306,
        SOC_2313, SOC_426, SOC_426A, SOC_432, SOC_838, SOC_839, SOC_840, SOC_841,
        SOC_846, SOC_2315, SOC_2316, SOC_2318, SOC_2321, OTHER
    }

    public enum Language { ENGLISH, SPANISH, CHINESE, ARMENIAN }

    public enum BviFormat { STANDARD, LARGE_FONT, BRAILLE, AUDIO_CD, DATA_CD }

    public enum PrintMethod { PRINT_NOW, NIGHTLY_BATCH }

    public enum FormStatus { PENDING, PRINTED, NOT_MAILED, INACTIVATED, SUPPRESSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", length = 30, nullable = false)
    private FormType formType;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20)
    private Language language = Language.ENGLISH;

    @Enumerated(EnumType.STRING)
    @Column(name = "bvi_format", length = 20)
    private BviFormat bviFormat = BviFormat.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_method", length = 20)
    private PrintMethod printMethod = PrintMethod.PRINT_NOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FormStatus status = FormStatus.PENDING;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "print_date")
    private LocalDate printDate;

    @Column(name = "mailed_date")
    private LocalDate mailedDate;

    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    /** Paramedical text — used for SOC 873 forms */
    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestDate == null) requestDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public FormType getFormType() { return formType; }
    public void setFormType(FormType formType) { this.formType = formType; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public BviFormat getBviFormat() { return bviFormat; }
    public void setBviFormat(BviFormat bviFormat) { this.bviFormat = bviFormat; }
    public PrintMethod getPrintMethod() { return printMethod; }
    public void setPrintMethod(PrintMethod printMethod) { this.printMethod = printMethod; }
    public FormStatus getStatus() { return status; }
    public void setStatus(FormStatus status) { this.status = status; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public LocalDate getPrintDate() { return printDate; }
    public void setPrintDate(LocalDate printDate) { this.printDate = printDate; }
    public LocalDate getMailedDate() { return mailedDate; }
    public void setMailedDate(LocalDate mailedDate) { this.mailedDate = mailedDate; }
    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }
    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
