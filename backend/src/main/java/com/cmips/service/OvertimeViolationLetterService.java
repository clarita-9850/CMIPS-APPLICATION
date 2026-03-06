package com.cmips.service;

import com.cmips.entity.OvertimeViolationEntity;
import com.cmips.entity.ProviderEntity;
import com.cmips.repository.OvertimeViolationRepository;
import com.cmips.repository.ProviderRepository;
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
 * Overtime Violation Letter Service — DSD Section 23
 *
 * Generates PDF notices sent to providers when overtime violations are detected.
 * Covers all violation stages per DSD Appendix / BR PVM 90-190:
 *
 *   SOC 2257 — 1st Violation Notice (informational)
 *   SOC 2258 — 2nd Violation Notice (required training)
 *   SOC 2259 — 3rd Violation Notice (90-day suspension)
 *   SOC 2260 — 4th Violation Notice (permanent termination)
 *   SOC 2261 — County Dispute Decision (uphold/overturn)
 *   SOC 2262 — Supervisor Review Decision
 *   SOC 2263 — CDSS Review Decision
 *
 * All letters are generated using OpenPDF (lowagie) and returned as byte arrays.
 */
@Service
public class OvertimeViolationLetterService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeViolationLetterService.class);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final Color HEADER_BLUE = new Color(0, 56, 101);
    private static final Color RULE_BLUE   = new Color(0, 102, 179);
    private static final Color LIGHT_GRAY  = new Color(245, 245, 245);

    private static final Font FONT_HEADER  = new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE);
    private static final Font FONT_TITLE   = new Font(Font.HELVETICA, 12, Font.BOLD, HEADER_BLUE);
    private static final Font FONT_LABEL   = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_BODY    = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL   = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
    private static final Font FONT_BOLD    = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_WARNING = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(153, 0, 0));

    private final OvertimeViolationRepository violationRepository;
    private final ProviderRepository providerRepository;

    public OvertimeViolationLetterService(OvertimeViolationRepository violationRepository,
                                          ProviderRepository providerRepository) {
        this.violationRepository = violationRepository;
        this.providerRepository = providerRepository;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generate the appropriate SOC letter for a given violation record.
     * Letter type is determined by violationNumber + review stage.
     *
     * @param violationId   OvertimeViolationEntity id
     * @param letterType    One of: VIOLATION_NOTICE, COUNTY_DISPUTE, SUPERVISOR_REVIEW, CDSS_REVIEW
     * @return PDF bytes
     */
    public byte[] generateLetter(Long violationId, String letterType) {
        OvertimeViolationEntity violation = violationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));

        ProviderEntity provider = null;
        if (violation.getProviderId() != null) {
            provider = providerRepository.findById(violation.getProviderId()).orElse(null);
        }

        if ("COUNTY_DISPUTE".equalsIgnoreCase(letterType)) {
            return generateSOC2261(violation, provider);
        } else if ("SUPERVISOR_REVIEW".equalsIgnoreCase(letterType)) {
            return generateSOC2262(violation, provider);
        } else if ("CDSS_REVIEW".equalsIgnoreCase(letterType)) {
            return generateSOC2263(violation, provider);
        } else {
            // VIOLATION_NOTICE — select by violation number
            return generateViolationNotice(violation, provider);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Violation Notice (SOC 2257–2260) — selected by violation number
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] generateViolationNotice(OvertimeViolationEntity v, ProviderEntity provider) {
        int num = v.getViolationNumber() != null ? v.getViolationNumber() : 1;
        String socNumber = switch (num) {
            case 2 -> "SOC 2258";
            case 3 -> "SOC 2259";
            case 4 -> "SOC 2260";
            default -> "SOC 2257";
        };
        String title = switch (num) {
            case 2 -> "OVERTIME VIOLATION NOTICE — SECOND VIOLATION (TRAINING REQUIRED)";
            case 3 -> "OVERTIME VIOLATION NOTICE — THIRD VIOLATION (90-DAY SUSPENSION)";
            case 4 -> "OVERTIME VIOLATION NOTICE — FOURTH VIOLATION (PERMANENT TERMINATION)";
            default -> "OVERTIME VIOLATION NOTICE — FIRST VIOLATION";
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 54, 54, 72, 54);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, socNumber, title);
            addProviderAddress(doc, provider);
            addViolationDetails(doc, v, socNumber);
            addViolationBody(doc, v, num);
            addHearingRights(doc, v);
            addFooter(doc, socNumber);

            doc.close();
            log.info("[OT-LETTER] Generated {} for violation={}", socNumber, v.getId());
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("[OT-LETTER] PDF generation failed for violation {}: {}", v.getId(), ex.getMessage());
            throw new RuntimeException("Failed to generate violation letter: " + ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SOC 2261 — County Dispute Decision
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] generateSOC2261(OvertimeViolationEntity v, ProviderEntity provider) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 54, 54, 72, 54);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, "SOC 2261", "OVERTIME VIOLATION — COUNTY DISPUTE DECISION");
            addProviderAddress(doc, provider);
            addViolationDetails(doc, v, "SOC 2261");

            doc.add(Chunk.NEWLINE);
            Paragraph p = new Paragraph();
            p.add(new Chunk("Dispute Outcome: ", FONT_BOLD));
            p.add(new Chunk(v.getCountyDisputeOutcome() != null ? v.getCountyDisputeOutcome() : "PENDING", FONT_BODY));
            doc.add(p);

            if (v.getCountyDisputeResolutionDate() != null) {
                p = new Paragraph();
                p.add(new Chunk("Resolution Date: ", FONT_BOLD));
                p.add(new Chunk(v.getCountyDisputeResolutionDate().format(FMT), FONT_BODY));
                doc.add(p);
            }

            if (v.getCountyDisputeComments() != null) {
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph("Decision Comments:", FONT_BOLD));
                doc.add(new Paragraph(v.getCountyDisputeComments(), FONT_BODY));
            }

            addHearingRights(doc, v);
            addFooter(doc, "SOC 2261");

            doc.close();
            log.info("[OT-LETTER] Generated SOC 2261 for violation={}", v.getId());
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate SOC 2261: " + ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SOC 2262 — Supervisor Review Decision
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] generateSOC2262(OvertimeViolationEntity v, ProviderEntity provider) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 54, 54, 72, 54);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, "SOC 2262", "OVERTIME VIOLATION — SUPERVISOR REVIEW DECISION");
            addProviderAddress(doc, provider);
            addViolationDetails(doc, v, "SOC 2262");

            doc.add(Chunk.NEWLINE);
            Paragraph p = new Paragraph();
            p.add(new Chunk("Supervisor Review Outcome: ", FONT_BOLD));
            p.add(new Chunk(v.getSupervisorReviewOutcome() != null ? v.getSupervisorReviewOutcome() : "PENDING", FONT_BODY));
            doc.add(p);

            if (v.getSupervisorReviewDate() != null) {
                p = new Paragraph();
                p.add(new Chunk("Review Date: ", FONT_BOLD));
                p.add(new Chunk(v.getSupervisorReviewDate().format(FMT), FONT_BODY));
                doc.add(p);
            }

            if (v.getSupervisorReviewComments() != null) {
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph("Supervisor Comments:", FONT_BOLD));
                doc.add(new Paragraph(v.getSupervisorReviewComments(), FONT_BODY));
            }

            addHearingRights(doc, v);
            addFooter(doc, "SOC 2262");

            doc.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate SOC 2262: " + ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SOC 2263 — CDSS Review Decision
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] generateSOC2263(OvertimeViolationEntity v, ProviderEntity provider) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 54, 54, 72, 54);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, "SOC 2263", "OVERTIME VIOLATION — CDSS REVIEW DECISION");
            addProviderAddress(doc, provider);
            addViolationDetails(doc, v, "SOC 2263");

            doc.add(Chunk.NEWLINE);
            Paragraph p = new Paragraph();
            p.add(new Chunk("CDSS Review Outcome: ", FONT_BOLD));
            p.add(new Chunk(v.getCdssReviewOutcome() != null ? v.getCdssReviewOutcome() : "PENDING", FONT_BODY));
            doc.add(p);

            if (v.getCdssReviewDate() != null) {
                p = new Paragraph();
                p.add(new Chunk("Review Date: ", FONT_BOLD));
                p.add(new Chunk(v.getCdssReviewDate().format(FMT), FONT_BODY));
                doc.add(p);
            }

            if (v.getCdssReviewComments() != null) {
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph("CDSS Comments:", FONT_BOLD));
                doc.add(new Paragraph(v.getCdssReviewComments(), FONT_BODY));
            }

            addHearingRights(doc, v);
            addFooter(doc, "SOC 2263");

            doc.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate SOC 2263: " + ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Shared layout helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void addHeader(Document doc, String formNumber, String title) throws DocumentException {
        // Blue header bar
        PdfPTable header = new PdfPTable(new float[]{1f, 3f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(12);

        PdfPCell formCell = new PdfPCell(new Phrase(formNumber, FONT_HEADER));
        formCell.setBackgroundColor(HEADER_BLUE);
        formCell.setPadding(10);
        formCell.setBorder(Rectangle.NO_BORDER);
        header.addCell(formCell);

        PdfPCell agencyCell = new PdfPCell(new Phrase("STATE OF CALIFORNIA — HEALTH AND HUMAN SERVICES\nDepartment of Social Services", FONT_HEADER));
        agencyCell.setBackgroundColor(HEADER_BLUE);
        agencyCell.setPadding(10);
        agencyCell.setBorder(Rectangle.NO_BORDER);
        header.addCell(agencyCell);

        doc.add(header);

        // Colored rule
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(10);
        PdfPCell ruleCell = new PdfPCell();
        ruleCell.setBackgroundColor(RULE_BLUE);
        ruleCell.setFixedHeight(3f);
        ruleCell.setBorder(Rectangle.NO_BORDER);
        rule.addCell(ruleCell);
        doc.add(rule);

        doc.add(new Paragraph(title, FONT_TITLE));
        doc.add(Chunk.NEWLINE);
    }

    private void addProviderAddress(Document doc, ProviderEntity provider) throws DocumentException {
        PdfPTable addressTable = new PdfPTable(new float[]{1f, 1f});
        addressTable.setWidthPercentage(100);
        addressTable.setSpacingAfter(10);

        // Left: CDSS address
        PdfPCell cdssCell = new PdfPCell();
        cdssCell.setBorder(Rectangle.NO_BORDER);
        cdssCell.addElement(new Paragraph("California Department of Social Services", FONT_BODY));
        cdssCell.addElement(new Paragraph("744 P Street", FONT_BODY));
        cdssCell.addElement(new Paragraph("Sacramento, CA 95814", FONT_BODY));
        addressTable.addCell(cdssCell);

        // Right: Provider address
        PdfPCell provCell = new PdfPCell();
        provCell.setBorder(Rectangle.NO_BORDER);
        if (provider != null) {
            String name = buildProviderName(provider);
            provCell.addElement(new Paragraph(name, FONT_BODY));
            if (provider.getStreetAddress() != null) provCell.addElement(new Paragraph(provider.getStreetAddress(), FONT_BODY));
            String cityState = buildCityState(provider);
            if (!cityState.isBlank()) provCell.addElement(new Paragraph(cityState, FONT_BODY));
        } else {
            provCell.addElement(new Paragraph("[Provider Address]", FONT_BODY));
        }
        addressTable.addCell(provCell);
        doc.add(addressTable);
    }

    private void addViolationDetails(Document doc, OvertimeViolationEntity v, String socNum) throws DocumentException {
        PdfPTable details = new PdfPTable(new float[]{1f, 1f, 1f, 1f});
        details.setWidthPercentage(100);
        details.setSpacingBefore(6);
        details.setSpacingAfter(10);
        details.getDefaultCell().setBackgroundColor(LIGHT_GRAY);
        details.getDefaultCell().setPadding(5);
        details.getDefaultCell().setBorderColor(new Color(200, 200, 200));

        addDetailCell(details, "Form Number", socNum);
        addDetailCell(details, "Violation #", v.getViolationNumber() != null ? String.valueOf(v.getViolationNumber()) : "1");
        addDetailCell(details, "Violation Date", v.getViolationDate() != null ? v.getViolationDate().format(FMT) : "N/A");
        addDetailCell(details, "Date Issued", LocalDate.now().format(FMT));
        addDetailCell(details, "Service Period", buildServicePeriod(v));
        addDetailCell(details, "Hours Claimed", v.getHoursClaimed() != null ? String.format("%.1f", v.getHoursClaimed()) : "N/A");
        addDetailCell(details, "Maximum Allowed", v.getMaximumAllowed() != null ? String.format("%.1f", v.getMaximumAllowed()) : "N/A");
        addDetailCell(details, "Hours Exceeded", v.getHoursExceeded() != null ? String.format("%.1f", v.getHoursExceeded()) : "N/A");

        doc.add(details);
    }

    private void addDetailCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(5);
        cell.setBorderColor(new Color(200, 200, 200));
        cell.addElement(new Phrase(label, FONT_SMALL));
        cell.addElement(new Phrase(value, FONT_BOLD));
        table.addCell(cell);
    }

    private void addViolationBody(Document doc, OvertimeViolationEntity v, int num) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        switch (num) {
            case 1 -> {
                doc.add(new Paragraph(
                    "This notice is to inform you that our records indicate you have worked hours that exceed " +
                    "the overtime limit established under the In-Home Supportive Services (IHSS) program for the " +
                    "service period shown above. Under the Fair Labor Standards Act (FLSA) and California welfare " +
                    "law, IHSS providers are subject to overtime limits.", FONT_BODY));
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(
                    "This is your FIRST violation notice. No action against your provider status is taken at this time. " +
                    "However, you are strongly encouraged to review your hours across all recipients to ensure compliance " +
                    "with overtime rules going forward.", FONT_BODY));
            }
            case 2 -> {
                doc.add(new Paragraph(
                    "This is your SECOND overtime violation notice. Under California Department of Social Services " +
                    "policy, a second overtime violation requires mandatory training.", FONT_WARNING));
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(
                    "You are required to complete an IHSS overtime training course within 45 days of the date of this " +
                    "notice. Failure to complete the required training may result in suspension of your provider status.", FONT_BODY));
                if (v.getTrainingDueDate() != null) {
                    doc.add(Chunk.NEWLINE);
                    Paragraph p = new Paragraph();
                    p.add(new Chunk("Training Completion Deadline: ", FONT_BOLD));
                    p.add(new Chunk(v.getTrainingDueDate().format(FMT), FONT_WARNING));
                    doc.add(p);
                }
            }
            case 3 -> {
                doc.add(new Paragraph(
                    "This is your THIRD overtime violation notice. Pursuant to California Welfare and Institutions Code " +
                    "Section 12301.24 and CDSS policy, a third overtime violation results in a 90-day suspension " +
                    "of your IHSS provider registration.", FONT_WARNING));
                doc.add(Chunk.NEWLINE);
                if (v.getTerminationEffectiveDate() != null) {
                    Paragraph p = new Paragraph();
                    p.add(new Chunk("Suspension Effective Date: ", FONT_BOLD));
                    p.add(new Chunk(v.getTerminationEffectiveDate().format(FMT), FONT_WARNING));
                    doc.add(p);
                    LocalDate reinstatement = v.getTerminationEffectiveDate().plusDays(90);
                    p = new Paragraph();
                    p.add(new Chunk("Reinstatement Eligible Date: ", FONT_BOLD));
                    p.add(new Chunk(reinstatement.format(FMT), FONT_BODY));
                    doc.add(p);
                }
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(
                    "During the suspension period you may not work as an IHSS provider for any recipient. " +
                    "You may request reinstatement after the 90-day suspension period has elapsed.", FONT_BODY));
            }
            default -> {
                doc.add(new Paragraph(
                    "This is your FOURTH (or subsequent) overtime violation notice. Pursuant to California Welfare " +
                    "and Institutions Code Section 12301.24 and CDSS policy, a fourth overtime violation results " +
                    "in PERMANENT TERMINATION of your IHSS provider registration.", FONT_WARNING));
                doc.add(Chunk.NEWLINE);
                if (v.getTerminationEffectiveDate() != null) {
                    Paragraph p = new Paragraph();
                    p.add(new Chunk("Termination Effective Date: ", FONT_BOLD));
                    p.add(new Chunk(v.getTerminationEffectiveDate().format(FMT), FONT_WARNING));
                    doc.add(p);
                }
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(
                    "You may no longer work as an IHSS provider for any recipient in the state of California. " +
                    "You have the right to contest this action through the State Hearing process.", FONT_BODY));
            }
        }
    }

    private void addHearingRights(Document doc, OvertimeViolationEntity v) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);
        box.setSpacingBefore(6);
        box.setSpacingAfter(6);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(255, 243, 205));
        cell.setBorderColor(new Color(204, 153, 0));
        cell.setPadding(10);

        cell.addElement(new Paragraph("YOUR RIGHT TO A STATE HEARING", FONT_BOLD));
        cell.addElement(Chunk.NEWLINE);
        cell.addElement(new Paragraph(
            "If you disagree with this action, you have the right to request a State Hearing. You must request " +
            "a hearing within 90 days from the date of this notice. To request a hearing, contact your local " +
            "county IHSS office or call the California Department of Social Services at 1-800-952-5253.", FONT_BODY));
        cell.addElement(Chunk.NEWLINE);
        cell.addElement(new Paragraph(
            "You also have the right to file a county dispute with your local IHSS office within 30 days of " +
            "this notice. The county must respond to your dispute within 45 days.", FONT_BODY));

        box.addCell(cell);
        doc.add(box);
    }

    private void addFooter(Document doc, String formNumber) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            formNumber + " (Rev. 01/2026)  |  State of California — Health and Human Services Agency — Department of Social Services",
            FONT_SMALL);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Utility helpers
    // ──────────────────────────────────────────────────────────────────────────

    private String buildProviderName(ProviderEntity p) {
        StringBuilder sb = new StringBuilder();
        if (p.getFirstName() != null) sb.append(p.getFirstName()).append(" ");
        if (p.getLastName() != null) sb.append(p.getLastName());
        return sb.toString().trim();
    }

    private String buildCityState(ProviderEntity p) {
        StringBuilder sb = new StringBuilder();
        if (p.getCity() != null) sb.append(p.getCity()).append(", ");
        if (p.getState() != null) sb.append(p.getState()).append(" ");
        if (p.getZipCode() != null) sb.append(p.getZipCode());
        return sb.toString().trim();
    }

    private String buildServicePeriod(OvertimeViolationEntity v) {
        if (v.getServiceMonth() != null && v.getServiceYear() != null) {
            return String.format("%02d/%d", v.getServiceMonth(), v.getServiceYear());
        }
        return "N/A";
    }
}
