package com.cmips.service;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.NoaCategoryMessageEntity;
import com.cmips.entity.NoticeOfActionEntity;
import com.cmips.entity.NoticeOfActionEntity.Language;
import com.cmips.entity.NoticeOfActionEntity.NoaType;
import com.cmips.entity.RecipientEntity;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.NoaCategoryMessageRepository;
import com.cmips.repository.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * NOA Content Assembler — DSD Section 31 / Appendix G
 *
 * Assembles the message body for a Notice of Action by:
 *   1. Mapping NoaType + triggerReasonCode → list of Appendix G category codes
 *   2. Fetching the approved translated text for each code
 *   3. Substituting dynamic variables: {DATE}, {HOURS}, {AMOUNT}, {SERVICES}, {COUNTY}, etc.
 *   4. Persisting the result in NoticeOfActionEntity.messageContent
 */
@Service
public class NoaContentAssemblerService {

    private static final Logger log = LoggerFactory.getLogger(NoaContentAssemblerService.class);
    private static final DateTimeFormatter NOA_DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final NoaCategoryMessageRepository categoryRepo;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;

    public NoaContentAssemblerService(NoaCategoryMessageRepository categoryRepo,
                                       CaseRepository caseRepository,
                                       RecipientRepository recipientRepository) {
        this.categoryRepo = categoryRepo;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Assembles and stores the message body on the NOA entity.
     * Call this after creating/saving the NOA to populate messageContent.
     *
     * @param noa         the NOA entity (already saved)
     * @param variables   optional map of variable overrides, e.g. {"HOURS":"200:00","AMOUNT":"125.00"}
     */
    public void assemble(NoticeOfActionEntity noa, Map<String, String> variables) {
        try {
            List<String> codes = resolveCategoryCodes(noa.getNoaType(), noa.getTriggerReasonCode());
            if (codes.isEmpty()) {
                log.debug("[NOA-ASSEMBLE] No category codes for type={} reason={}", noa.getNoaType(), noa.getTriggerReasonCode());
                return;
            }

            // Build variable map from case/recipient context + caller overrides
            Map<String, String> vars = buildContextVariables(noa);
            if (variables != null) vars.putAll(variables);

            Language lang = noa.getLanguage() != null ? noa.getLanguage() : Language.ENGLISH;

            StringBuilder body = new StringBuilder();
            StringJoiner codeList = new StringJoiner(",");

            for (String code : codes) {
                categoryRepo.findByCategoryCode(code).ifPresent(msg -> {
                    String text = selectText(msg, lang);
                    if (text != null && !text.isBlank()) {
                        body.append(substitute(text, vars)).append("\n\n");
                        codeList.add(msg.getCategoryCode());
                    }
                });
            }

            // Always append State Hearing rights (IN01 + SH01) per DSD Section 31
            appendHearingRights(body, codeList, lang, vars);

            noa.setMessageContent(body.toString().strip());
            noa.setAssembledCategories(codeList.toString());

        } catch (Exception ex) {
            log.error("[NOA-ASSEMBLE] Failed to assemble content for NOA {}: {}", noa.getId(), ex.getMessage());
        }
    }

    /** Convenience overload — no extra variables. */
    public void assemble(NoticeOfActionEntity noa) {
        assemble(noa, null);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Category Code Resolution
    //
    // Maps NoaType + optional triggerReasonCode → ordered list of Appendix G codes.
    // Rules are derived from DSD Section 31 and Appendix G.
    // ──────────────────────────────────────────────────────────────────────────

    private List<String> resolveCategoryCodes(NoaType noaType, String reasonCode) {
        if (noaType == null) return List.of();

        return switch (noaType) {

            // ── NA 1250: Approval ─────────────────────────────────────────────
            case NA_1250 -> {
                if ("PROVISIONAL".equalsIgnoreCase(reasonCode))  yield List.of("AA01", "AS01");
                if ("FINAL_PROVISIONAL".equalsIgnoreCase(reasonCode)) yield List.of("AA02", "AS01");
                if ("PREVIOUSLY_DENIED_ERROR".equalsIgnoreCase(reasonCode)) yield List.of("AA03", "AS01");
                yield List.of("AS01");   // standard approval — body comes from service table on the form
            }

            // ── NA 1251: Continuation ─────────────────────────────────────────
            case NA_1251 -> {
                if (reasonCode != null && reasonCode.startsWith("HR")) yield List.of(reasonCode, "VS01");
                yield List.of("VS01");
            }

            // ── NA 1252: Denial ───────────────────────────────────────────────
            case NA_1252 -> {
                String dnCode = mapDenialToDnCode(reasonCode);
                yield dnCode != null ? List.of(dnCode) : List.of("DN24");
            }

            // ── NA 1253: Change in Award (increase) ───────────────────────────
            case NA_1253 -> {
                if (reasonCode != null && reasonCode.startsWith("FS")) yield List.of(reasonCode, "HR03");
                yield List.of("HR03");
            }

            // ── NA 1254: Change – Reduction ───────────────────────────────────
            case NA_1254 -> {
                if (reasonCode != null && reasonCode.startsWith("LM")) yield List.of(reasonCode);
                if (reasonCode != null && reasonCode.startsWith("AR")) yield List.of(reasonCode);
                if (reasonCode != null && reasonCode.startsWith("FS")) yield List.of(reasonCode, "HR04");
                if (reasonCode != null && reasonCode.startsWith("FR")) yield List.of(reasonCode);
                yield List.of("HR04");
            }

            // ── NA 1255: Termination ──────────────────────────────────────────
            case NA_1255 -> {
                String trCode = mapTerminationToTrCode(reasonCode);
                yield trCode != null ? List.of(trCode) : List.of("TR01");
            }

            // ── NA 1256: Share of Cost ────────────────────────────────────────
            case NA_1256 -> {
                if ("NEW".equalsIgnoreCase(reasonCode))  yield List.of("SC01");
                if ("INCREASE".equalsIgnoreCase(reasonCode)) yield List.of("SC02");
                if ("DECREASE".equalsIgnoreCase(reasonCode)) yield List.of("SC03");
                if ("ELIMINATED".equalsIgnoreCase(reasonCode)) yield List.of("SC04");
                yield List.of("SC01");
            }

            // ── NA 1257: Multi-Program ────────────────────────────────────────
            case NA_1257 -> List.of("VS02", "AS01");
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Reason Code Mapping Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Maps CMIPS denial reason codes to Appendix G DN codes. */
    private String mapDenialToDnCode(String reasonCode) {
        if (reasonCode == null) return null;
        return switch (reasonCode) {
            case "D0001" -> "DN01";   // SSI/SSP Board & Care
            case "D0002" -> "DN02";   // Citizenship
            case "D0003" -> "DN03";   // Non-CA residency
            case "D0004" -> "DN04";   // Not in own home
            case "D0005" -> "DN05";   // Whereabouts unknown
            case "D0006" -> "DN06";   // Hospitalized
            case "D0007" -> "DN07";   // SNF
            case "D0008" -> "DN08";   // ICF
            case "D0009" -> "DN09";   // CCF
            case "D0010" -> "DN10";   // Death
            case "D0011" -> "DN11";   // Invalid SSN
            case "D0012" -> "DN12";   // Duplicate SSN
            case "D0013" -> "DN13";   // Medical cert not received
            case "D0014" -> "DN14";   // Medical cert insufficient
            case "D0015" -> "DN15";   // PACE enrolled
            case "D0016" -> "DN16";   // No functional need
            case "D0017" -> "DN17";   // Medi-Cal ineligible
            case "D0018" -> "DN18";   // Age
            case "D0019" -> "DN19";   // Income exceeds limit
            case "D0020" -> "DN20";   // No response to home visit
            case "D0021" -> "DN21";   // No authorized rep
            case "D0022" -> "DN22";   // Application withdrawn
            case "D0023" -> "DN23";   // Failure to provide info
            default -> "DN24";
        };
    }

    /** Maps CMIPS termination reason codes to Appendix G TR codes. */
    private String mapTerminationToTrCode(String reasonCode) {
        if (reasonCode == null) return null;
        return switch (reasonCode) {
            case "CC501" -> "TR01";   // Left California
            case "CC502" -> "TR02";   // No longer in own home
            case "CC503" -> "TR03";   // No longer Medi-Cal eligible
            case "CC504" -> "TR06";   // Medi-Cal non-compliance (CC514 also maps here)
            case "CC505" -> "TR05";   // No functional need
            case "CC506" -> "TR07";   // Recipient request
            case "CC507" -> "TR08";   // Whereabouts unknown
            case "CC508" -> "TR09";   // Income exceeds limit
            case "CC509" -> "TR04";   // Death
            case "CC510" -> "TR04";   // Death (alternate)
            case "CC511" -> "TR04";   // Recipient deceased
            case "CC514" -> "TR06";   // Medi-Cal non-compliance
            default -> "TR01";
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Variable Substitution
    // ──────────────────────────────────────────────────────────────────────────

    /** Build context variables from case and recipient data. */
    private Map<String, String> buildContextVariables(NoticeOfActionEntity noa) {
        Map<String, String> vars = new HashMap<>();
        vars.put("DATE", noa.getEffectiveDate() != null
                ? noa.getEffectiveDate().format(NOA_DATE_FMT)
                : LocalDate.now().format(NOA_DATE_FMT));

        if (noa.getCaseId() != null) {
            caseRepository.findById(noa.getCaseId()).ifPresent(c -> {
                if (c.getCountyCode() != null) vars.put("COUNTY", c.getCountyCode());
                if (c.getAuthorizedHoursMonthly() != null) {
                    vars.put("HOURS", formatHours(c.getAuthorizedHoursMonthly()));
                }
                if (c.getShareOfCostAmount() != null) {
                    vars.put("AMOUNT", String.format("$%.2f", c.getShareOfCostAmount()));
                }
            });
        }

        if (noa.getRecipientId() != null) {
            recipientRepository.findById(noa.getRecipientId()).ifPresent(r -> {
                String name = buildName(r);
                if (!name.isBlank()) vars.put("NAME", name);
            });
        }

        return vars;
    }

    private String substitute(String template, Map<String, String> vars) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /** Format decimal hours (e.g. 195.5) to HHH:MM string (e.g. "195:30"). */
    private String formatHours(Double hours) {
        if (hours == null) return "0:00";
        int h = hours.intValue();
        int m = (int) Math.round((hours - h) * 60);
        return String.format("%d:%02d", h, m);
    }

    private String buildName(RecipientEntity r) {
        StringBuilder sb = new StringBuilder();
        if (r.getFirstName() != null) sb.append(r.getFirstName()).append(" ");
        if (r.getLastName() != null)  sb.append(r.getLastName());
        return sb.toString().trim();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Language Selection
    // ──────────────────────────────────────────────────────────────────────────

    private String selectText(NoaCategoryMessageEntity msg, Language lang) {
        return switch (lang) {
            case SPANISH  -> msg.getTextSpanish()  != null ? msg.getTextSpanish()  : msg.getTextEnglish();
            case CHINESE  -> msg.getTextChinese()  != null ? msg.getTextChinese()  : msg.getTextEnglish();
            case ARMENIAN -> msg.getTextArmenian() != null ? msg.getTextArmenian() : msg.getTextEnglish();
            default       -> msg.getTextEnglish();
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    // State Hearing Rights Footer
    // DSD Section 31: Hearing rights paragraph must appear on every NOA.
    // ──────────────────────────────────────────────────────────────────────────

    private void appendHearingRights(StringBuilder body, StringJoiner codeList,
                                     Language lang, Map<String, String> vars) {
        categoryRepo.findByCategoryCode("SH01").ifPresent(sh01 -> {
            String text = selectText(sh01, lang);
            if (text != null && !text.isBlank()) {
                body.append(substitute(text, vars)).append("\n\n");
                codeList.add("SH01");
            }
        });
    }
}
