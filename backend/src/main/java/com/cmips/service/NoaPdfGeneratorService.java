package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.ElectronicFormEntity.BviFormat;
import com.cmips.entity.NoticeOfActionEntity.Language;
import com.cmips.entity.NoticeOfActionEntity.NoaType;
import com.cmips.repository.*;
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
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * NOA PDF Generator — DSD Section 31
 *
 * Renders a downloadable/printable PDF for any of the 8 NOA types (NA 1250–1257).
 * Supports Standard, Large-Font (BVI), and Audio/Data CD text variants.
 *
 * Layout per California CDSS NA form specifications:
 *   1. Header bar  — "STATE OF CALIFORNIA — HEALTH AND HUMAN SERVICES AGENCY"
 *   2. Form ID     — "NA 1250" (top-right)
 *   3. County block— County IHSS office name + address
 *   4. Date block  — Issue date
 *   5. Recipient   — Recipient name + mailing address
 *   6. Case info   — Case number | Social worker | Phone
 *   7. Title       — "NOTICE OF ACTION" + sub-title per NOA type
 *   8. Body        — Assembled messageContent from NoaContentAssemblerService
 *   9. Service table (NA_1250 only) — Service | Authorized Hours/Month
 *  10. Hearing rights — bold header + paragraph
 *  11. Footer      — County office address + signature area
 */
@Service
public class NoaPdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(NoaPdfGeneratorService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // ── California state brand colours ───────────────────────────────────────
    private static final Color HEADER_BLUE  = new Color(0, 56, 101);   // CDSS dark blue
    private static final Color RULE_BLUE    = new Color(0, 102, 179);   // rule line
    private static final Color LIGHT_GRAY   = new Color(245, 245, 245); // service table bg
    private static final Color DARK_GRAY    = new Color(80, 80, 80);

    private final NoticeOfActionRepository noaRepository;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final ServiceEligibilityRepository eligibilityRepository;

    public NoaPdfGeneratorService(NoticeOfActionRepository noaRepository,
                                   CaseRepository caseRepository,
                                   RecipientRepository recipientRepository,
                                   ServiceEligibilityRepository eligibilityRepository) {
        this.noaRepository = noaRepository;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.eligibilityRepository = eligibilityRepository;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generate a PDF for the given NOA.
     *
     * @param noaId     ID of the NoticeOfActionEntity
     * @param bviFormat BVI format override (null → STANDARD)
     * @return PDF bytes
     */
    public byte[] generateNoaPdf(Long noaId, BviFormat bviFormat) {
        NoticeOfActionEntity noa = noaRepository.findById(noaId)
                .orElseThrow(() -> new RuntimeException("NOA not found: " + noaId));

        if (bviFormat == null) bviFormat = BviFormat.STANDARD;

        // Audio CD / Data CD → return plain-text bytes
        if (bviFormat == BviFormat.AUDIO_CD || bviFormat == BviFormat.DATA_CD) {
            return generateTextVariant(noa, bviFormat);
        }

        CaseEntity caseEntity = caseRepository.findById(noa.getCaseId()).orElse(null);
        RecipientEntity recipient = (noa.getRecipientId() != null)
                ? recipientRepository.findById(noa.getRecipientId()).orElse(null) : null;

        boolean largePrint = bviFormat == BviFormat.LARGE_FONT;

        return buildPdf(noa, caseEntity, recipient, largePrint);
    }

    /** Convenience — standard print. */
    public byte[] generateNoaPdf(Long noaId) {
        return generateNoaPdf(noaId, BviFormat.STANDARD);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PDF Construction
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] buildPdf(NoticeOfActionEntity noa,
                             CaseEntity caseEntity,
                             RecipientEntity recipient,
                             boolean largePrint) {
        try {
            Document doc = new Document(PageSize.LETTER, 54, 54, 54, 54);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(doc, out);

            doc.addTitle("Notice of Action — " + noaType(noa));
            doc.addAuthor("CMIPS / California DSS");
            doc.addCreationDate();

            doc.open();

            float baseSize = largePrint ? 14f : 10f;
            float titleSize = largePrint ? 18f : 14f;
            float headSize  = largePrint ? 16f : 12f;

            Font headerFont  = new Font(Font.HELVETICA, largePrint ? 10f : 8f, Font.NORMAL, Color.WHITE);
            Font titleFont   = new Font(Font.HELVETICA, titleSize, Font.BOLD, HEADER_BLUE);
            Font subFont     = new Font(Font.HELVETICA, headSize,  Font.BOLD, HEADER_BLUE);
            Font bodyFont    = new Font(Font.HELVETICA, baseSize,  Font.NORMAL, Color.BLACK);
            Font boldBody    = new Font(Font.HELVETICA, baseSize,  Font.BOLD,   Color.BLACK);
            Font smallFont   = new Font(Font.HELVETICA, largePrint ? 12f : 9f, Font.NORMAL, DARK_GRAY);
            Font tableHead   = new Font(Font.HELVETICA, baseSize,  Font.BOLD,   Color.WHITE);
            Font tableCell   = new Font(Font.HELVETICA, baseSize,  Font.NORMAL, Color.BLACK);

            // ── 1. Header bar ─────────────────────────────────────────────────
            PdfPTable headerTbl = new PdfPTable(2);
            headerTbl.setWidthPercentage(100);
            headerTbl.setWidths(new float[]{75, 25});
            headerTbl.setSpacingAfter(6);

            PdfPCell agencyCell = new PdfPCell();
            agencyCell.setBackgroundColor(HEADER_BLUE);
            agencyCell.setPadding(6);
            agencyCell.setBorder(Rectangle.NO_BORDER);
            Paragraph agency = new Paragraph();
            agency.add(new Chunk("STATE OF CALIFORNIA — HEALTH AND HUMAN SERVICES AGENCY\n",
                    new Font(Font.HELVETICA, largePrint ? 9f : 7f, Font.BOLD, Color.WHITE)));
            agency.add(new Chunk("DEPARTMENT OF SOCIAL SERVICES",
                    new Font(Font.HELVETICA, largePrint ? 8f : 6.5f, Font.NORMAL, Color.WHITE)));
            agencyCell.addElement(agency);
            headerTbl.addCell(agencyCell);

            PdfPCell formNumCell = new PdfPCell();
            formNumCell.setBackgroundColor(HEADER_BLUE);
            formNumCell.setPadding(6);
            formNumCell.setBorder(Rectangle.NO_BORDER);
            formNumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            formNumCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            String formId = noaFormNumber(noa.getNoaType(), false);
            formNumCell.addElement(new Paragraph(formId,
                    new Font(Font.HELVETICA, largePrint ? 14f : 11f, Font.BOLD, Color.WHITE)));
            headerTbl.addCell(formNumCell);

            doc.add(headerTbl);

            // ── 2. Address & Case Info row ─────────────────────────────────────
            PdfPTable addrTbl = new PdfPTable(2);
            addrTbl.setWidthPercentage(100);
            addrTbl.setWidths(new float[]{50, 50});
            addrTbl.setSpacingAfter(8);

            // County block (left)
            PdfPCell countyCell = new PdfPCell();
            countyCell.setBorder(Rectangle.BOX);
            countyCell.setPadding(6);
            countyCell.setBackgroundColor(LIGHT_GRAY);
            Paragraph countyPara = new Paragraph();
            String countyName = caseEntity != null && caseEntity.getCountyCode() != null
                    ? caseEntity.getCountyCode() + " County" : "County";
            countyPara.add(new Chunk("In-Home Supportive Services\n", boldBody));
            countyPara.add(new Chunk(countyName + " Department of Social Services\n", bodyFont));
            countyPara.add(new Chunk("Phone: (800) 952-5253\n", bodyFont));
            countyPara.add(new Chunk("TDD: (800) 952-8349", smallFont));
            countyCell.addElement(countyPara);
            addrTbl.addCell(countyCell);

            // Recipient block (right)
            PdfPCell recipCell = new PdfPCell();
            recipCell.setBorder(Rectangle.BOX);
            recipCell.setPadding(6);
            Paragraph recipPara = new Paragraph();
            if (recipient != null) {
                String fullName = trim(recipient.getFirstName()) + " " + trim(recipient.getLastName());
                recipPara.add(new Chunk(fullName.trim() + "\n", boldBody));
                String addr = buildAddress(recipient);
                if (!addr.isBlank()) recipPara.add(new Chunk(addr, bodyFont));
            } else {
                recipPara.add(new Chunk("Recipient\n", bodyFont));
            }
            recipCell.addElement(recipPara);
            addrTbl.addCell(recipCell);

            doc.add(addrTbl);

            // ── 3. Case metadata row ──────────────────────────────────────────
            PdfPTable metaTbl = new PdfPTable(3);
            metaTbl.setWidthPercentage(100);
            metaTbl.setSpacingAfter(10);
            String caseNum = caseEntity != null ? caseEntity.getCaseNumber() : "—";
            String worker  = caseEntity != null && caseEntity.getCaseOwnerId() != null
                    ? caseEntity.getCaseOwnerId() : "—";
            String issueDate = noa.getRequestDate() != null
                    ? noa.getRequestDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT);

            addMetaCell(metaTbl, "Case Number", caseNum, bodyFont, boldBody);
            addMetaCell(metaTbl, "Social Worker", worker, bodyFont, boldBody);
            addMetaCell(metaTbl, "Date", issueDate, bodyFont, boldBody);
            doc.add(metaTbl);

            // ── 4. Rule line ───────────────────────────────────────────────────
            LineSeparator rule = new LineSeparator(2f, 100f, RULE_BLUE, Element.ALIGN_LEFT, -2);
            doc.add(new Chunk(rule));
            doc.add(Chunk.NEWLINE);

            // ── 5. Title ───────────────────────────────────────────────────────
            Paragraph titlePara = new Paragraph("NOTICE OF ACTION", titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(4);
            doc.add(titlePara);

            String subtitle = noaSubtitle(noa.getNoaType());
            Paragraph subPara = new Paragraph(subtitle, subFont);
            subPara.setAlignment(Element.ALIGN_CENTER);
            subPara.setSpacingAfter(10);
            doc.add(subPara);

            doc.add(new Chunk(rule));
            doc.add(Chunk.NEWLINE);

            // ── 6. Effective date notice ───────────────────────────────────────
            if (noa.getEffectiveDate() != null) {
                Paragraph effPara = new Paragraph(
                        "Effective Date: " + noa.getEffectiveDate().format(DATE_FMT), boldBody);
                effPara.setSpacingAfter(8);
                doc.add(effPara);
            }

            // ── 7. Service table (NA_1250 approval only) ───────────────────────
            if (noa.getNoaType() == NoaType.NA_1250 && caseEntity != null) {
                addServiceTable(doc, caseEntity, tableHead, tableCell, largePrint);
            }

            // ── 8. Body text (assembled messageContent) ────────────────────────
            String body = noa.getMessageContent();
            if (body != null && !body.isBlank()) {
                // Split into paragraphs on double newline
                for (String section : body.split("\n\n")) {
                    section = section.trim();
                    if (section.isBlank()) continue;

                    // Hearing rights section — bold header
                    if (section.startsWith("You have the right to request a State Hearing")) {
                        doc.add(Chunk.NEWLINE);
                        Paragraph hearingHead = new Paragraph("YOUR RIGHT TO A STATE HEARING", boldBody);
                        hearingHead.setSpacingBefore(6);
                        doc.add(hearingHead);
                        Paragraph hearingBody = new Paragraph(section, bodyFont);
                        hearingBody.setSpacingAfter(8);
                        doc.add(hearingBody);
                    } else {
                        Paragraph p = new Paragraph(section, bodyFont);
                        p.setSpacingAfter(8);
                        doc.add(p);
                    }
                }
            } else {
                doc.add(new Paragraph("See attached details.", bodyFont));
            }

            // ── 9. Aid-paid-pending notice (applicable when AID PAID PENDING) ──
            if (noa.getNoaType() == NoaType.NA_1255 || noa.getNoaType() == NoaType.NA_1252) {
                doc.add(Chunk.NEWLINE);
                Paragraph aidPara = new Paragraph(
                        "NOTE: If you request a State Hearing before the effective date of this action, " +
                        "your current level of services will continue until the hearing decision is issued " +
                        "(aid paid pending).", bodyFont);
                aidPara.setSpacingAfter(10);
                doc.add(aidPara);
            }

            // ── 10. Footer ─────────────────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            doc.add(new Chunk(new LineSeparator(1f, 100f, DARK_GRAY, Element.ALIGN_LEFT, -2)));
            doc.add(Chunk.NEWLINE);

            Paragraph footer = new Paragraph("", smallFont);
            footer.add(new Chunk("This notice was generated by CMIPS on " +
                    LocalDate.now().format(DATE_FMT) + ".  ", smallFont));
            footer.add(new Chunk("Form " + formId + (largePrint ? "L" : "") +
                    " (Rev. 01/2026)  |  " +
                    "California Department of Social Services", smallFont));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            log.info("[NOA-PDF] Generated {} PDF for NOA {} (bvi={})",
                    noa.getNoaType(), noa.getId(), largePrint ? "LARGE_FONT" : "STANDARD");
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[NOA-PDF] Failed to generate PDF for NOA {}: {}", noa.getId(), e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Service Table (NA_1250 — lists each authorized service + hours/month)
    // ──────────────────────────────────────────────────────────────────────────

    private void addServiceTable(Document doc, CaseEntity caseEntity,
                                  Font tableHead, Font tableCell, boolean large) throws DocumentException {
        // Load latest active assessment for this case
        Optional<ServiceEligibilityEntity> assessOpt = eligibilityRepository
                .findActiveEligibilityByCaseId(caseEntity.getId());

        Paragraph tableTitle = new Paragraph("Authorized Services", tableHead);
        tableTitle.setFont(new Font(Font.HELVETICA, large ? 13f : 11f, Font.BOLD, HEADER_BLUE));
        tableTitle.setSpacingBefore(6);
        tableTitle.setSpacingAfter(4);
        doc.add(tableTitle);

        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(70);
        tbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        tbl.setWidths(new float[]{70, 30});
        tbl.setSpacingAfter(10);

        // Header row
        addTableHeaderCell(tbl, "Service", tableHead, HEADER_BLUE);
        addTableHeaderCell(tbl, "Hours/Month", tableHead, HEADER_BLUE);

        // Service rows
        ServiceEligibilityEntity a = assessOpt.orElse(null);
        List<String[]> rows = buildServiceRows(a, caseEntity);
        boolean shade = false;
        for (String[] row : rows) {
            Color bg = shade ? LIGHT_GRAY : Color.WHITE;
            addTableDataCell(tbl, row[0], tableCell, bg);
            addTableDataCell(tbl, row[1], tableCell, bg);
            shade = !shade;
        }

        // Total row
        String totalHours = caseEntity.getAuthorizedHoursMonthly() != null
                ? formatHours(caseEntity.getAuthorizedHoursMonthly()) : "—";
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AUTHORIZED HOURS/MONTH",
                new Font(Font.HELVETICA, tableCell.getSize(), Font.BOLD, Color.BLACK)));
        totalLabelCell.setPadding(4);
        totalLabelCell.setBackgroundColor(new Color(230, 240, 255));
        tbl.addCell(totalLabelCell);

        PdfPCell totalHoursCell = new PdfPCell(new Phrase(totalHours,
                new Font(Font.HELVETICA, tableCell.getSize(), Font.BOLD, Color.BLACK)));
        totalHoursCell.setPadding(4);
        totalHoursCell.setBackgroundColor(new Color(230, 240, 255));
        totalHoursCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tbl.addCell(totalHoursCell);

        doc.add(tbl);
    }

    private List<String[]> buildServiceRows(ServiceEligibilityEntity a, CaseEntity c) {
        List<String[]> rows = new ArrayList<>();
        addRow(rows, "Domestic Services",          a != null ? a.getDomesticServicesHours() : null);
        addRow(rows, "Related Services",            a != null ? a.getRelatedServicesHours() : null);
        addRow(rows, "Personal Care",               a != null ? a.getPersonalCareHours() : null);
        addRow(rows, "Meal Preparation",            a != null ? a.getMealPreparationHours() : null);
        addRow(rows, "Meal Cleanup",                a != null ? a.getMealCleanupHours() : null);
        addRow(rows, "Laundry",                     a != null ? a.getLaundryHours() : null);
        addRow(rows, "Shopping / Errands",          a != null ? a.getShoppingErrandsHours() : null);
        addRow(rows, "Ambulation",                  a != null ? a.getAmbulationHours() : null);
        addRow(rows, "Bathing / Oral Hygiene",      a != null ? a.getBathingOralHygieneHours() : null);
        addRow(rows, "Grooming",                    a != null ? a.getGroomingHours() : null);
        addRow(rows, "Dressing",                    a != null ? a.getDressingHours() : null);
        addRow(rows, "Bowel / Bladder Care",        a != null ? a.getBowelBladderCareHours() : null);
        addRow(rows, "Transfer / Repositioning",    a != null ? a.getTransferRepositioningHours() : null);
        addRow(rows, "Feeding",                     a != null ? a.getFeedingHours() : null);
        addRow(rows, "Respiration",                 a != null ? a.getRespirationHours() : null);
        addRow(rows, "Skin Care",                   a != null ? a.getSkinCareHours() : null);
        addRow(rows, "Paramedical",                 a != null ? a.getParamedicalHours() : null);
        addRow(rows, "Protective Supervision",      a != null ? a.getProtectiveSupervisionHours() : null);
        addRow(rows, "Accompaniment (Medical)",     a != null ? a.getAccompanimentMedicalHours() : null);
        addRow(rows, "Accompaniment (Alt. Res.)",   a != null ? a.getAccompanimentAltResourcesHours() : null);
        if (rows.isEmpty()) {
            rows.add(new String[]{"Services as authorized",
                    c.getAuthorizedHoursMonthly() != null ? formatHours(c.getAuthorizedHoursMonthly()) : "—"});
        }
        return rows;
    }

    private void addRow(List<String[]> rows, String label, Double hours) {
        if (hours != null && hours > 0) {
            rows.add(new String[]{label, formatHours(hours)});
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Audio CD / Data CD plain-text variant (BVI)
    // Digits are space-separated per CDSS braille/audio formatting rules
    // ──────────────────────────────────────────────────────────────────────────

    private byte[] generateTextVariant(NoticeOfActionEntity noa, BviFormat fmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("NOTICE OF ACTION\n");
        sb.append(noaFormNumber(noa.getNoaType(), false)).append("\n");
        sb.append("California Department of Social Services\n\n");
        sb.append("Date: ").append(spaceDigits(
                noa.getRequestDate() != null ? noa.getRequestDate().format(DATE_FMT) : "")).append("\n");
        if (noa.getEffectiveDate() != null) {
            sb.append("Effective: ").append(spaceDigits(noa.getEffectiveDate().format(DATE_FMT))).append("\n");
        }
        sb.append("\n");
        sb.append(noaSubtitle(noa.getNoaType())).append("\n\n");

        String body = noa.getMessageContent();
        if (body != null && !body.isBlank()) {
            // Audio CD: space out all digit sequences for TTS readability
            if (fmt == BviFormat.AUDIO_CD) body = spaceDigitsInText(body);
            sb.append(body);
        }

        sb.append("\n\n--- End of Notice ---\n");
        sb.append("Form ").append(noaFormNumber(noa.getNoaType(), false))
          .append(" | CMIPS | ").append(LocalDate.now().format(DATE_FMT));
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Space out digit sequences: "03/06/2026" → "0 3 / 0 6 / 2 0 2 6" */
    private String spaceDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("(\\d)", "$1 ").trim();
    }

    private String spaceDigitsInText(String text) {
        if (text == null) return "";
        // Space every digit for TTS readability; collapse double-spaces
        return text.replaceAll("(\\d)", "$1 ").replaceAll(" {2,}", " ");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Table cell helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void addTableHeaderCell(PdfPTable tbl, String text, Font f, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(f.getFamily(), f.getSize(), Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        tbl.addCell(cell);
    }

    private void addTableDataCell(PdfPTable tbl, String text, Font f, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        tbl.addCell(cell);
    }

    private void addMetaCell(PdfPTable tbl, String label, String value, Font body, Font bold) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setPadding(5);
        c.setBackgroundColor(LIGHT_GRAY);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", new Font(Font.HELVETICA, body.getSize() - 1f, Font.NORMAL, DARK_GRAY)));
        p.add(new Chunk(value, bold));
        c.addElement(p);
        tbl.addCell(c);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Utility helpers
    // ──────────────────────────────────────────────────────────────────────────

    private String noaType(NoticeOfActionEntity noa) {
        return noa.getNoaType() != null ? noa.getNoaType().name() : "NOA";
    }

    private String noaFormNumber(NoaType type, boolean large) {
        if (type == null) return "NA 1250";
        String base = switch (type) {
            case NA_1250 -> "NA 1250";
            case NA_1251 -> "NA 1251";
            case NA_1252 -> "NA 1252";
            case NA_1253 -> "NA 1253";
            case NA_1254 -> "NA 1254";
            case NA_1255 -> "NA 1255";
            case NA_1256 -> "NA 1256";
            case NA_1257 -> "NA 1257";
        };
        return large ? base + "L" : base;
    }

    private String noaSubtitle(NoaType type) {
        if (type == null) return "";
        return switch (type) {
            case NA_1250 -> "Your application for In-Home Supportive Services (IHSS) has been APPROVED";
            case NA_1251 -> "Your In-Home Supportive Services are being CONTINUED";
            case NA_1252 -> "Your application for In-Home Supportive Services (IHSS) has been DENIED";
            case NA_1253 -> "Your In-Home Supportive Services authorization has CHANGED";
            case NA_1254 -> "Your In-Home Supportive Services have been REDUCED";
            case NA_1255 -> "Your In-Home Supportive Services have been TERMINATED";
            case NA_1256 -> "Your Medi-Cal Share of Cost has changed";
            case NA_1257 -> "Notice Regarding Multiple In-Home Supportive Services Programs";
        };
    }

    private String buildAddress(RecipientEntity r) {
        StringBuilder sb = new StringBuilder();
        // Prefer mailing address; fall back to residence
        String street = trim(r.getMailingStreetNumber()) + " " + trim(r.getMailingStreetName());
        if (street.isBlank()) {
            street = trim(r.getResidenceStreetNumber()) + " " + trim(r.getResidenceStreetName());
        }
        if (!street.isBlank()) sb.append(street.trim()).append("\n");

        String city = trim(r.getResidenceCity());
        String zip  = trim(r.getResidenceZip());
        if (!city.isBlank() || !zip.isBlank()) {
            sb.append(city);
            if (!city.isBlank() && !zip.isBlank()) sb.append(", CA ").append(zip);
            else if (!zip.isBlank()) sb.append("CA ").append(zip);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatHours(Double h) {
        if (h == null) return "0:00";
        int hrs  = h.intValue();
        int mins = (int) Math.round((h - hrs) * 60);
        return String.format("%d:%02d", hrs, mins);
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }
}
