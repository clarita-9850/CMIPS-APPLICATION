/**
 * DSD Case Lifecycle Code Tables
 * Per DSD Section 6 - Code Table Definitions
 * These mirror the backend CaseCodeTables.java for offline/fallback use
 */

// 6.1 Case Status Codes (CI-68084)
export const CASE_STATUS_CODES = {
  CS001: 'Pending',
  CS002: 'Eligible',
  CS003: 'Presumptive Eligible',
  CS004: 'Leave',
  CS005: 'Terminated',
  CS006: 'Denied',
  CS007: 'Application Withdrawn',
  CS008: 'In-Progress',
  CS009: 'Active',
  CS010: 'Inactive',
};

// 6.2 Withdrawal Reason Codes (CI-68077)
export const WITHDRAWAL_REASONS = {
  WO001: 'Withdrawal requested by Recipient',
};

// 6.3 Leave Reason Codes (CI-68081) - enabled only
export const LEAVE_REASONS = {
  L0001: 'Temporarily in Hospital',
  L0002: 'Temporarily in SNF',
  L0003: 'Temporarily in ICF',
  L0004: 'Temporarily in CCF',
  L0005: 'Temporarily out of State over 6 months',
  L0006: 'Undervalue disposal of resources',
  L0008: 'Other Facility',
};

// 6.4 Termination Reason Codes (CI-68074) - enabled only
export const TERMINATION_REASONS = {
  CC501: 'No longer in own home',
  CC502: 'Recipient request',
  CC503: 'Recipient did not pay IHSS Share of Cost',
  CC504: 'Out of State longer than 60 days',
  CC505: 'Moved out of State',
  CC506: 'Failure to provide needed information',
  CC507: 'Not returning home from Hospital',
  CC508: 'Not returning home from CCF',
  CC509: 'Not returning home from ICF',
  CC510: 'Not returning home from SNF',
  CC511: 'Recipient Death',
  CC512: 'Out of Country longer than 30 days',
  CC513: 'Whereabouts unknown',
  CC514: 'Non-cooperation with Medi-Cal',
  CC516: 'Suspect SSN',
  CC517: 'Duplicate SSN',
  CC518: 'Health Care Certification - Not Received',
  CC519: 'Non-Compliance - UHV',
  CC520: 'Health Care Certification - No Need',
  CC522: 'Enrolled in PACE program',
};

// 6.5 Rescind Reason Codes (CI-68089) - enabled only
export const RESCIND_REASONS = {
  R0001: 'State Hearing Filed before Termination effective',
  R0002: 'Recipient rescinds request for termination of services',
  R0003: 'Administrative Error',
  R0004: 'State Hearing Decision',
};

// Referral Source Codes
export const REFERRAL_SOURCES = {
  SELF: 'Self-Referral',
  FAMILY: 'Family Member',
  AGENCY: 'Agency Referral',
  HOSPITAL: 'Hospital/Medical Facility',
  APS: 'Adult Protective Services',
  OTHER: 'Other',
};

// 6.6 State Hearing Search Status (CI-68051)
export const STATE_HEARING_SEARCH_STATUS = {
  SSHS001: 'Requested',
  SSHS002: 'Scheduled',
  SSHS003: 'Resolved',
  SSHS004: 'Requested And Scheduled',
};

// 6.7 Case Appeal Status (auto-calculated per BR CM 04-06a)
export const CASE_APPEAL_STATUS = {
  CS1: 'Requested',
  CS2: 'Scheduled',
  CS3: 'Resolved',
};

// 6.8 Case Appeal Outcome (DSD EO codes)
export const CASE_APPEAL_OUTCOME = {
  EO501: 'Pending',                       // Default on Create
  EO2:   'Grant',
  EO3:   'Denial',
  EO502: 'Conditional Withdrawal',
  EO503: 'Complete Withdrawal',
  EO504: 'Partial Grant/Partial Denial',
  EO505: 'Dismissal',
  EO506: 'Erroneous Entry',
};

// 6.9 Rescheduled Reason (DSD RS codes)
export const RESCHEDULED_REASON = {
  RS001: 'Recipient Request',
  RS002: 'County Request',
  RS003: 'Administrative Law Judge Request',
};

// Meets Residency Requirement
export const RESIDENCY_REQUIREMENTS = {
  YES: 'Meets Residency Requirement',
  NO: 'Does Not Meet Residency Requirement',
  NON_CA: 'Non-California Residence',
  WHEREABOUTS_UNKNOWN: 'Whereabouts Unknown',
  NO_LONGER_IN_HOME: 'No Longer in Own Home',
};
