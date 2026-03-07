package com.cmips.service;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.TimesheetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MEDS POS (Point of Service) API Client — DSD SOCDEP 06-09, 29-30.
 *
 * Sends ANSI X12 270/271 eligibility inquiry to MEDS for Medi-Cal SOC verification.
 * Used by SocDeductionEvaluationService when the timesheet pay period falls within
 * the 13-month lookback window and the eligibility status is uncertified ('5').
 *
 * Transaction flow:
 *   1. CMIPS builds 270 eligibility inquiry (subscriber ID = CIN, service date = pay period)
 *   2. Sends to MEDS POS endpoint via REST (replaces legacy CMDS103C batch interface)
 *   3. MEDS returns 271 eligibility response with:
 *      - Benefit status (active/inactive)
 *      - SOC certification status (certified/uncertified)
 *      - SOC amount if applicable
 *      - Error codes if transaction failed
 *
 * Configuration:
 *   meds.pos.api.url       — MEDS POS endpoint URL
 *   meds.pos.api.api-key   — API authentication key
 *   meds.pos.api.timeout   — Request timeout in ms
 *   meds.pos.api.enabled   — true = call real API; false = use mock (default)
 */
@Service
public class MedsPosApiClient {

    private static final Logger log = LoggerFactory.getLogger(MedsPosApiClient.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${meds.pos.api.url:}")
    private String medsApiUrl;

    @Value("${meds.pos.api.api-key:}")
    private String medsApiKey;

    @Value("${meds.pos.api.timeout:5000}")
    private int medsTimeout;

    @Value("${meds.pos.api.enabled:false}")
    private boolean medsEnabled;

    private final RestTemplate restTemplate;

    public MedsPosApiClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send a MEDS POS 270 eligibility inquiry and return the 271 response.
     *
     * @param ts          the timesheet being evaluated
     * @param caseEntity  the associated case (for CIN and recipient info)
     * @return MedsPosResponse with certification status, SOC amount, or error
     */
    public MedsPosResponse queryEligibility(TimesheetEntity ts, CaseEntity caseEntity) {
        if (!medsEnabled || medsApiUrl == null || medsApiUrl.isBlank()) {
            log.info("[MEDS-POS] API disabled or URL not configured — using mock response");
            return mockResponse(caseEntity);
        }

        try {
            log.info("[MEDS-POS] Sending 270 inquiry: case={}, payPeriod={}/{}",
                    caseEntity.getId(), ts.getPayPeriodStart(), ts.getPayPeriodEnd());

            // Build X12 270 request payload
            Map<String, Object> request = build270Request(ts, caseEntity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", medsApiKey);
            headers.set("X-Transaction-Type", "270");
            headers.set("X-Source-System", "CMIPS");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    medsApiUrl + "/eligibility/inquiry",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parse271Response(response.getBody());
            } else {
                log.warn("[MEDS-POS] Non-success response: {}", response.getStatusCode());
                return MedsPosResponse.error("MEDS POS returned HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("[MEDS-POS] API call failed: {}", e.getMessage());
            return MedsPosResponse.error("MEDS POS communication error: " + e.getMessage());
        }
    }

    /**
     * Build a simplified 270 eligibility inquiry payload.
     */
    private Map<String, Object> build270Request(TimesheetEntity ts, CaseEntity caseEntity) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("transactionType", "270");
        req.put("sourceSystem", "CMIPS");
        req.put("subscriberId", caseEntity.getCin() != null ? caseEntity.getCin() : "");
        req.put("caseId", String.valueOf(caseEntity.getId()));
        req.put("recipientId", String.valueOf(ts.getRecipientId()));
        req.put("serviceDate", ts.getPayPeriodStart().format(DATE_FMT));
        req.put("serviceDateEnd", ts.getPayPeriodEnd().format(DATE_FMT));
        req.put("inquiryType", "SOC_VERIFICATION");
        req.put("fundingSource", caseEntity.getFundingSource());
        req.put("mediCalStatus", caseEntity.getMediCalStatus());
        return req;
    }

    /**
     * Parse the 271 eligibility response from MEDS.
     */
    @SuppressWarnings("unchecked")
    private MedsPosResponse parse271Response(Map<String, Object> body) {
        String status = (String) body.getOrDefault("benefitStatus", "UNKNOWN");
        boolean certified = "CERTIFIED".equalsIgnoreCase(
                (String) body.getOrDefault("socCertificationStatus", ""));
        Double socAmount = body.containsKey("socAmount")
                ? ((Number) body.get("socAmount")).doubleValue() : null;
        String errorCode = (String) body.get("errorCode");

        if (errorCode != null && !errorCode.isBlank()) {
            String errorMsg = (String) body.getOrDefault("errorMessage", "MEDS POS error: " + errorCode);
            log.warn("[MEDS-POS] 271 response contains error: {} - {}", errorCode, errorMsg);
            return MedsPosResponse.error(errorMsg);
        }

        log.info("[MEDS-POS] 271 response: benefitStatus={}, certified={}, socAmount={}",
                status, certified, socAmount);

        return new MedsPosResponse(certified, false, null, socAmount, status);
    }

    /**
     * Mock response when MEDS POS API is disabled.
     * Uses case mediCalStatus to simulate realistic responses.
     */
    private MedsPosResponse mockResponse(CaseEntity caseEntity) {
        String status = caseEntity.getMediCalStatus();

        // Status '3' = previously certified
        if (status != null && status.contains("3")) {
            log.info("[MEDS-POS] Mock: mediCalStatus contains '3' → previously certified");
            return new MedsPosResponse(true, false, null,
                    caseEntity.getShareOfCostAmount(), "ACTIVE");
        }

        // Status '5' = uncertified — simulate no previous certification
        if (status != null && status.contains("5")) {
            log.info("[MEDS-POS] Mock: mediCalStatus contains '5' → uncertified, no previous cert");
            return new MedsPosResponse(false, false, null, null, "ACTIVE_UNCERTIFIED");
        }

        // Default: no certification found
        log.info("[MEDS-POS] Mock: default → no certification found");
        return new MedsPosResponse(false, false, null, null, "INACTIVE");
    }

    /**
     * MEDS POS 271 Response DTO.
     */
    public static class MedsPosResponse {
        public final boolean previouslyCertified;
        public final boolean hasError;
        public final String errorMessage;
        public final Double socAmount;
        public final String benefitStatus;

        public MedsPosResponse(boolean previouslyCertified, boolean hasError,
                               String errorMessage, Double socAmount, String benefitStatus) {
            this.previouslyCertified = previouslyCertified;
            this.hasError = hasError;
            this.errorMessage = errorMessage;
            this.socAmount = socAmount;
            this.benefitStatus = benefitStatus;
        }

        public static MedsPosResponse error(String message) {
            return new MedsPosResponse(false, true, message, null, "ERROR");
        }

        public boolean hasError() { return hasError; }
    }
}
