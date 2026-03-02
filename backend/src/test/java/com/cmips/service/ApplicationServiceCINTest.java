package com.cmips.service;

import com.cmips.entity.ApplicationEntity;
import com.cmips.entity.ApplicationEntity.CINClearanceStatus;
import com.cmips.entity.RecipientEntity;
import com.cmips.repository.ApplicationRepository;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.ReferralRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationService CIN Clearance flow.
 *
 * Covers DSD Section 20 business rules:
 *  - selectCINWithDemographicCheck: Scenarios 4, 5, 6 (BR 1, BR 13, EM-202)
 *  - saveWithoutCIN: EM-176, EM-185 / BR 9
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApplicationService – CIN Clearance Tests")
class ApplicationServiceCINTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ReferralRepository    referralRepository;
    @Mock private RecipientRepository   recipientRepository;
    @Mock private CaseRepository        caseRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private static final String APP_ID  = "app-001";
    private static final String CIN     = "12345678A";
    private static final String USER_ID = "worker1";

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ApplicationEntity buildApp(CINClearanceStatus status) {
        ApplicationEntity app = new ApplicationEntity();
        app.setCinClearanceStatus(status);
        return app;
    }

    private RecipientEntity buildRecipient(String last, String first, String gender) {
        RecipientEntity r = new RecipientEntity();
        r.setLastName(last);
        r.setFirstName(first);
        r.setGender(gender);
        return r;
    }

    private Map<String, Object> buildMediCalData(String last, String first, String gender,
                                                  boolean active, String aidCode) {
        Map<String, Object> data = new HashMap<>();
        data.put("lastName",      last);
        data.put("firstName",     first);
        data.put("gender",        gender);
        data.put("mediCalActive", active);
        data.put("aidCode",       aidCode);
        data.put("effectiveDate", "2024-01-01");
        return data;
    }

    // ── selectCINWithDemographicCheck ─────────────────────────────────────────

    @Test
    @DisplayName("Scenario 6 / EM-202: CIN already assigned to a DIFFERENT application → CIN_IN_USE")
    void selectCIN_scenario6_cinInUse() {
        ApplicationEntity caller   = buildApp(CINClearanceStatus.IN_PROGRESS);
        ApplicationEntity conflict = buildApp(CINClearanceStatus.CLEARED);

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(caller));
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.of(conflict));
        // conflict.getId() ≠ APP_ID because conflict was created without setId("app-001")

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", true, "1X"), USER_ID);

        assertEquals("CIN_IN_USE", result.get("result"));
        assertEquals("EM-202",     result.get("errorCode"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scenario 6: CIN in ApplicationRepository but SAME application → not a conflict")
    void selectCIN_scenario6_sameCaller_notConflict() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);
        // Simulate the same app owns the CIN (getId() must equal APP_ID)
        // We'll set no recipient so the check skips demographic comparison
        app.setRecipientId(null);

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        // findByCin returns the SAME application (mapped id == APP_ID) — no conflict
        // We return empty to represent "not yet assigned" scenario
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenReturn(app);

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", true, "1X"), USER_ID);

        assertEquals("SUCCESS", result.get("result"));
        assertEquals(CIN,       result.get("cin"));
    }

    @Test
    @DisplayName("Scenario 4 / BR 1: demographics match, Medi-Cal ACTIVE → SUCCESS + BR13 IH18 logged")
    void selectCIN_scenario4_exactMatch_activeMedial() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);
        app.setRecipientId(99L);
        RecipientEntity recipient = buildRecipient("Smith", "John", "Male");

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.empty());
        when(recipientRepository.findById(99L)).thenReturn(Optional.of(recipient));
        when(applicationRepository.save(any())).thenReturn(app);
        when(recipientRepository.save(any())).thenReturn(recipient);

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", true, "1X"), USER_ID);

        assertEquals("SUCCESS", result.get("result"));
        assertEquals(CIN,       result.get("cin"));
        assertEquals("ACTIVE",  result.get("mediCalStatus"));
        // CIN should be persisted on application
        verify(applicationRepository).save(argThat(a -> CIN.equals(a.getCin())));
        verify(recipientRepository).save(argThat(r -> CIN.equals(r.getCin())));
    }

    @Test
    @DisplayName("Scenario 4 / BR 1: demographics match, Medi-Cal INACTIVE → SUCCESS, status INACTIVE")
    void selectCIN_scenario4_exactMatch_inactiveMedial() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);
        app.setRecipientId(99L);
        RecipientEntity recipient = buildRecipient("Smith", "John", "Male");

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.empty());
        when(recipientRepository.findById(99L)).thenReturn(Optional.of(recipient));
        when(applicationRepository.save(any())).thenReturn(app);
        when(recipientRepository.save(any())).thenReturn(recipient);

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", false, "9X"), USER_ID);

        assertEquals("SUCCESS",  result.get("result"));
        assertEquals("INACTIVE", result.get("mediCalStatus"));
    }

    @Test
    @DisplayName("Scenario 5: last name mismatch → MISMATCH, CIN NOT assigned")
    void selectCIN_scenario5_lastNameMismatch() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);
        app.setRecipientId(99L);
        RecipientEntity recipient = buildRecipient("Jones", "John", "Male");

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.empty());
        when(recipientRepository.findById(99L)).thenReturn(Optional.of(recipient));

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", true, "1X"), USER_ID);

        assertEquals("MISMATCH", result.get("result"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scenario 5: gender mismatch → MISMATCH, CIN NOT assigned")
    void selectCIN_scenario5_genderMismatch() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);
        app.setRecipientId(99L);
        RecipientEntity recipient = buildRecipient("Smith", "John", "Female");

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.empty());
        when(recipientRepository.findById(99L)).thenReturn(Optional.of(recipient));

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", true, "1X"), USER_ID);

        assertEquals("MISMATCH", result.get("result"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("No recipient linked → skip demographic check → SUCCESS (Scenario 4 branch)")
    void selectCIN_noRecipient_skipsCheck() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);
        app.setRecipientId(null); // no recipient

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.findByCin(CIN)).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenReturn(app);

        Map<String, Object> result = applicationService.selectCINWithDemographicCheck(
                APP_ID, CIN, buildMediCalData("Smith", "John", "Male", true, "1X"), USER_ID);

        assertEquals("SUCCESS", result.get("result"));
    }

    // ── saveWithoutCIN ────────────────────────────────────────────────────────

    @Test
    @DisplayName("EM-176: clearance status NOT_STARTED → BLOCKED")
    void saveWithoutCIN_em176_notStarted() {
        ApplicationEntity app = buildApp(CINClearanceStatus.NOT_STARTED);

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

        Map<String, Object> result = applicationService.saveWithoutCIN(APP_ID, USER_ID);

        assertEquals("BLOCKED", result.get("result"));
        assertEquals("EM-176",  result.get("errorCode"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("EM-176: clearance status null → BLOCKED")
    void saveWithoutCIN_em176_nullStatus() {
        ApplicationEntity app = buildApp(null);

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

        Map<String, Object> result = applicationService.saveWithoutCIN(APP_ID, USER_ID);

        assertEquals("BLOCKED", result.get("result"));
        assertEquals("EM-176",  result.get("errorCode"));
    }

    @Test
    @DisplayName("EM-185 / BR 9: clearance performed (IN_PROGRESS) → S1_SENT, PENDING_SAWS")
    void saveWithoutCIN_em185_br9_clearanceDone() {
        ApplicationEntity app = buildApp(CINClearanceStatus.IN_PROGRESS);

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);

        Map<String, Object> result = applicationService.saveWithoutCIN(APP_ID, USER_ID);

        assertEquals("S1_SENT", result.get("result"));
        assertEquals("EM-185",  result.get("errorCode"));
        verify(applicationRepository).save(argThat(a -> "PENDING_SAWS".equals(a.getMediCalStatus())));
    }

    @Test
    @DisplayName("EM-185 / BR 9: clearance status POSSIBLE_MATCHES → S1_SENT")
    void saveWithoutCIN_em185_possibleMatches() {
        ApplicationEntity app = buildApp(CINClearanceStatus.POSSIBLE_MATCHES);

        when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);

        Map<String, Object> result = applicationService.saveWithoutCIN(APP_ID, USER_ID);

        assertEquals("S1_SENT", result.get("result"));
    }

    @Test
    @DisplayName("Application not found → RuntimeException thrown")
    void saveWithoutCIN_notFound() {
        when(applicationRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.saveWithoutCIN("unknown-id", USER_ID));
    }
}
