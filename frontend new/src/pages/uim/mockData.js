/**
 * Mock data for UIM-converted pages.
 */

export const mockPersonSearchResults = [
  {
    id: 1, concernRoleID: 'CR-10001', personFullName: 'Smith, John A.',
    socialSecurityNumber: '***-**-1234', ssnType: 'SSN', cin: 'A1234567',
    dateOfBirth: '03/15/1975', sex: 'Male', personType: 'Recipient',
    residenceAddress: '123 Main St', city: 'Sacramento', countyCode: 'Sacramento',
  },
  {
    id: 2, concernRoleID: 'CR-10002', personFullName: 'Lee, Maria R.',
    socialSecurityNumber: '***-**-5678', ssnType: 'SSN', cin: 'B2345678',
    dateOfBirth: '07/22/1988', sex: 'Female', personType: 'Provider',
    residenceAddress: '456 Oak Ave', city: 'Fresno', countyCode: 'Fresno',
  },
  {
    id: 3, concernRoleID: 'CR-10003', personFullName: 'Johnson, Robert T.',
    socialSecurityNumber: '***-**-9012', ssnType: 'ITIN', cin: 'C3456789',
    dateOfBirth: '11/30/1960', sex: 'Male', personType: 'Recipient',
    residenceAddress: '789 Pine Blvd', city: 'Los Angeles', countyCode: 'Los Angeles',
  },
];

export const mockCountyOptions = [
  { value: '', label: '-- Select County --' },
  { value: 'Alameda', label: 'Alameda' },
  { value: 'Fresno', label: 'Fresno' },
  { value: 'Los Angeles', label: 'Los Angeles' },
  { value: 'Sacramento', label: 'Sacramento' },
  { value: 'San Diego', label: 'San Diego' },
  { value: 'San Francisco', label: 'San Francisco' },
  { value: 'Santa Clara', label: 'Santa Clara' },
];

export const mockDistrictOfficeOptions = [
  { value: '', label: '-- Select District Office --' },
  { value: 'DO-001', label: 'Sacramento Central DO' },
  { value: 'DO-002', label: 'Sacramento North DO' },
  { value: 'DO-003', label: 'Fresno DO' },
  { value: 'DO-004', label: 'Los Angeles Metro DO' },
  { value: 'DO-005', label: 'San Diego DO' },
];

export const mockStateHearingResults = [
  { caseID: 'CASE-55001', caseNumber: 'CASE-55001', recipientName: 'Smith, John A.', stateHearingStatus: 'Scheduled', appealID: 'APL-1001' },
  { caseID: 'CASE-55002', caseNumber: 'CASE-55002', recipientName: 'Lee, Maria R.', stateHearingStatus: 'Pending', appealID: 'APL-1002' },
  { caseID: 'CASE-55003', caseNumber: 'CASE-55003', recipientName: 'Johnson, Robert T.', stateHearingStatus: 'Completed', appealID: 'APL-1003' },
];

export const mockMergeDuplicateSSN = {
  ssn: '***-**-1234',
  masterRecord: 'A1234567',
  makeMaster: false,
  duplicates: [
    { rec: 'B2345678' },
    { rec: 'C3456789' },
    { rec: '' },
    { rec: '' },
    { rec: '' },
  ],
};

export const mockWarrantReplacements = [
  {
    replacementEntryDate: '01/15/2025',
    replacementDate: '01/20/2025',
    scoWarrantNumber: 'WR-200001',
    issueDate: '12/31/2024',
    amount: '$1,250.00',
    countyCode: 'Sacramento',
    caseNumber: 'CASE-55001',
    recipientFullName: 'Smith, John A.',
    payeeNumber: 'PR-10001',
    payeeFullName: 'Lee, Maria R.',
  },
  {
    replacementEntryDate: '02/01/2025',
    replacementDate: '02/05/2025',
    scoWarrantNumber: 'WR-200002',
    issueDate: '01/15/2025',
    amount: '$980.50',
    countyCode: 'Fresno',
    caseNumber: 'CASE-55002',
    recipientFullName: 'Johnson, Robert T.',
    payeeNumber: 'PR-10002',
    payeeFullName: 'Garcia, Ana M.',
  },
];
