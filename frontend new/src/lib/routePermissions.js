/**
 * Route Permission Mapping
 * Maps frontend route path prefixes to required Keycloak roles.
 * Based on backend @RequirePermission annotations and Keycloak ROLE_GROUPS.
 *
 * Each entry: { pathPrefix, roles[] }
 * A user must have at least ONE of the listed roles to access routes matching the prefix.
 * Routes not matching any prefix require only authentication (RequireAuth).
 */

// Role constants matching setup_keycloak_authorization.py
const CASE_ROLES = [
  'CASEMANAGEMENTROLE', 'CASEMANAGEMENTSUPERVISORROLE', 'CASEMANAGEMENTWITHAPPROVALROLE',
  'CASEMGMTPROVMGMTROLE', 'CASEMGMTWITHAPPROVALPROVMGMTROLE',
  'CASEMGMTPAYROLLROLE', 'CASEMGMTPAYROLLAPPROVERROLE',
  'CASEMGMTPROVMGMTPAYROLLROLE', 'CASEMGMTPROVMGMTPAYROLLAPPROVERROLE',
  'CASEMGMTWITHAPPROVALPAYROLLROLE', 'CASEMGMTWITHAPPROVALPAYROLLAPPROVERROLE',
  'CASEMGMTWITHAPPROVALPROVMGMTPAYROLLROLE', 'CASEMGMTWITHAPPROVALPROVMGMTPAYROLLAPPROVERROLE',
  'HELPDESKROLE', 'COUNTYVIEWONLYROLE'
];

const PERSON_ROLES = [
  ...CASE_ROLES,
  'INTAKEROLE', 'INTAKESUPERVISORROLE', 'REFERRALINTAKEROLE', 'CALLCENTERROLE',
  'ELIGIBILITYROLE', 'ELIGIBILITYSUPERVISORROLE'
];

const PROVIDER_ROLES = [
  'PROVIDERMANAGEMENTROLE', 'PROVIDERMGMTPAYROLLAPPROVERROLE', 'PROVIDERMGMTPAYROLLMGMTROLE',
  'CASEMGMTPROVMGMTROLE', 'CASEMGMTWITHAPPROVALPROVMGMTROLE',
  'CASEMGMTPROVMGMTPAYROLLROLE', 'CASEMGMTPROVMGMTPAYROLLAPPROVERROLE',
  'CASEMGMTWITHAPPROVALPROVMGMTPAYROLLROLE', 'CASEMGMTWITHAPPROVALPROVMGMTPAYROLLAPPROVERROLE',
  'PUBLICAUTHORITYROLE', 'PABENEFITSROLE', 'PAPROVIDERENROLLROLE', 'PAPROVIDERENROLLBENEFITSROLE'
];

const PAYMENT_ROLES = [
  'PAYROLLROLE', 'PAYROLLMGMTROLE', 'CROSSCOUNTYPAYROLLROLE', 'WPCSROLE',
  'HPWARRANTREPLACEMENTROLE', 'HPETRAVELCLAIMPAYMENTROLE',
  'TIMESHEETROLE', 'BVINOAANDTIMESHEETMANAGEMENTROLE', 'ETIMESHEETHELPDESKROLE',
  'HDBVINOAANDTSMGMTROLE',
  'CASEMGMTPAYROLLROLE', 'CASEMGMTPAYROLLAPPROVERROLE',
  'CASEMGMTPROVMGMTPAYROLLROLE', 'CASEMGMTPROVMGMTPAYROLLAPPROVERROLE',
  'CASEMGMTWITHAPPROVALPAYROLLROLE', 'CASEMGMTWITHAPPROVALPAYROLLAPPROVERROLE',
  'PROVIDERMGMTPAYROLLAPPROVERROLE', 'PROVIDERMGMTPAYROLLMGMTROLE'
];

const SUPERVISOR_ROLES = [
  'CASEMANAGEMENTSUPERVISORROLE', 'SUPERVISOR',
  'INTAKESUPERVISORROLE', 'ELIGIBILITYSUPERVISORROLE'
];

const EVIDENCE_ROLES = [
  ...CASE_ROLES,
  'ELIGIBILITYROLE', 'ELIGIBILITYSUPERVISORROLE',
  'HPMEDSELIGBA1ROLE', 'HPMEDSELIGBA2ROLE', 'HPMEDSELIGBA3ROLE', 'HPMEDSELIGBA4ROLE'
];

const ADMIN_ROLES = [
  'ADMIN'
];

const HOMEMAKER_ROLES = [
  'HOMEMAKERROLE', ...CASE_ROLES
];

const HELPDESK_ROLES = [
  'HELPDESKROLE', 'ETIMESHEETHELPDESKROLE', 'HDBVINOAANDTSMGMTROLE'
];

const COUNTY_ROLES = [
  ...ADMIN_ROLES, 'COUNTYVIEWONLYROLE', 'PROGRAMMGMTROLE'
];

const CDSS_ROLES = [
  'CDSSVIEWROLE', 'CDSSFISCALROLE', 'CDSSMODIFYROLE', 'CDSSPROGRAMMGMTROLE'
];

export const ANALYTICS_ROLES = [
  'PROGRAMMGMTROLE', ...ADMIN_ROLES, ...CDSS_ROLES, ...SUPERVISOR_ROLES
];

/**
 * Domain-to-route-prefix permission mapping.
 * Ordered from most specific to least specific prefixes.
 */
export const ROUTE_PERMISSIONS = [
  // Admin routes
  { pathPrefix: '/organization/', roles: ADMIN_ROLES },
  // Supervisor routes
  { pathPrefix: '/supervisor/', roles: SUPERVISOR_ROLES },
  // Case routes
  { pathPrefix: '/case/', roles: CASE_ROLES },
  // Person/Recipient routes
  { pathPrefix: '/person/', roles: PERSON_ROLES },
  // Provider routes
  { pathPrefix: '/provider/', roles: PROVIDER_ROLES },
  // Payment routes
  { pathPrefix: '/payment/', roles: PAYMENT_ROLES },
  // Evidence routes
  { pathPrefix: '/evidence/', roles: EVIDENCE_ROLES },
  // Task management routes
  { pathPrefix: '/task-management/', roles: CASE_ROLES },
  // Homemaker routes
  { pathPrefix: '/homemaker/', roles: HOMEMAKER_ROLES },
  // County routes
  { pathPrefix: '/county/', roles: COUNTY_ROLES },
  // Help desk routes
  { pathPrefix: '/help-desk/', roles: HELPDESK_ROLES },
  // Back office routes
  { pathPrefix: '/back-office/', roles: ADMIN_ROLES },
  // Misc routes (generally accessible)
  { pathPrefix: '/misc/', roles: null },
];

/**
 * Returns the required roles for a given route path, or null if no specific roles required.
 */
export function getRequiredRoles(routePath) {
  for (const { pathPrefix, roles } of ROUTE_PERMISSIONS) {
    if (routePath.startsWith(pathPrefix)) {
      return roles;
    }
  }
  return null; // No specific roles required — authentication is enough
}

export default ROUTE_PERMISSIONS;
