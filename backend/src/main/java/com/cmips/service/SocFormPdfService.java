package com.cmips.service;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.ElectronicFormEntity;
import com.cmips.entity.ElectronicFormEntity.FormType;
import com.cmips.entity.RecipientEntity;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.ElectronicFormRepository;
import com.cmips.repository.RecipientRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * SOC Form PDF Service — DSD Section 25, CI-71055/67718/67782/67898
 *
 * Generates printable PDFs for all 22 electronic SOC form types tracked in
 * ElectronicFormEntity. Each form follows the standard CDSS layout:
 *   Header (blue bar, form number, agency name)
 *   Case / Recipient identification block
 *   Form-specific content section
 *   Signature / certification block
 *   Footer
 *
 * Form types: SOC 295, 295A, 296, 426, 426A, 432, 838–841, 846, 873,
 *             2303–2306, 2313, 2315, 2316, 2318, 2321, OTHER
 */
@Service
public class SocFormPdfService {

    private static final Logger log = LoggerFactory.getLogger(SocFormPdfService.class);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final Color HEADER_BLUE  = new Color(0, 56, 101);
    private static final Color RULE_BLUE    = new Color(0, 102, 179);
    private static final Color LIGHT_GRAY   = new Color(245, 245, 245);
    private static final Color BORDER_GRAY  = new Color(200, 200, 200);

    // Fonts
    private static final Font F_WHITE_BOLD  = new Font(Font.HELVETICA, 10, Font.BOLD,   Color.WHITE);
    private static final Font F_TITLE       = new Font(Font.HELVETICA, 13, Font.BOLD,   HEADER_BLUE);
    private static final Font F_SUBTITLE    = new Font(Font.HELVETICA, 10, Font.BOLD,   HEADER_BLUE);
    private static final Font F_BOLD        = new Font(Font.HELVETICA, 10, Font.BOLD,   Color.BLACK);
    private static final Font F_BODY        = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font F_SMALL       = new Font(Font.HELVETICA,  8, Font.NORMAL, Color.GRAY);
    private static final Font F_LABEL       = new Font(Font.HELVETICA,  9, Font.BOLD,   new Color(60, 60, 60));
    private static final Font F_FIELD       = new Font(Font.HELVETICA,  9, Font.NORMAL, Color.BLACK);

    private final ElectronicFormRepository formRepository;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;

    public SocFormPdfService(ElectronicFormRepository formRepository,
                              CaseRepository caseRepository,
                              RecipientRepository recipientRepository) {
        this.formRepository = formRepository;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    public byte[] generateFormPdf(Long formId) {
        ElectronicFormEntity form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found: " + formId));

        CaseEntity caseEntity = form.getCaseId() != null
                ? caseRepository.findById(form.getCaseId()).orElse(null) : null;
        RecipientEntity recipient = form.getRecipientId() != null
                ? recipientRepository.findById(form.getRecipientId()).orElse(null)
                : (caseEntity != null && caseEntity.getRecipientId() != null
                        ? recipientRepository.findById(caseEntity.getRecipientId()).orElse(null) : null);

        try {
            byte[] pdf = buildPdf(form, caseEntity, recipient);
            log.info("[SOC-PDF] Generated {} for form id={}", form.getFormType(), formId);
            return pdf;
        } catch (Exception ex) {
            log.error("[SOC-PDF] Failed to generate form {}: {}", formId, ex.getMessage());
            throw new RuntimeException("PDF generation failed: " + ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PDF Construction
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] buildPdf(ElectronicFormEntity form, CaseEntity c, RecipientEntity r)
            throws DocumentException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 54, 54, 54, 54);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        String formNumber = resolveFormNumber(form.getFormType());
        String formTitle  = resolveFormTitle(form.getFormType());

        addHeader(doc, formNumber, formTitle);
        addCaseBlock(doc, form, c, r);
        addRuleLine(doc);
        addFormBody(doc, form, c, r, formNumber, formTitle);
        addSignatureBlock(doc, form.getFormType());
        addFooter(doc, formNumber);

        doc.close();
        return baos.toByteArray();
    }

    // ── 1. Header ─────────────────────────────────────────────────────────────

    private void addHeader(Document doc, String formNumber, String formTitle) throws DocumentException {
        PdfPTable tbl = new PdfPTable(new float[]{4f, 1f});
        tbl.setWidthPercentage(100);
        tbl.setSpacingAfter(8);

        PdfPCell agencyCell = new PdfPCell();
        agencyCell.setBackgroundColor(HEADER_BLUE);
        agencyCell.setPadding(8);
        agencyCell.setBorder(Rectangle.NO_BORDER);
        agencyCell.addElement(new Paragraph("STATE OF CALIFORNIA — HEALTH AND HUMAN SERVICES AGENCY",
                new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE)));
        agencyCell.addElement(new Paragraph("Department of Social Services",
                new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE)));
        tbl.addCell(agencyCell);

        PdfPCell numCell = new PdfPCell(new Phrase(formNumber, F_WHITE_BOLD));
        numCell.setBackgroundColor(HEADER_BLUE);
        numCell.setPadding(8);
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tbl.addCell(numCell);

        doc.add(tbl);

        // Rule line
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(10);
        PdfPCell rc = new PdfPCell();
        rc.setBackgroundColor(RULE_BLUE);
        rc.setFixedHeight(3f);
        rc.setBorder(Rectangle.NO_BORDER);
        rule.addCell(rc);
        doc.add(rule);

        Paragraph title = new Paragraph(formTitle, F_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        doc.add(title);
    }

    // ── 2. Case / Recipient identification block ───────────────────────────────

    private void addCaseBlock(Document doc, ElectronicFormEntity form, CaseEntity c, RecipientEntity r)
            throws DocumentException {

        PdfPTable tbl = new PdfPTable(new float[]{1f, 1f, 1f});
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(8);
        tbl.setSpacingAfter(8);

        String caseNum   = c != null && c.getCaseNumber() != null ? c.getCaseNumber() : "—";
        String county    = c != null && c.getCountyCode() != null ? c.getCountyCode() + " County" : "—";
        String recipName = buildRecipientName(r);
        String dateStr   = LocalDate.now().format(FMT);

        addInfoCell(tbl, "Case Number",    caseNum);
        addInfoCell(tbl, "County",         county);
        addInfoCell(tbl, "Date Prepared",  dateStr);
        addInfoCell(tbl, "Recipient Name", recipName);
        addInfoCell(tbl, "Form Type",      form.getFormType() != null ? form.getFormType().name() : "OTHER");
        addInfoCell(tbl, "Language",       form.getLanguage() != null ? form.getLanguage().name() : "ENGLISH");

        doc.add(tbl);
    }

    private void addInfoCell(PdfPTable tbl, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorderColor(BORDER_GRAY);
        cell.setPadding(5);
        cell.addElement(new Phrase(label, F_LABEL));
        cell.addElement(new Phrase(value != null ? value : "—", F_FIELD));
        tbl.addCell(cell);
    }

    // ── 3. Rule line ───────────────────────────────────────────────────────────

    private void addRuleLine(Document doc) throws DocumentException {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(10);
        PdfPCell rc = new PdfPCell();
        rc.setBackgroundColor(RULE_BLUE);
        rc.setFixedHeight(2f);
        rc.setBorder(Rectangle.NO_BORDER);
        rule.addCell(rc);
        doc.add(rule);
    }

    // ── 4. Form-specific body ──────────────────────────────────────────────────

    private void addFormBody(Document doc, ElectronicFormEntity form, CaseEntity c, RecipientEntity r,
                             String formNumber, String formTitle) throws DocumentException {
        FormType ft = form.getFormType();
        if (ft == null) ft = FormType.OTHER;

        switch (ft) {
            case SOC_295, SOC_295A -> addApplication(doc, ft, c, r);
            case SOC_296            -> addDetermination(doc, c);
            case SOC_426, SOC_426A  -> addNoticeOfAction426(doc, c);
            case SOC_432            -> addContractorAuth(doc, c);
            case SOC_838            -> addRecipientDeclaration(doc, r, "SOC 838 — Recipient Program Declaration");
            case SOC_839            -> addRecipientDeclaration(doc, r, "SOC 839 — Recipient Declaration of Need");
            case SOC_840            -> addRecipientDeclaration(doc, r, "SOC 840 — Independent Provider Agreement");
            case SOC_841            -> addRecipientDeclaration(doc, r, "SOC 841 — Recipient Payroll Designation");
            case SOC_846            -> addMedicalCertification(doc, c, r);
            case SOC_873            -> addPhysicianReport(doc, c, r, form.getNotes());
            case SOC_2303           -> addVoterRegistration(doc, r);
            case SOC_2304           -> addConsentRelease(doc, r, "SOC 2304");
            case SOC_2305           -> addProviderNoticeForms(doc, c, "SOC 2305 — Provider Employment Notice");
            case SOC_2306           -> addProviderNoticeForms(doc, c, "SOC 2306 — Provider Rights and Responsibilities");
            case SOC_2313           -> addAssessment(doc, c, r);
            case SOC_2315           -> addConsentRelease(doc, r, "SOC 2315");
            case SOC_2316           -> addConsentRelease(doc, r, "SOC 2316 — Authorization for Release of Information");
            case SOC_2318           -> addConsentRelease(doc, r, "SOC 2318 — Consent for Background Check");
            case SOC_2321           -> addEspInactivation(doc, c, r);
            default                 -> addGenericBody(doc, formTitle);
        }
    }

    // ── Form-specific sections ─────────────────────────────────────────────────

    private void addApplication(Document doc, FormType ft, CaseEntity c, RecipientEntity r)
            throws DocumentException {
        doc.add(new Paragraph("APPLICATION FOR IN-HOME SUPPORTIVE SERVICES", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        if (ft == FormType.SOC_295A) {
            doc.add(new Paragraph("SUPPLEMENT — Additional household and functional information.", F_BODY));
            doc.add(Chunk.NEWLINE);
        }
        addFieldBlock(doc, new String[][]{
            {"Full Legal Name", buildRecipientName(r)},
            {"Case Number", c != null ? c.getCaseNumber() : ""},
            {"County", c != null ? c.getCountyCode() : ""},
            {"Application Date", LocalDate.now().format(FMT)},
            {"Program", "IHSS"},
        });
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "The applicant requests In-Home Supportive Services as authorized under " +
            "Welfare and Institutions Code Section 12300 et seq. The applicant certifies that all " +
            "information provided is true and correct to the best of their knowledge.", F_BODY));
    }

    private void addDetermination(Document doc, CaseEntity c) throws DocumentException {
        doc.add(new Paragraph("NOTICE OF PROGRAM DETERMINATION", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This form confirms the determination of your In-Home Supportive Services application " +
            "or annual review. Please retain this document for your records.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Case Number", c != null ? c.getCaseNumber() : ""},
            {"Determination Date", LocalDate.now().format(FMT)},
            {"Authorized Hours/Month", c != null && c.getAuthorizedHoursMonthly() != null
                    ? String.format("%.0f", c.getAuthorizedHoursMonthly()) : "—"},
        });
    }

    private void addNoticeOfAction426(Document doc, CaseEntity c) throws DocumentException {
        doc.add(new Paragraph("NOTICE OF ACTION — SHARE OF COST / BENEFIT CHANGE", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "Your IHSS benefits have been reviewed. This notice informs you of a change " +
            "to your authorized services or share of cost obligation.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Case Number", c != null ? c.getCaseNumber() : ""},
            {"Effective Date", LocalDate.now().format(FMT)},
            {"Share of Cost", c != null && c.getShareOfCostAmount() != null
                    ? String.format("$%.2f", c.getShareOfCostAmount()) : "$0.00"},
        });
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "If you disagree with this action you have the right to request a State Hearing " +
            "within 90 days of this notice by calling 1-800-952-5253.", F_BODY));
    }

    private void addContractorAuth(Document doc, CaseEntity c) throws DocumentException {
        doc.add(new Paragraph("COUNTY CONTRACTOR AUTHORIZATION — SOC 432", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This form authorizes payment to the county contractor for services provided " +
            "to the recipient identified above. The county certifies that services were rendered " +
            "and that the invoice amount is correct.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Case Number",       c != null ? c.getCaseNumber() : ""},
            {"Authorization Date", LocalDate.now().format(FMT)},
            {"County",            c != null ? c.getCountyCode() : ""},
        });
    }

    private void addRecipientDeclaration(Document doc, RecipientEntity r, String title)
            throws DocumentException {
        doc.add(new Paragraph(title, F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "I, the undersigned recipient, acknowledge receipt of this form and certify " +
            "that the information provided is accurate and complete.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Recipient Name", buildRecipientName(r)},
            {"Date",           LocalDate.now().format(FMT)},
        });
    }

    private void addMedicalCertification(Document doc, CaseEntity c, RecipientEntity r)
            throws DocumentException {
        doc.add(new Paragraph("MEDICAL CERTIFICATION OF NEED FOR SERVICES — SOC 846", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This form certifies the medical necessity of In-Home Supportive Services " +
            "for the recipient named above. Completion by a licensed health care provider is required.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Recipient Name", buildRecipientName(r)},
            {"Case Number",    c != null ? c.getCaseNumber() : ""},
            {"Date of Exam",   LocalDate.now().format(FMT)},
            {"Diagnosis",      ""},
            {"Prognosis",      ""},
        });
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Licensed Health Care Provider Certification:", F_BOLD));
        doc.add(new Paragraph(
            "I certify that the above-named individual requires in-home supportive services " +
            "due to the medical condition(s) listed and that these services are medically necessary.", F_BODY));
    }

    private void addPhysicianReport(Document doc, CaseEntity c, RecipientEntity r, String paramedicalText)
            throws DocumentException {
        doc.add(new Paragraph("PHYSICIAN'S REPORT — SOC 873", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This form is to be completed by the treating physician or licensed health care " +
            "professional for the IHSS recipient named above.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Recipient Name",    buildRecipientName(r)},
            {"Case Number",       c != null ? c.getCaseNumber() : ""},
            {"Date of Report",    LocalDate.now().format(FMT)},
            {"Primary Diagnosis", ""},
            {"Secondary Diagnosis",""},
            {"Functional Limitations", ""},
        });
        if (paramedicalText != null && !paramedicalText.isBlank()) {
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Paramedical Information:", F_BOLD));
            doc.add(new Paragraph(paramedicalText, F_BODY));
        }
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Physician Certification:", F_BOLD));
        doc.add(new Paragraph(
            "I certify that the medical information provided on this form is true, accurate, " +
            "and complete to the best of my knowledge and belief.", F_BODY));
    }

    private void addVoterRegistration(Document doc, RecipientEntity r) throws DocumentException {
        doc.add(new Paragraph("VOTER REGISTRATION — SOC 2303", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "California law requires county social services agencies to offer voter registration " +
            "services to all applicants and recipients. This form provides the opportunity to register " +
            "to vote or update your voter registration information.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Name",          buildRecipientName(r)},
            {"Date of Birth", ""},
            {"Party",         ""},
            {"Address",       ""},
        });
    }

    private void addConsentRelease(Document doc, RecipientEntity r, String formRef) throws DocumentException {
        String[] titles = {
            "SOC 2304" , "Consent for Release of Information",
            "SOC 2315" , "Authorization — Medi-Cal/IHSS Records",
            "SOC 2316" , "Authorization for Release of Information",
            "SOC 2318" , "Consent for Background Check — Provider",
        };
        String title = formRef;
        for (int i = 0; i < titles.length - 1; i += 2) {
            if (formRef.startsWith(titles[i])) { title = titles[i+1]; break; }
        }
        doc.add(new Paragraph(title.toUpperCase() + " — " + formRef, F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "I, the undersigned, authorize the California Department of Social Services and " +
            "the county IHSS office to obtain, release, and share information as described on " +
            "this form for the purpose of determining eligibility for and administering " +
            "In-Home Supportive Services.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Name", buildRecipientName(r)},
            {"Date", LocalDate.now().format(FMT)},
        });
    }

    private void addProviderNoticeForms(Document doc, CaseEntity c, String title) throws DocumentException {
        doc.add(new Paragraph(title, F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This notice contains important information about your rights and responsibilities " +
            "as an IHSS provider. Please read this document carefully and retain a copy for your records.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Case Number", c != null ? c.getCaseNumber() : ""},
            {"Date Issued", LocalDate.now().format(FMT)},
        });
    }

    private void addAssessment(Document doc, CaseEntity c, RecipientEntity r) throws DocumentException {
        doc.add(new Paragraph("FUNCTIONAL INDEX ASSESSMENT — SOC 2313", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This assessment documents the recipient's functional limitations and resulting " +
            "need for In-Home Supportive Services across all assessed service categories.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Recipient Name",    buildRecipientName(r)},
            {"Case Number",       c != null ? c.getCaseNumber() : ""},
            {"Assessment Date",   LocalDate.now().format(FMT)},
            {"Authorized Hours",  c != null && c.getAuthorizedHoursMonthly() != null
                    ? String.format("%.0f hrs/month", c.getAuthorizedHoursMonthly()) : "—"},
        });
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Service Category Ratings (1–5 scale):", F_BOLD));
        String[][] categories = {
            {"Domestic Services", ""},
            {"Personal Care", ""},
            {"Meal Preparation", ""},
            {"Protective Supervision", ""},
            {"Paramedical Services", ""},
        };
        addFieldBlock(doc, categories);
    }

    private void addEspInactivation(Document doc, CaseEntity c, RecipientEntity r) throws DocumentException {
        doc.add(new Paragraph("ESP ACCOUNT INACTIVATION NOTICE — SOC 2321", F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This notice is to inform you that your Electronic Services Portal (ESP) / " +
            "Electronic Timesheet account has been inactivated by the county IHSS office. " +
            "You will no longer be able to submit timesheets electronically until the account " +
            "is reactivated.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Recipient Name",      buildRecipientName(r)},
            {"Case Number",         c != null ? c.getCaseNumber() : ""},
            {"Inactivation Date",   LocalDate.now().format(FMT)},
            {"Action Required",     "Contact your county IHSS office to reactivate your account."},
        });
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "If you believe this action is in error, contact your county IHSS office at the " +
            "phone number listed on your benefit documents.", F_BODY));
    }

    private void addGenericBody(Document doc, String formTitle) throws DocumentException {
        doc.add(new Paragraph(formTitle, F_SUBTITLE));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "This form has been issued by the California Department of Social Services for the " +
            "IHSS recipient and case identified above. Please retain for your records.", F_BODY));
        doc.add(Chunk.NEWLINE);
        addFieldBlock(doc, new String[][]{
            {"Date Issued", LocalDate.now().format(FMT)},
        });
    }

    // ── 5. Signature block ─────────────────────────────────────────────────────

    private void addSignatureBlock(Document doc, FormType ft) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        addRuleLine(doc);

        boolean needsRecipientSig = ft != FormType.SOC_432 && ft != FormType.SOC_873
                && ft != FormType.SOC_846 && ft != FormType.SOC_2305 && ft != FormType.SOC_2306;
        boolean needsProviderSig  = ft == FormType.SOC_840 || ft == FormType.SOC_841 || ft == FormType.SOC_2318;
        boolean needsPhysicianSig = ft == FormType.SOC_873 || ft == FormType.SOC_846;
        boolean needsWorkerSig    = ft == FormType.SOC_432 || ft == FormType.SOC_2313;

        PdfPTable sigTbl = new PdfPTable(2);
        sigTbl.setWidthPercentage(100);
        sigTbl.setSpacingBefore(6);

        if (needsRecipientSig) {
            addSigCell(sigTbl, "Recipient Signature");
            addSigCell(sigTbl, "Date");
        }
        if (needsProviderSig) {
            addSigCell(sigTbl, "Provider Signature");
            addSigCell(sigTbl, "Date");
        }
        if (needsPhysicianSig) {
            addSigCell(sigTbl, "Physician / Provider Signature");
            addSigCell(sigTbl, "License Number");
            addSigCell(sigTbl, "Printed Name");
            addSigCell(sigTbl, "Date");
        }
        if (needsWorkerSig) {
            addSigCell(sigTbl, "Social Worker Signature");
            addSigCell(sigTbl, "Date");
        }
        doc.add(sigTbl);
    }

    private void addSigCell(PdfPTable tbl, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_GRAY);
        cell.setPadding(4);
        cell.setFixedHeight(36f);
        cell.addElement(new Phrase(label, F_LABEL));
        tbl.addCell(cell);
    }

    // ── 6. Footer ──────────────────────────────────────────────────────────────

    private void addFooter(Document doc, String formNumber) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            formNumber + " (Rev. 01/2026)  |  State of California — Health and Human Services Agency — " +
            "Department of Social Services  |  Page 1 of 1", F_SMALL);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    private void addFieldBlock(Document doc, String[][] fields) throws DocumentException {
        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(100);
        tbl.setSpacingBefore(4);
        tbl.setSpacingAfter(4);
        for (String[] row : fields) {
            PdfPCell labelCell = new PdfPCell(new Phrase(row[0], F_LABEL));
            labelCell.setBackgroundColor(LIGHT_GRAY);
            labelCell.setBorderColor(BORDER_GRAY);
            labelCell.setPadding(4);
            tbl.addCell(labelCell);

            String val = row.length > 1 ? row[1] : "";
            PdfPCell valCell = new PdfPCell();
            valCell.setBorderColor(BORDER_GRAY);
            valCell.setPadding(4);
            valCell.setFixedHeight(val != null && val.isBlank() ? 24f : 0f);
            if (val != null && !val.isBlank()) {
                valCell.addElement(new Phrase(val, F_FIELD));
            }
            tbl.addCell(valCell);
        }
        doc.add(tbl);
    }

    private String buildRecipientName(RecipientEntity r) {
        if (r == null) return "—";
        StringBuilder sb = new StringBuilder();
        if (r.getFirstName() != null) sb.append(r.getFirstName()).append(" ");
        if (r.getLastName() != null)  sb.append(r.getLastName());
        String name = sb.toString().trim();
        return name.isBlank() ? "—" : name;
    }

    // ── Form metadata ──────────────────────────────────────────────────────────

    private String resolveFormNumber(FormType ft) {
        if (ft == null) return "SOC";
        return switch (ft) {
            case SOC_295   -> "SOC 295";
            case SOC_295A  -> "SOC 295A";
            case SOC_296   -> "SOC 296";
            case SOC_426   -> "SOC 426";
            case SOC_426A  -> "SOC 426A";
            case SOC_432   -> "SOC 432";
            case SOC_838   -> "SOC 838";
            case SOC_839   -> "SOC 839";
            case SOC_840   -> "SOC 840";
            case SOC_841   -> "SOC 841";
            case SOC_846   -> "SOC 846";
            case SOC_873   -> "SOC 873";
            case SOC_2303  -> "SOC 2303";
            case SOC_2304  -> "SOC 2304";
            case SOC_2305  -> "SOC 2305";
            case SOC_2306  -> "SOC 2306";
            case SOC_2313  -> "SOC 2313";
            case SOC_2315  -> "SOC 2315";
            case SOC_2316  -> "SOC 2316";
            case SOC_2318  -> "SOC 2318";
            case SOC_2321  -> "SOC 2321";
            default        -> "SOC";
        };
    }

    private String resolveFormTitle(FormType ft) {
        if (ft == null) return "IHSS FORM";
        return switch (ft) {
            case SOC_295   -> "APPLICATION FOR IN-HOME SUPPORTIVE SERVICES";
            case SOC_295A  -> "APPLICATION FOR IHSS — SUPPLEMENTAL INFORMATION";
            case SOC_296   -> "NOTICE OF PROGRAM DETERMINATION";
            case SOC_426   -> "NOTICE OF ACTION — IHSS BENEFIT CHANGE";
            case SOC_426A  -> "NOTICE OF ACTION — SUPPLEMENTAL";
            case SOC_432   -> "COUNTY CONTRACTOR AUTHORIZATION";
            case SOC_838   -> "RECIPIENT PROGRAM DECLARATION";
            case SOC_839   -> "RECIPIENT DECLARATION OF NEED";
            case SOC_840   -> "INDEPENDENT PROVIDER AGREEMENT";
            case SOC_841   -> "RECIPIENT PAYROLL DESIGNATION";
            case SOC_846   -> "MEDICAL CERTIFICATION OF NEED";
            case SOC_873   -> "PHYSICIAN'S REPORT";
            case SOC_2303  -> "VOTER REGISTRATION OPPORTUNITY";
            case SOC_2304  -> "CONSENT FOR RELEASE OF INFORMATION";
            case SOC_2305  -> "PROVIDER EMPLOYMENT NOTICE";
            case SOC_2306  -> "PROVIDER RIGHTS AND RESPONSIBILITIES";
            case SOC_2313  -> "FUNCTIONAL INDEX ASSESSMENT";
            case SOC_2315  -> "AUTHORIZATION — MEDI-CAL / IHSS RECORDS";
            case SOC_2316  -> "AUTHORIZATION FOR RELEASE OF INFORMATION";
            case SOC_2318  -> "CONSENT FOR BACKGROUND CHECK";
            case SOC_2321  -> "ESP ACCOUNT INACTIVATION NOTICE";
            default        -> "IN-HOME SUPPORTIVE SERVICES FORM";
        };
    }
}
