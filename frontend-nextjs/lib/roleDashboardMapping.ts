/**
 * Maps Keycloak roles (realm + client) to CMIPS dashboard types.
 * Supports all CMIPS Keycloak composite roles - not just the 5 base roles.
 *
 * Priority: ADMIN > SUPERVISOR > CASE_WORKER > PROVIDER > RECIPIENT
 */

export type DashboardType = 'ADMIN' | 'SUPERVISOR' | 'CASE_WORKER' | 'PROVIDER' | 'RECIPIENT' | 'USER';

const PRIORITY: Record<DashboardType, number> = {
  ADMIN: 5,
  SUPERVISOR: 4,
  CASE_WORKER: 3,
  PROVIDER: 2,
  RECIPIENT: 1,
  USER: 0,
};

/** Roles that map to ADMIN dashboard (Keycloak Admin Console / Keycloak management) */
const ADMIN_ROLES = new Set([
  'ADMIN',
  'UAADMINROLE',
  'HPADMIN',
  'COUNTYSECURITYADMINROLE',
  'SYSTEMROLE',
]);

/** Roles that map to SUPERVISOR dashboard */
const SUPERVISOR_ROLES = new Set([
  'SUPERVISOR',
  'SUPERVISORROLE',
  'SUPERROLE',
  'CASEMANAGEMENTSUPERVISORROLE',
  'ELIGIBILITYSUPERVISORROLE',
  'INTAKESUPERVISORROLE',
  'HOMEMAKERSUPERVISOR',
]);

/** Roles that map to PROVIDER dashboard (IHSS provider - exact match to avoid PROVIDERMANAGEMENTROLE) */
const PROVIDER_ROLES = new Set(['PROVIDER']);

/** Roles that map to RECIPIENT dashboard (IHSS recipient) */
const RECIPIENT_ROLES = new Set(['RECIPIENT']);

/** System/Keycloak roles to ignore when resolving dashboard */
const IGNORED_ROLES = new Set([
  'offline_access',
  'uma_authorization',
  'default-roles-cmips',
  'BASESECURITYGROUP',
]);

/** Check if role should be ignored */
function isIgnored(role: string): boolean {
  const r = (role || '').trim();
  if (!r) return true;
  if (IGNORED_ROLES.has(r)) return true;
  if (r.startsWith('default-roles-')) return true;
  return false;
}

/** Map a single role to dashboard type, or null if no mapping */
function mapRoleToDashboard(role: string): DashboardType | null {
  const r = (role || '').toUpperCase().trim();
  if (!r) return null;

  if (ADMIN_ROLES.has(r)) return 'ADMIN';
  if (SUPERVISOR_ROLES.has(r)) return 'SUPERVISOR';
  if (PROVIDER_ROLES.has(r)) return 'PROVIDER';
  if (RECIPIENT_ROLES.has(r)) return 'RECIPIENT';

  // Pattern: *SUPERVISOR* (but not CASEMANAGEMENTSUPERVISOR - already in set)
  if (r.includes('SUPERVISOR')) return 'SUPERVISOR';

  // CASE_WORKER: all other staff roles (CMIPS composite roles, groups, etc.)
  // Includes: CASEMANAGEMENT*, PAYROLL*, INTAKE*, ELIGIBILITY*, PROVIDERMANAGEMENT*, etc.
  if (
    r.includes('CASEMANAGEMENT') ||
    r.includes('PAYROLL') ||
    r.includes('INTAKE') ||
    r.includes('ELIGIBILITY') ||
    r.includes('PROVIDERMANAGEMENT') ||
    r.includes('REFERRAL') ||
    r.includes('HOMEMAKER') && !r.endsWith('PROVIDER') ||
    r.includes('TIMESHEET') ||
    r.includes('AUDIT') ||
    r.includes('CDSS') ||
    r.includes('HELPDESK') ||
    r.includes('CALLCENTER') ||
    r.includes('INVESTIGATOR') ||
    r.includes('BVI') ||
    r.includes('COLLECTION') ||
    r.includes('OVERPAYMENT') ||
    r.includes('STATEHEARING') ||
    r.includes('WARRANT') ||
    r.includes('PAYMENTCORRECTION') ||
    r.includes('ICT') ||
    r.includes('FORMSCORR') ||
    r.includes('HOMEVISIT') ||
    r.includes('CASENOTES') ||
    r.includes('PERSONNOTES') ||
    r.includes('QUALITYASSURANCE') ||
    r.includes('SPECTRAN') ||
    r.includes('CASEAPPROVAL') ||
    r.includes('DPPROCESS') ||
    r.includes('IHSSPAYROLL') ||
    r.includes('CMIPSCORE') ||
    r.includes('NORMALLOGIN') ||
    r.includes('CASELOAD') ||
    r.includes('COUNTY') ||
    r.includes('PUBLICAUTHORITY') ||
    r.includes('PABENEFITS') ||
    r.includes('PAPROVIDER') ||
    r.includes('PROGRAMMGMT') ||
    r.includes('CIROLE') ||
    r.includes('COMBINEDROLE') ||
    r.includes('WEBSERVICESROLE') ||
    r.includes('WPCSROLE') ||
    r.includes('TPF') ||
    r.includes('HP') && !r.includes('PROVIDER') ||
    r === 'CASE_WORKER'
  ) {
    return 'CASE_WORKER';
  }

  // Any other role (e.g. NEWUSERROLE, BASESECURITYROLE) - treat as staff
  if (r.endsWith('ROLE') || r.endsWith('GROUP')) {
    return 'CASE_WORKER';
  }

  return null;
}

/**
 * Collect all roles from JWT payload (realm_access.roles + resource_access client roles)
 */
export function collectRolesFromJwt(decoded: Record<string, unknown>): string[] {
  const roles: string[] = [];
  const seen = new Set<string>();

  // Realm roles
  const realmRoles = decoded.realm_access && typeof decoded.realm_access === 'object'
    ? (decoded.realm_access as { roles?: string[] }).roles
    : [];
  if (Array.isArray(realmRoles)) {
    realmRoles.forEach((r) => {
      if (typeof r === 'string' && !seen.has(r.toUpperCase())) {
        seen.add(r.toUpperCase());
        roles.push(r);
      }
    });
  }

  // Client roles (resource_access)
  const resourceAccess = decoded.resource_access;
  if (resourceAccess && typeof resourceAccess === 'object') {
    Object.values(resourceAccess as Record<string, { roles?: string[] }>).forEach((client) => {
      const clientRoles = client?.roles;
      if (Array.isArray(clientRoles)) {
        clientRoles.forEach((r) => {
          if (typeof r === 'string' && !seen.has(r.toUpperCase())) {
            seen.add(r.toUpperCase());
            roles.push(r);
          }
        });
      }
    });
  }

  return roles;
}

/**
 * Resolve the dashboard type from a list of Keycloak roles.
 * Returns the highest-priority dashboard the user qualifies for.
 */
export function getDashboardForRoles(roles: string[]): DashboardType {
  let result: DashboardType = 'USER';
  let maxPriority = PRIORITY.USER;

  for (const role of roles) {
    if (isIgnored(role)) continue;
    const dashboard = mapRoleToDashboard(role);
    if (dashboard && PRIORITY[dashboard] > maxPriority) {
      result = dashboard;
      maxPriority = PRIORITY[dashboard];
    }
  }

  return result;
}

/**
 * Main dashboard for ALL users. Everyone lands here after login.
 * Navigation tabs and shortcuts are filtered by role within this screen.
 */
export const MAIN_DASHBOARD_URL = '/my-workspace';

/** Dashboard URL for each type - all authenticated users go to main dashboard */
export const DASHBOARD_URLS: Record<DashboardType, string> = {
  ADMIN: MAIN_DASHBOARD_URL,
  SUPERVISOR: MAIN_DASHBOARD_URL,
  CASE_WORKER: MAIN_DASHBOARD_URL,
  PROVIDER: MAIN_DASHBOARD_URL,
  RECIPIENT: MAIN_DASHBOARD_URL,
  USER: '/login',
};

/** Tab config for workspace - optional role filter (empty = visible to all) */
export interface NavTabConfig {
  id: string;
  label: string;
  href: string;
  paths: string[];
  /** If specified, user needs at least one of these roles. Empty = visible to all. */
  requiredRoles?: string[];
}

/** Shortcut config - optional role filter (empty = visible to all) */
export interface ShortcutConfig {
  id: string;
  label: string;
  icon: string;
  href: string;
  /** If specified, user needs at least one of these roles. Empty = visible to all. */
  requiredRoles?: string[];
}

/** Workspace tabs - shown based on user roles */
export const WORKSPACE_TABS_CONFIG: NavTabConfig[] = [
  { id: 'my-workspace', label: 'My Workspace', href: '/my-workspace', paths: ['/', '/my-workspace', '/workspace'] },
  { id: 'my-cases', label: 'My Cases', href: '/my-cases', paths: ['/my-cases', '/cases'] },
  { id: 'inbox', label: 'Inbox', href: '/inbox', paths: ['/inbox'] },
  { id: 'calendar', label: 'Calendar', href: '#', paths: [] },
  { id: 'reports', label: 'Reports', href: '/analytics', paths: ['/analytics'] },
];

/** Sidebar/main shortcuts - shown based on user roles */
export const SHORTCUTS_CONFIG: ShortcutConfig[] = [
  { id: 'new-referral', label: 'New Referral', icon: '🏠', href: '/recipients/new' },
  { id: 'new-application', label: 'New Application', icon: '📋', href: '/new-application' },
  { id: 'find-person', label: 'Find a Person', icon: '👤', href: '/recipients' },
  { id: 'find-hearing-case', label: 'Find a State Hearing Case', icon: '⚖️', href: '#' },
];

/** Check if user can see a nav item (tab or shortcut). Empty requiredRoles = visible to all. */
export function isNavItemVisible(
  requiredRoles: string[] | undefined,
  userRoles: string[]
): boolean {
  if (!requiredRoles || requiredRoles.length === 0) return true;
  const userSet = new Set(userRoles.map((r) => (r || '').toUpperCase()));
  return requiredRoles.some((r) => userSet.has((r || '').toUpperCase()));
}

/** Filter tabs by user roles */
export function getVisibleTabs(userRoles: string[]): NavTabConfig[] {
  return WORKSPACE_TABS_CONFIG.filter((tab) => isNavItemVisible(tab.requiredRoles, userRoles));
}

/** Filter shortcuts by user roles */
export function getVisibleShortcuts(userRoles: string[]): ShortcutConfig[] {
  return SHORTCUTS_CONFIG.filter((s) => isNavItemVisible(s.requiredRoles, userRoles));
}

/** Check if user with given role(s) can access a specific dashboard */
export function canAccessDashboard(
  userRoles: string[],
  dashboard: DashboardType
): boolean {
  const resolved = getDashboardForRoles(userRoles);
  if (dashboard === 'CASE_WORKER') {
    return resolved === 'CASE_WORKER' || resolved === 'SUPERVISOR' || resolved === 'ADMIN';
  }
  if (dashboard === 'SUPERVISOR') {
    return resolved === 'SUPERVISOR' || resolved === 'ADMIN';
  }
  if (dashboard === 'ADMIN') return resolved === 'ADMIN';
  if (dashboard === 'PROVIDER') return resolved === 'PROVIDER';
  if (dashboard === 'RECIPIENT') return resolved === 'RECIPIENT';
  return false;
}
