package com.cmips.controller;

import com.cmips.service.SCIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SCIController.
 *
 * Covers:
 *  - GET /api/sci/search     : MATCHES_FOUND (EM-186), NO_MATCH, service exception
 *  - GET /api/sci/meds-eligibility : SUCCESS, FAILED (Scenario 3), exception
 *  - GET /api/sci/check-cin  : available=true, available=false (EM-202), exception
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SCIController Tests")
class SCIControllerTest {

    @Mock  private SCIService sciService;
    @InjectMocks private SCIController sciController;

    private static final String CIN = "12345678A";

    // ── /search ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OI search: matches found → 200 with MATCHES_FOUND status")
    void sciSearch_matchesFound_returns200() {
        Map<String, Object> serviceResponse = Map.of(
                "status",  "MATCHES_FOUND",
                "message", "EM-186: Valid matches were found",
                "results", List.of(Map.of("cin", CIN))
        );
        when(sciService.sciSearch(anyString(), anyString(), anyString(),
                                  anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(serviceResponse);

        ResponseEntity<?> response = sciController.sciSearch(
                "Smith", "John", "1975-03-15", "Male", null, null, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("MATCHES_FOUND", body.get("status"));
    }

    @Test
    @DisplayName("OI search: no match → 200 with NO_MATCH status")
    void sciSearch_noMatch_returns200() {
        Map<String, Object> serviceResponse = Map.of(
                "status",  "NO_MATCH",
                "message", "CIN does not exist for the applicant",
                "results", List.of()
        );
        when(sciService.sciSearch(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(serviceResponse);

        ResponseEntity<?> response = sciController.sciSearch(
                "Unknown", "Person", null, null, null, null, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("NO_MATCH", body.get("status"));
    }

    @Test
    @DisplayName("OI search: service throws exception → 400 with error")
    void sciSearch_serviceException_returns400() {
        when(sciService.sciSearch(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("SCI mainframe unavailable"));

        ResponseEntity<?> response = sciController.sciSearch(
                "Smith", "John", null, null, null, null, false);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
    }

    @Test
    @DisplayName("OI search (BR 32): CIN parameter passed through to service")
    void sciSearch_br32_cinPresent_passedToService() {
        when(sciService.sciSearch(any(), any(), any(), any(), eq(CIN), any(), anyBoolean()))
                .thenReturn(Map.of("status", "MATCHES_FOUND", "results", List.of()));

        sciController.sciSearch("Smith", "John", "1975-03-15", "Male", CIN, "111-22-3333", false);

        verify(sciService).sciSearch("Smith", "John", "1975-03-15", "Male", CIN, "111-22-3333", false);
    }

    @Test
    @DisplayName("OI search (Medi-Cal Pseudo): mediCalPseudo=true passed through to service")
    void sciSearch_mediCalPseudo_passedToService() {
        when(sciService.sciSearch(any(), any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(Map.of("status", "NO_MATCH", "results", List.of()));

        sciController.sciSearch("Smith", "John", null, null, null, null, true);

        verify(sciService).sciSearch(any(), any(), any(), any(), any(), any(), eq(true));
    }

    // ── /meds-eligibility ────────────────────────────────────────────────────

    @Test
    @DisplayName("EL/OM: known CIN with ACTIVE eligibility → 200 SUCCESS")
    void getMediCalEligibility_activeCin_returns200() {
        Map<String, Object> eligibility = Map.of(
                "status",            "SUCCESS",
                "cin",               CIN,
                "eligibilityStatus", "ACTIVE",
                "mediCalActive",     true
        );
        when(sciService.getMediCalEligibility(CIN)).thenReturn(eligibility);

        ResponseEntity<?> response = sciController.getMediCalEligibility(CIN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("SUCCESS", body.get("status"));
        assertEquals(CIN,       body.get("cin"));
    }

    @Test
    @DisplayName("EL/OM Scenario 3: unknown CIN → 200 with FAILED status")
    void getMediCalEligibility_unknownCin_failedStatus() {
        when(sciService.getMediCalEligibility("UNKNOWN-CIN")).thenReturn(
                Map.of("status", "FAILED", "message", "SCI EL transaction was not successful"));

        ResponseEntity<?> response = sciController.getMediCalEligibility("UNKNOWN-CIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("FAILED", body.get("status"));
    }

    @Test
    @DisplayName("EL/OM: service exception → 400 with error")
    void getMediCalEligibility_exception_returns400() {
        when(sciService.getMediCalEligibility(any()))
                .thenThrow(new RuntimeException("Network timeout"));

        ResponseEntity<?> response = sciController.getMediCalEligibility(CIN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── /check-cin ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CIN availability: not taken → 200 { available: true }")
    void checkCinAvailability_available() {
        when(sciService.checkCinAvailability(CIN, "app-001"))
                .thenReturn(Map.of("available", true));

        ResponseEntity<?> response = sciController.checkCinAvailability(CIN, "app-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("available"));
    }

    @Test
    @DisplayName("CIN availability / EM-202: taken by other → { available: false, errorCode: EM-202 }")
    void checkCinAvailability_em202_taken() {
        when(sciService.checkCinAvailability(CIN, "app-002")).thenReturn(Map.of(
                "available",  false,
                "errorCode",  "EM-202",
                "message",    "Person record with indicated CIN already exists."
        ));

        ResponseEntity<?> response = sciController.checkCinAvailability(CIN, "app-002");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(false,   body.get("available"));
        assertEquals("EM-202", body.get("errorCode"));
    }

    @Test
    @DisplayName("CIN availability: no applicationId (default empty string) → delegated to service")
    void checkCinAvailability_defaultApplicationId() {
        when(sciService.checkCinAvailability(CIN, ""))
                .thenReturn(Map.of("available", true));

        ResponseEntity<?> response = sciController.checkCinAvailability(CIN, "");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sciService).checkCinAvailability(CIN, "");
    }

    @Test
    @DisplayName("CIN availability: service exception → 400 with error")
    void checkCinAvailability_exception_returns400() {
        when(sciService.checkCinAvailability(any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = sciController.checkCinAvailability(CIN, "app-001");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
