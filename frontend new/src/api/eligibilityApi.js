import http from './httpClient';

// Base maps to /api/eligibility (controller @RequestMapping("/api/eligibility"))
const base = '/eligibility';

// ── Assessment CRUD ──────────────────────────────────────────────────────────
export const getAssessments = (caseId) =>
  http.get(`${base}/case/${caseId}`).then(r => r.data);

export const getAssessmentById = (id) =>
  http.get(`${base}/${id}`).then(r => r.data);

export const createAssessment = (data) =>
  http.post(`${base}`, data).then(r => r.data);

// ── Home Visit ────────────────────────────────────────────────────────────────
export const recordHomeVisit = (id, homeVisitDate) =>
  http.put(`${base}/${id}/home-visit`, { homeVisitDate }).then(r => r.data);

// ── Service Hours (25 service types) ─────────────────────────────────────────
export const updateServiceHours = (id, data) =>
  http.put(`${base}/${id}/service-hours`, data).then(r => r.data);

export const getTotalHours = (id) =>
  http.get(`${base}/${id}/total-hours`).then(r => r.data);

// ── Functional Ranks (category-level) ────────────────────────────────────────
export const updateFunctionalRanks = (id, data) =>
  http.put(`${base}/${id}/functional-ranks`, data).then(r => r.data);

// ── Share of Cost ─────────────────────────────────────────────────────────────
export const updateShareOfCost = (id, data) =>
  http.put(`${base}/${id}/share-of-cost`, data).then(r => r.data);

// ── Waiver Program ────────────────────────────────────────────────────────────
export const updateWaiverProgram = (id, waiverProgram) =>
  http.put(`${base}/${id}/waiver-program`, { waiverProgram }).then(r => r.data);

// ── Advance Pay ───────────────────────────────────────────────────────────────
export const updateAdvancePay = (id, data) =>
  http.put(`${base}/${id}/advance-pay`, data).then(r => r.data);

// ── Approval Workflow (DSD Section 22) ───────────────────────────────────────
export const submitForApproval = (id) =>
  http.put(`${base}/${id}/submit-for-approval`).then(r => r.data);

export const approveAssessment = (id) =>
  http.put(`${base}/${id}/approve`).then(r => r.data);

export const rejectAssessment = (id, reason) =>
  http.put(`${base}/${id}/reject`, { reason }).then(r => r.data);

// Check Eligibility — preview mode only, no NOA generated (DSD Section 22)
export const checkEligibility = (id) =>
  http.post(`${base}/${id}/check-eligibility`).then(r => r.data);

// ── Health Care Certification (BR SE 28-50) ───────────────────────────────────
export const getHealthCerts = (caseId) =>
  http.get(`${base}/case/${caseId}/health-cert`).then(r => r.data);

export const createHealthCert = (data) =>
  http.post(`${base}/health-cert`, data).then(r => r.data);

export const recordDocumentationReceived = (id, data) =>
  http.put(`${base}/health-cert/${id}/documentation-received`, data).then(r => r.data);

export const requestGoodCauseExtension = (id, data) =>
  http.put(`${base}/health-cert/${id}/good-cause-extension`, data).then(r => r.data);

// ── Legacy aliases (used in CaseDetailPage tabs) ──────────────────────────────
export const getServicePlans = (caseId) =>
  http.get(`${base}/case/${caseId}`).then(r => r.data);

export const scheduleReassessment = (caseId, data) =>
  http.post(`${base}/case/${caseId}/schedule-reassessment`, data).then(r => r.data).catch(() => null);

// ── Authorization Summary (DSD Section 22) ────────────────────────────────────
export const getAuthorizationSummary = (caseId) =>
  http.get(`/cases/${caseId}/authorization-summary`).then(r => r.data);

// ── SOC Spend-Down 4-Week Split (DSD Section 22 BR SE 49) ────────────────────
export const getSocSpendDown = (assessmentId) =>
  http.get(`${base}/${assessmentId}/soc-spend-down`).then(r => r.data);

// ── PRO0927A — send authorized hours to Advantage Payroll ────────────────────
export const sendPro0927A = (assessmentId) =>
  http.post(`${base}/${assessmentId}/send-pro0927a`).then(r => r.data);

// ── Household Evidence (BR SE 26-27) ─────────────────────────────────────────
export const getHouseholdEvidence = (caseId) =>
  http.get(`/cases/${caseId}/household-evidence`).then(r => r.data);

export const createHouseholdEvidence = (caseId, data) =>
  http.post(`/cases/${caseId}/household-evidence`, data).then(r => r.data);

export const updateHouseholdEvidence = (id, data) =>
  http.put(`/cases/household-evidence/${id}`, data).then(r => r.data);

// ── Program Evidence ──────────────────────────────────────────────────────────
export const getProgramEvidence = (caseId) =>
  http.get(`/cases/${caseId}/program-evidence`).then(r => r.data);

export const createProgramEvidence = (caseId, data) =>
  http.post(`/cases/${caseId}/program-evidence`, data).then(r => r.data);

export const updateProgramEvidence = (id, data) =>
  http.put(`/cases/program-evidence/${id}`, data).then(r => r.data);

// ── Disaster Preparedness Contacts ────────────────────────────────────────────
export const getDisasterContacts = (caseId) =>
  http.get(`/cases/${caseId}/disaster-contacts`).then(r => r.data);

export const createDisasterContact = (caseId, data) =>
  http.post(`/cases/${caseId}/disaster-contacts`, data).then(r => r.data);

export const updateDisasterContact = (id, data) =>
  http.put(`/cases/disaster-contacts/${id}`, data).then(r => r.data);

export const inactivateDisasterContact = (id) =>
  http.put(`/cases/disaster-contacts/${id}/inactivate`).then(r => r.data);

// ── County Pay Rates ──────────────────────────────────────────────────────────
export const getAllCountyRates = () =>
  http.get('/county-pay-rates').then(r => r.data);

export const getCountyRates = (countyCode) =>
  http.get(`/county-pay-rates/${countyCode}`).then(r => r.data);

export const getCurrentCountyRate = (countyCode, asOf, rateType) =>
  http.get(`/county-pay-rates/${countyCode}/current`, { params: { asOf, rateType } }).then(r => r.data);

export const createCountyRate = (data) =>
  http.post('/county-pay-rates', data).then(r => r.data);

export const updateCountyRate = (id, data) =>
  http.put(`/county-pay-rates/${id}`, data).then(r => r.data);

// ── Authorization Segments ────────────────────────────────────────────────────
export const getAuthSegments = (caseId) =>
  http.get(`/cases/${caseId}/authorization-segments`).then(r => r.data);

export const createAuthSegment = (caseId, data) =>
  http.post(`/cases/${caseId}/authorization-segments`, data).then(r => r.data);

export const inactivateAuthSegment = (id) =>
  http.put(`/cases/authorization-segments/${id}/inactivate`).then(r => r.data);

// ── Assessment Evidence CRUD (DSD Section 21 child entities) ─────────────────
const evidBase = (assessmentId) => `/assessments/${assessmentId}/evidence`;

// Household Evidence
export const getHouseholdEvidenceByAssessment = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/household`).then(r => r.data);
export const createHouseholdEvidenceForAssessment = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/household`, data).then(r => r.data);
export const updateHouseholdEvidenceForAssessment = (assessmentId, id, data) =>
  http.put(`${evidBase(assessmentId)}/household/${id}`, data).then(r => r.data);

// Household Members
export const getHouseholdMembers = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/household-members`).then(r => r.data);
export const createHouseholdMember = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/household-members`, data).then(r => r.data);
export const updateHouseholdMember = (assessmentId, id, data) =>
  http.put(`${evidBase(assessmentId)}/household-members/${id}`, data).then(r => r.data);
export const deleteHouseholdMember = (assessmentId, id) =>
  http.delete(`${evidBase(assessmentId)}/household-members/${id}`).then(r => r.data);

// Functional Index (14 ranks)
export const getFunctionalIndex = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/functional-index`).then(r => r.data);
export const createFunctionalIndex = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/functional-index`, data).then(r => r.data);
export const updateFunctionalIndex = (assessmentId, id, data) =>
  http.put(`${evidBase(assessmentId)}/functional-index/${id}`, data).then(r => r.data);

// Service Type Evidence (25 service types)
export const getServiceTypes = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/service-types`).then(r => r.data);
export const createServiceType = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/service-types`, data).then(r => r.data);
export const updateServiceType = (assessmentId, id, data) =>
  http.put(`${evidBase(assessmentId)}/service-types/${id}`, data).then(r => r.data);
export const deleteServiceType = (assessmentId, id) =>
  http.delete(`${evidBase(assessmentId)}/service-types/${id}`).then(r => r.data);

// Service Task Evidence (tasks per service type)
export const getServiceTasks = (assessmentId, serviceTypeId) =>
  http.get(`${evidBase(assessmentId)}/service-types/${serviceTypeId}/tasks`).then(r => r.data);
export const createServiceTask = (assessmentId, serviceTypeId, data) =>
  http.post(`${evidBase(assessmentId)}/service-types/${serviceTypeId}/tasks`, data).then(r => r.data);
export const updateServiceTask = (assessmentId, id, data) =>
  http.put(`${evidBase(assessmentId)}/service-tasks/${id}`, data).then(r => r.data);
export const deleteServiceTask = (assessmentId, id) =>
  http.delete(`${evidBase(assessmentId)}/service-tasks/${id}`).then(r => r.data);

// Paramedical Evidence
export const getParamedical = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/paramedical`).then(r => r.data);
export const createParamedical = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/paramedical`, data).then(r => r.data);
export const updateParamedical = (assessmentId, id, data) =>
  http.put(`${evidBase(assessmentId)}/paramedical/${id}`, data).then(r => r.data);

// Protective Supervision
export const getProtectiveSupervision = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/protective-supervision`).then(r => r.data);
export const createProtectiveSupervision = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/protective-supervision`, data).then(r => r.data);

// Disaster Preparedness (per assessment)
export const getDisasterPrepByAssessment = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/disaster-preparedness`).then(r => r.data);
export const createDisasterPrepForAssessment = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/disaster-preparedness`, data).then(r => r.data);

// Share of Cost Evidence
export const getShareOfCostEvidence = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/share-of-cost`).then(r => r.data);
export const createShareOfCostEvidence = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/share-of-cost`, data).then(r => r.data);

// Income Evidence (child of SOC evidence)
export const getIncomeEvidence = (assessmentId, socId) =>
  http.get(`${evidBase(assessmentId)}/share-of-cost/${socId}/income`).then(r => r.data);
export const createIncomeEvidence = (assessmentId, socId, data) =>
  http.post(`${evidBase(assessmentId)}/share-of-cost/${socId}/income`, data).then(r => r.data);

// SAWS Notifications
export const getSAWSNotifications = (assessmentId) =>
  http.get(`${evidBase(assessmentId)}/saws-notifications`).then(r => r.data);
export const createSAWSNotification = (assessmentId, data) =>
  http.post(`${evidBase(assessmentId)}/saws-notifications`, data).then(r => r.data);

// HTG Reference lookup
export const getHTGReference = (assessmentId, params) =>
  http.get(`${evidBase(assessmentId)}/htg`, { params }).then(r => r.data);
