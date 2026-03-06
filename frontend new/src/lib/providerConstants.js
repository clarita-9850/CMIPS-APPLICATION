/**
 * Shared provider constants — dropdown options, code tables, and status mappings.
 * Derived from DSD code table definitions.
 */

// All 58 California counties
export const COUNTY_OPTIONS = [
  { value: '', label: 'Select County...' },
  { value: '01', label: 'Alameda' },
  { value: '02', label: 'Alpine' },
  { value: '03', label: 'Amador' },
  { value: '04', label: 'Butte' },
  { value: '05', label: 'Calaveras' },
  { value: '06', label: 'Colusa' },
  { value: '07', label: 'Contra Costa' },
  { value: '08', label: 'Del Norte' },
  { value: '09', label: 'El Dorado' },
  { value: '10', label: 'Fresno' },
  { value: '11', label: 'Glenn' },
  { value: '12', label: 'Humboldt' },
  { value: '13', label: 'Imperial' },
  { value: '14', label: 'Inyo' },
  { value: '15', label: 'Kern' },
  { value: '16', label: 'Kings' },
  { value: '17', label: 'Lake' },
  { value: '18', label: 'Lassen' },
  { value: '19', label: 'Los Angeles' },
  { value: '20', label: 'Madera' },
  { value: '21', label: 'Marin' },
  { value: '22', label: 'Mariposa' },
  { value: '23', label: 'Mendocino' },
  { value: '24', label: 'Merced' },
  { value: '25', label: 'Modoc' },
  { value: '26', label: 'Mono' },
  { value: '27', label: 'Monterey' },
  { value: '28', label: 'Napa' },
  { value: '29', label: 'Nevada' },
  { value: '30', label: 'Orange' },
  { value: '31', label: 'Placer' },
  { value: '32', label: 'Plumas' },
  { value: '33', label: 'Riverside' },
  { value: '34', label: 'Sacramento' },
  { value: '35', label: 'San Benito' },
  { value: '36', label: 'San Bernardino' },
  { value: '37', label: 'San Diego' },
  { value: '38', label: 'San Francisco' },
  { value: '39', label: 'San Joaquin' },
  { value: '40', label: 'San Luis Obispo' },
  { value: '41', label: 'San Mateo' },
  { value: '42', label: 'Santa Barbara' },
  { value: '43', label: 'Santa Clara' },
  { value: '44', label: 'Santa Cruz' },
  { value: '45', label: 'Shasta' },
  { value: '46', label: 'Sierra' },
  { value: '47', label: 'Siskiyou' },
  { value: '48', label: 'Solano' },
  { value: '49', label: 'Sonoma' },
  { value: '50', label: 'Stanislaus' },
  { value: '51', label: 'Sutter' },
  { value: '52', label: 'Tehama' },
  { value: '53', label: 'Trinity' },
  { value: '54', label: 'Tulare' },
  { value: '55', label: 'Tuolumne' },
  { value: '56', label: 'Ventura' },
  { value: '57', label: 'Yolo' },
  { value: '58', label: 'Yuba' },
];

// DSD Code Table: Enrollment Eligibility Code
export const ELIGIBLE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'YES', label: 'Yes' },
  { value: 'NO', label: 'No' },
];

// DSD Code Table: Provider Ineligible Code (14 values)
export const INELIGIBLE_REASON_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'CORI_TIER1', label: 'CORI Tier 1 Conviction' },
  { value: 'CORI_TIER2', label: 'CORI Tier 2 Conviction' },
  { value: 'MEDI_CAL_SUSPENDED', label: 'Medi-Cal Suspended/Ineligible' },
  { value: 'FAILED_ENROLLMENT', label: 'Failed to Complete Enrollment' },
  { value: 'INACTIVE_12_MONTHS', label: 'No Payroll Activity for 12 Months' },
  { value: 'OT_VIOLATION_3', label: 'Overtime Violation 3 (90-day)' },
  { value: 'OT_VIOLATION_4', label: 'Overtime Violation 4 (365-day)' },
  { value: 'VOLUNTARY_WITHDRAWAL', label: 'Voluntary Withdrawal' },
  { value: 'DECEASED', label: 'Deceased' },
  { value: 'SSN_DECEASED', label: 'SSN Belongs to Deceased' },
  { value: 'SSN_INVALID', label: 'SSN Invalid/Not Issued' },
  { value: 'MOVED_OUT_OF_STATE', label: 'Moved Out of State' },
  { value: 'OTHER', label: 'Other' },
];

// DSD Code Table: Provider Appeal Status
export const APPEAL_STATUS_OPTIONS = [
  { value: '', label: 'None' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'SCHEDULED', label: 'Scheduled' },
  { value: 'UPHELD', label: 'Upheld' },
  { value: 'OVERTURNED', label: 'Overturned' },
  { value: 'WITHDRAWN', label: 'Withdrawn' },
];

// DSD Code Table: CORI Tier
export const CORI_TIER_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'TIER_1', label: 'Tier 1 - Ineligible (No Waiver)' },
  { value: 'TIER_2', label: 'Tier 2 - Eligible with Waiver' },
];

// DSD Code Table: SSN Verification Status
export const SSN_VERIFICATION_STATUS = {
  VERIFIED: { label: 'Verified', color: '#38a169', cssClass: 'uim-badge-green' },
  NAME_MISMATCH: { label: 'Name Mismatch', color: '#d69e2e', cssClass: 'uim-badge-yellow' },
  DOB_MISMATCH: { label: 'DOB Mismatch', color: '#d69e2e', cssClass: 'uim-badge-yellow' },
  NAME_DOB_MISMATCH: { label: 'Name & DOB Mismatch', color: '#e53e3e', cssClass: 'uim-badge-red' },
  NOT_FOUND: { label: 'Not Found', color: '#e53e3e', cssClass: 'uim-badge-red' },
  DECEASED: { label: 'Deceased', color: '#e53e3e', cssClass: 'uim-badge-red' },
  INVALID_FORMAT: { label: 'Invalid Format', color: '#e53e3e', cssClass: 'uim-badge-red' },
  NOT_ISSUED: { label: 'Not Issued', color: '#e53e3e', cssClass: 'uim-badge-red' },
  NOT_YET_VERIFIED: { label: 'Not Yet Verified', color: '#718096', cssClass: 'uim-badge-gray' },
};

// Background Check Status
export const BACKGROUND_CHECK_STATUS = {
  CLEAR: { label: 'Clear', cssClass: 'uim-badge-green' },
  RECORD_FOUND: { label: 'Record Found', cssClass: 'uim-badge-yellow' },
  PENDING: { label: 'Pending', cssClass: 'uim-badge-gray' },
};

// Title prefix options
export const TITLE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'MR', label: 'Mr' },
  { value: 'MRS', label: 'Mrs' },
  { value: 'MS', label: 'Ms' },
  { value: 'DR', label: 'Dr' },
];

// Suffix options
export const SUFFIX_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'JR', label: 'Jr' },
  { value: 'SR', label: 'Sr' },
  { value: 'II', label: 'II' },
  { value: 'III', label: 'III' },
  { value: 'IV', label: 'IV' },
];

// Gender options
export const GENDER_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'NON_BINARY', label: 'Non-Binary' },
  { value: 'OTHER', label: 'Other' },
];

// Language options
export const LANGUAGE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'ENGLISH', label: 'English' },
  { value: 'SPANISH', label: 'Spanish' },
  { value: 'CHINESE_CANTONESE', label: 'Chinese (Cantonese)' },
  { value: 'CHINESE_MANDARIN', label: 'Chinese (Mandarin)' },
  { value: 'TAGALOG', label: 'Tagalog' },
  { value: 'VIETNAMESE', label: 'Vietnamese' },
  { value: 'KOREAN', label: 'Korean' },
  { value: 'ARMENIAN', label: 'Armenian' },
  { value: 'RUSSIAN', label: 'Russian' },
  { value: 'FARSI', label: 'Farsi' },
  { value: 'HMONG', label: 'Hmong' },
  { value: 'CAMBODIAN', label: 'Cambodian' },
  { value: 'OTHER', label: 'Other' },
];

// Provider Type options
export const PROVIDER_TYPE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'INDIVIDUAL_PROVIDER', label: 'Individual Provider' },
  { value: 'HOME_CARE_AGENCY', label: 'Home Care Agency' },
  { value: 'LIVE_IN', label: 'Live-In' },
  { value: 'RESPITE', label: 'Respite' },
];

// Phone Type options
export const PHONE_TYPE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'HOME', label: 'Home' },
  { value: 'CELL', label: 'Cell' },
  { value: 'WORK', label: 'Work' },
];

// Address Type options
export const ADDRESS_TYPE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'RESIDENTIAL', label: 'Residential' },
  { value: 'MAILING', label: 'Mailing' },
  { value: 'PO_BOX', label: 'P.O. Box' },
];

// Blank SSN Reason options
export const BLANK_SSN_REASON_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'APPLIED_FOR_SSN', label: 'Applied For SSN' },
  { value: 'APPLIED_FOR_ITIN', label: 'Applied For ITIN' },
];

// State options (default CA)
export const STATE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'CA', label: 'California' },
  { value: 'AZ', label: 'Arizona' },
  { value: 'NV', label: 'Nevada' },
  { value: 'OR', label: 'Oregon' },
  { value: 'WA', label: 'Washington' },
];

// District Office options
export const DISTRICT_OFFICE_OPTIONS = [
  { value: '', label: 'Select District Office...' },
  { value: 'DO-001', label: 'Sacramento Central DO' },
  { value: 'DO-002', label: 'Sacramento North DO' },
  { value: 'DO-003', label: 'Fresno DO' },
  { value: 'DO-004', label: 'Los Angeles Metro DO' },
  { value: 'DO-005', label: 'San Diego DO' },
  { value: 'DO-006', label: 'San Francisco DO' },
  { value: 'DO-007', label: 'San Jose DO' },
  { value: 'DO-008', label: 'Oakland DO' },
  { value: 'DO-009', label: 'Riverside DO' },
  { value: 'DO-010', label: 'San Bernardino DO' },
];

// ==================== County Contractor Constants ====================

// DSD Code Table: County Contractor Invoice Status
export const INVOICE_STATUS_OPTIONS = [
  { value: 'Pending', label: 'Pending' },
  { value: 'Paid', label: 'Paid' },
];

// DSD: Service Period codes
export const SERVICE_PERIOD_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'A', label: 'A - Days 1-15' },
  { value: 'B', label: 'B - Days 16-31' },
  { value: 'C', label: 'C - Monthly' },
];

// DSD: Funding Source / Aid Code options
export const FUNDING_SOURCE_OPTIONS = [
  { value: '', label: 'Select...' },
  { value: 'WPCS', label: 'WPCS' },
  { value: 'IHSS-R', label: 'IHSS-R' },
  { value: 'PCSP', label: 'PCSP' },
  { value: 'IPW1', label: 'IPW1' },
  { value: 'IPW2-6', label: 'IPW2-6' },
  { value: 'IPO1-5', label: 'IPO 1-5' },
  { value: 'CFCO', label: 'CFCO' },
];

// Helper: Get county label by value
export function getCountyLabel(value) {
  const found = COUNTY_OPTIONS.find(o => o.value === value);
  return found ? found.label : value || '\u2014';
}

// Helper: Get ineligible reason label by value
export function getIneligibleReasonLabel(value) {
  const found = INELIGIBLE_REASON_OPTIONS.find(o => o.value === value);
  return found ? found.label : value || '\u2014';
}

// Helper: Format date string for display
export function formatDate(dateStr) {
  if (!dateStr) return '\u2014';
  try {
    return new Date(dateStr).toLocaleDateString();
  } catch {
    return dateStr;
  }
}
