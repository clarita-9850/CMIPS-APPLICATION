/**
 * Navigation map - maps pageIds (from UIM specs) and linkIds (from SideNav) to routes
 */
import { resolvePageId } from '../routes/pageIdToRoute';

export const TAB_TO_ROUTE = {
  'My Workspace': '/workspace',
  'My Cases': '/cases',
  Inbox: '/inbox',
  Calendar: '/calendar',
  Reports: '/analytics'
};

export const ROUTE_TO_TAB = Object.fromEntries(
  Object.entries(TAB_TO_ROUTE).map(([k, v]) => [v, k])
);

/** Side nav link ID -> route */
export const SIDE_NAV_ROUTES = {
  'new-referral': '/persons/search/referral',
  'new-application': '/persons/search/application',
  'message-center': '/inbox',
  'inbox': '/inbox',
  'assigned-tasks': '/tasks/assigned',
  'reserved-tasks': '/tasks/reserved',
  'deferred-tasks': '/tasks/deferred',
  'task-search': '/tasks/search',
  'work-queues': '/work-queues',
  'person-search': '/recipients',
  'case-search': '/cases',
  'state-hearing-search': '/cases/state-hearing',
  'provider-search': '/providers',
  'new-case': '/cases/new',
  'register-provider': '/providers/register',
  'public-authority-wage-rate': '/wage-rate',
  'county-contractor-rate': '/wage-rate',
  'county-contractor-home': '/providers',
  'county-contractor-search': '/providers',
  'homemaker-home': '/providers',
  'homemaker-search': '/providers',
  'org-home': '/admin',
  'child-org-units': '/admin',
  'positions': '/admin/users',
  'users': '/admin/users',
  'user-home': '/admin/users',
  // Help desk search links
  'helpdesk-person-search': '/help-desk/search-person',
  'helpdesk-timesheet-search': '/help-desk/search-timesheet',
  'helpdesk-payment-search': '/help-desk/search-person-payments',
  // Case domain links
  'case-contacts': '/case/list-contacts',
  'case-service-plans': '/case/list-service-plan',
  'case-assessments': '/case/resolve-case-home-from-task',
  'case-authorizations': '/case/authorization-list-decision',
  'case-notes': '/case/list-case-note',
  'case-forms': '/case/list-forms-correspondence',
  // Evidence domain links
  'evidence-home': '/evidence/home',
  'evidence-household': '/evidence/household-evidence-home',
  'evidence-companion': '/evidence/list-companion-cases',
  'evidence-income': '/evidence/resolve-household-home-page-evidence-id',
  'evidence-soc': '/evidence/resolve-soc-home-page-evidence-id',
  // Person domain links
  'person-forms': '/person/list-electronic-forms',
  'person-addresses': '/person/maintain-general-address-from-view',
  'person-cp-claims': '/person/search-cp-claims',
  'person-merge': '/recipients/merge',
  // Services links
  'evv': '/evv',
  'waivers': '/waivers',
  'analytics': '/analytics',
  'bi-reports': '/bi/reports',
  // Admin sub-routes
  'admin-work-queues': '/admin/work-queues',
  'data-pipeline': '/admin/data-pipeline',
  // Supervisor links
  'supervisor-dashboard': '/supervisor',
  'approval-queue': '/supervisor/approvals',
  'supervisor-tasks': '/supervisor/tasks',
  // Payment links
  'timesheets': '/payments/timesheets',
  'warrant-replacements': '/payments/warrant-replacements',
  'bvi-timesheet': '/payments/bvi-timesheet',
  'travel-claim': '/payments/travel-claim',
  'direct-deposit': '/payments/direct-deposit',
  'sick-leave': '/payments/sick-leave',
};

/**
 * UIM pageId -> route (supports params like :taskID, :id).
 * Hand-coded overrides take priority (for routes that differ from the generated mapping),
 * then falls back to the auto-generated PAGE_ID_TO_ROUTE for all 1,289 pageIds.
 */
export function getRouteForPageId(pageId, params = {}) {
  // Hand-coded overrides for routes that map to hand-crafted pages
  const overrides = {
    'TaskManagement_taskHome': '/tasks/' + (params.taskID || params.id || ''),
    'Case_createCase': '/cases/new',
    'Case_viewCase': '/cases/' + (params.caseID || params.id || ''),
    'Person_viewPerson': '/recipients/' + (params.personID || params.id || ''),
    'Person_editPerson': '/recipients/' + (params.personID || params.id || '') + '/edit',
    'Provider_viewProvider': '/providers/' + (params.providerID || params.id || ''),
    'Provider_registerProvider': '/providers/register',
    'Person_duplicateReferralCheck': '/recipients/new',
    'Person_duplicateRegistrationCheck': '/applications/new',
    'Person_search': '/recipients',
    'Case_searchStateHearing': '/cases/state-hearing',
    'Person_duplicateProviderCheck': '/providers/register',
    'Person_mergeDuplicateSSN': '/recipients/merge',
    'Payment_listWarrantReplacements': '/payments/warrant-replacements',
    'Case_reissueBVITimesheet': '/payments/bvi-timesheet',
    'Case_enterTravelClaimNum': '/payments/travel-claim',
    'Person_liveInProvSelfCertificationSearch': '/providers/live-in',
    'BackOffice_DirectDepositBatches': '/payments/direct-deposit',
    'Person_enterSickLeaveClaimManualEntry': '/payments/sick-leave',
  };

  if (overrides[pageId]) {
    let route = overrides[pageId];
    Object.keys(params).forEach(k => {
      route = route.replace(':' + k, params[k] || '');
    });
    return route;
  }

  // Fall back to the complete auto-generated mapping (1,289 pageIds)
  return resolvePageId(pageId, params);
}
