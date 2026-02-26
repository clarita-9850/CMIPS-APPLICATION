import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth, ProtectedRoute } from './auth/AuthContext';
import { RequireAuth } from './auth/RequireAuth';
import { PermissionRoute } from './auth/PermissionRoute';
import { BreadcrumbProvider } from './lib/BreadcrumbContext';

// Auto-generated UIM stub routes (1,265 pages via React.lazy for code-splitting)
import { allGeneratedRoutes } from './routes/allGeneratedRoutes';

// Components
import { LoginPageExample } from './components/LoginPage';
import { AppShell } from './layout/AppShell';
import { WorkspaceContent } from './pages/WorkspaceContent';
import { CasesPage } from './pages/CasesPage';
import { InboxPage } from './pages/InboxPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { RecipientsPage } from './pages/RecipientsPage';
import { RecipientsNewPage } from './pages/RecipientsNewPage';
import { ApplicationsNewPage } from './pages/ApplicationsNewPage';
import { ApplicationDetailPage } from './pages/ApplicationDetailPage';
import { ProvidersPage } from './pages/ProvidersPage';
import { ProviderDetailPage } from './pages/ProviderDetailPage';
import { ProviderRegisterPage } from './pages/ProviderRegisterPage';
import { CaseDetailPage } from './pages/CaseDetailPage';
import { CaseCreatePage } from './pages/CaseCreatePage';
import { RecipientDetailPage } from './pages/RecipientDetailPage';
import { RecipientEditPage } from './pages/RecipientEditPage';
import { TaskDetailPage } from './pages/TaskDetailPage';
import { PageRenderer } from './shared/components/PageRenderer';

// UIM-converted pages
import { PersonDuplicateReferralCheckPage } from './pages/uim/PersonDuplicateReferralCheckPage';
import { PersonDuplicateRegistrationCheckPage } from './pages/uim/PersonDuplicateRegistrationCheckPage';
import { PersonDuplicateProviderCheckPage } from './pages/uim/PersonDuplicateProviderCheckPage';
import { PersonEnterSickLeaveClaimManualEntryPage } from './pages/uim/PersonEnterSickLeaveClaimManualEntryPage';
import { PersonLiveInProvSelfCertificationSearchPage } from './pages/uim/PersonLiveInProvSelfCertificationSearchPage';
import { PersonSearchPage } from './pages/uim/PersonSearchPage';
import { PersonSearchReferralPage } from './pages/PersonSearchReferralPage';
import { PersonSearchApplicationPage } from './pages/PersonSearchApplicationPage';
import { PersonCreateReferralPage } from './pages/PersonCreateReferralPage';
import { CaseSearchStateHearingPage } from './pages/uim/CaseSearchStateHearingPage';
import { PersonMergeDuplicateSsnPage } from './pages/uim/PersonMergeDuplicateSsnPage';
import { PaymentListWarrantReplacementsPage } from './pages/uim/PaymentListWarrantReplacementsPage';
import { CaseReissueBviTimesheetPage } from './pages/uim/CaseReissueBviTimesheetPage';

// Work Queues & Task Management
import { WorkQueuesPage } from './pages/WorkQueuesPage';
import { WorkQueueDetailPage } from './pages/WorkQueueDetailPage';
import { WorkQueueTasksPage } from './pages/WorkQueueTasksPage';
import { WorkQueueAdminPage } from './pages/WorkQueueAdminPage';
import { AssignedTasksPage } from './pages/AssignedTasksPage';
import { ReservedTasksPage } from './pages/ReservedTasksPage';
import { DeferredTasksPage } from './pages/DeferredTasksPage';
import { TaskSearchPage } from './pages/TaskSearchPage';
import { TasksBeforeDeadlinePage } from './pages/TasksBeforeDeadlinePage';
import { SupervisorTasksPage } from './pages/SupervisorTasksPage';
import { PersonMergePage } from './pages/PersonMergePage';
import { SupervisorDashboardPage } from './pages/SupervisorDashboardPage';
import { ApprovalQueuePage } from './pages/ApprovalQueuePage';

// Payment
import { TimesheetsPage } from './pages/TimesheetsPage';
import { TimesheetDetailPage } from './pages/TimesheetDetailPage';
import { TimesheetCreatePage } from './pages/TimesheetCreatePage';
import { WarrantReplacementsPage } from './pages/WarrantReplacementsPage';
import { BVITimesheetReissuePage } from './pages/BVITimesheetReissuePage';
import { TravelClaimPage } from './pages/TravelClaimPage';
import { DirectDepositPage } from './pages/DirectDepositPage';
import { SickLeavePage } from './pages/SickLeavePage';

// Organization / Admin
import { OrganizationPage } from './pages/OrganizationPage';
import { UsersPage } from './pages/UsersPage';
import { UserDetailPage } from './pages/UserDetailPage';

// Feature 9: EVV, Waivers, BI, Pipeline
import { EVVPage } from './pages/EVVPage';
import { WaiversPage } from './pages/WaiversPage';
import { WaiverDetailPage } from './pages/WaiverDetailPage';
import { BIReportsPage } from './pages/BIReportsPage';
import { DataPipelinePage } from './pages/DataPipelinePage';

// Utility pages
import { StateHearingSearchPage } from './pages/StateHearingSearchPage';
import { LiveInProviderPage } from './pages/LiveInProviderPage';
import { CalendarPage } from './pages/CalendarPage';
import { PreferencesPage } from './pages/PreferencesPage';
import { WageRatePage } from './pages/WageRatePage';

/**
 * Logged Out Page
 */
const LoggedOutPage = () => (
  <div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    flexDirection: 'column',
    gap: '1.5rem',
    backgroundColor: '#f8f9fa'
  }}>
    <div style={{
      backgroundColor: 'white',
      padding: '3rem',
      borderRadius: '8px',
      boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
      textAlign: 'center',
      maxWidth: '500px'
    }}>
      <h2 style={{ color: '#153554', marginBottom: '1rem' }}>
        You have been logged out
      </h2>
      <p style={{ color: '#666', marginBottom: '2rem' }}>
        Thank you for using CMIPS. You have been successfully logged out.
      </p>
      <button
        onClick={() => window.location.href = '/'}
        style={{
          backgroundColor: '#153554',
          color: 'white',
          padding: '0.75rem 2rem',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '1rem',
          fontWeight: 500
        }}
      >
        Return to Home
      </button>
    </div>
  </div>
);

function App() {
  const { authenticated, keycloak } = useAuth();

  const isKeycloakEnabled = !!keycloak;

  // Auth state comes entirely from AuthContext (which reads localStorage + events).
  const isAuthenticated = authenticated || localStorage.getItem('isAuthenticated') === 'true';

  const handleLoginSuccess = () => {
    // LoginPageExample already stores token/user in localStorage and dispatches event.
    // AuthContext picks up the event and sets authenticated=true.
  };

  return (
    <BreadcrumbProvider>
      <BrowserRouter>
        <Routes>
          {/* Login Route - For development/mock auth only */}
          {!isKeycloakEnabled && (
            <Route
              path="/login"
              element={
                isAuthenticated ?
                  <Navigate to="/workspace" replace /> :
                  <LoginPageExample onLoginSuccess={handleLoginSuccess} />
              }
            />
          )}

          {/* Logged Out Page */}
          <Route path="/logged-out" element={<LoggedOutPage />} />

          {/* Protected Routes - Wrapped in RequireAuth and AppShell */}
          <Route
            element={
              <RequireAuth>
                <AppShell />
              </RequireAuth>
            }
          >
            {/* Workspace route */}
            <Route path="/workspace" element={<WorkspaceContent />} />
            <Route path="/cases" element={<CasesPage />} />
            <Route path="/cases/new" element={<CaseCreatePage />} />
            <Route path="/cases/state-hearing" element={<StateHearingSearchPage />} />
            <Route path="/cases/:id" element={<CaseDetailPage />} />
            <Route path="/inbox" element={<InboxPage />} />
            <Route path="/analytics" element={<AnalyticsPage />} />
            <Route path="/recipients" element={<RecipientsPage />} />
            <Route path="/recipients/new" element={<RecipientsNewPage />} />
            <Route path="/recipients/merge" element={<PersonMergePage />} />
            <Route path="/recipients/:id/edit" element={<RecipientEditPage />} />
            <Route path="/recipients/:id" element={<RecipientDetailPage />} />
            <Route path="/applications/new" element={<ApplicationsNewPage />} />
            <Route path="/applications/:id" element={<ApplicationDetailPage />} />
            {/* Initial Contact — Person Search + Create Referral (CI-67784, CI-67788) */}
            <Route path="/persons/search/referral" element={<PersonSearchReferralPage />} />
            <Route path="/persons/search/application" element={<PersonSearchApplicationPage />} />
            <Route path="/persons/referral/new" element={<PersonCreateReferralPage />} />
            <Route path="/providers" element={<ProvidersPage />} />
            <Route path="/providers/register" element={<ProviderRegisterPage />} />
            <Route path="/providers/live-in" element={<LiveInProviderPage />} />
            <Route path="/providers/:id" element={<ProviderDetailPage />} />

            {/* UIM Routes */}
            <Route path="/person/duplicate-referral-check" element={<PersonDuplicateReferralCheckPage />} />
            <Route path="/person/duplicate-registration-check" element={<PersonDuplicateRegistrationCheckPage />} />
            <Route path="/person/duplicate-provider-check" element={<PersonDuplicateProviderCheckPage />} />
            <Route path="/person/enter-sick-leave-claim-manual-entry" element={<PersonEnterSickLeaveClaimManualEntryPage />} />
            <Route path="/person/live-in-prov-self-certification-search" element={<PersonLiveInProvSelfCertificationSearchPage />} />
            <Route path="/person/search" element={<PersonSearchPage />} />
            <Route path="/person/merge-duplicate-ssn" element={<PersonMergeDuplicateSsnPage />} />
            <Route path="/case/search-state-hearing" element={<CaseSearchStateHearingPage />} />
            <Route path="/case/reissue-bvi-timesheet" element={<CaseReissueBviTimesheetPage />} />
            <Route path="/payment/list-warrant-replacements" element={<PaymentListWarrantReplacementsPage />} />

            {/* Payment Routes */}
            <Route path="/payments/timesheets" element={<TimesheetsPage />} />
            <Route path="/payments/timesheets/new" element={<TimesheetCreatePage />} />
            <Route path="/payments/timesheets/:id" element={<TimesheetDetailPage />} />
            <Route path="/payments/warrant-replacements" element={<WarrantReplacementsPage />} />
            <Route path="/payments/bvi-timesheet" element={<BVITimesheetReissuePage />} />
            <Route path="/payments/travel-claim" element={<TravelClaimPage />} />
            <Route path="/payments/direct-deposit" element={<DirectDepositPage />} />
            <Route path="/payments/sick-leave" element={<SickLeavePage />} />

            {/* Work Queues */}
            <Route path="/work-queues" element={<WorkQueuesPage />} />
            <Route path="/work-queues/:queueId" element={<WorkQueueDetailPage />} />
            <Route path="/work-queues/:queueId/tasks" element={<WorkQueueTasksPage />} />

            {/* Task Management */}
            <Route path="/tasks/assigned" element={<AssignedTasksPage />} />
            <Route path="/tasks/reserved" element={<ReservedTasksPage />} />
            <Route path="/tasks/deferred" element={<DeferredTasksPage />} />
            <Route path="/tasks/search" element={<TaskSearchPage />} />
            <Route path="/tasks/before-deadline" element={<TasksBeforeDeadlinePage />} />
            <Route path="/tasks/:id" element={<TaskDetailPage />} />

            {/* Supervisor Routes — guarded by supervisor roles */}
            <Route path="/supervisor" element={
              <ProtectedRoute roles={['CASEMANAGEMENTSUPERVISORROLE', 'SUPERVISOR', 'INTAKESUPERVISORROLE', 'ELIGIBILITYSUPERVISORROLE']}>
                <SupervisorDashboardPage />
              </ProtectedRoute>
            } />
            <Route path="/supervisor/approvals" element={
              <ProtectedRoute roles={['CASEMANAGEMENTSUPERVISORROLE', 'SUPERVISOR', 'INTAKESUPERVISORROLE', 'ELIGIBILITYSUPERVISORROLE']}>
                <ApprovalQueuePage />
              </ProtectedRoute>
            } />
            <Route path="/supervisor/tasks" element={
              <ProtectedRoute roles={['CASEMANAGEMENTSUPERVISORROLE', 'SUPERVISOR', 'INTAKESUPERVISORROLE', 'ELIGIBILITYSUPERVISORROLE']}>
                <SupervisorTasksPage />
              </ProtectedRoute>
            } />

            {/* Admin Routes — guarded by security admin roles */}
            <Route path="/admin" element={
              <ProtectedRoute roles={['ADMIN']}>
                <OrganizationPage />
              </ProtectedRoute>
            } />
            <Route path="/admin/users" element={
              <ProtectedRoute roles={['ADMIN']}>
                <UsersPage />
              </ProtectedRoute>
            } />
            <Route path="/admin/users/:id" element={
              <ProtectedRoute roles={['ADMIN']}>
                <UserDetailPage />
              </ProtectedRoute>
            } />
            <Route path="/admin/work-queues" element={
              <ProtectedRoute roles={['ADMIN']}>
                <WorkQueueAdminPage />
              </ProtectedRoute>
            } />

            {/* EVV */}
            <Route path="/evv" element={<EVVPage />} />

            {/* Waivers */}
            <Route path="/waivers" element={<WaiversPage />} />
            <Route path="/waivers/:id" element={<WaiverDetailPage />} />

            {/* BI Reports */}
            <Route path="/bi/reports" element={<BIReportsPage />} />

            {/* Data Pipeline (admin) */}
            <Route path="/admin/data-pipeline" element={
              <ProtectedRoute roles={['ADMIN']}>
                <DataPipelinePage />
              </ProtectedRoute>
            } />

            {/* General */}
            <Route path="/calendar" element={<CalendarPage />} />
            <Route path="/preferences" element={<PreferencesPage />} />
            <Route path="/wage-rate" element={<WageRatePage />} />

            {/* Auto-generated UIM stub pages (1,265 lazy-loaded routes) */}
            {/* Each route is permission-checked based on its domain path prefix */}
            {allGeneratedRoutes.map(({ path: p, Component }) => (
              <Route
                key={p}
                path={p}
                element={
                  <PermissionRoute>
                    <Suspense fallback={
                      <div style={{ padding: '2rem', color: '#153554' }}>Loading page...</div>
                    }>
                      <Component />
                    </Suspense>
                  </PermissionRoute>
                }
              />
            ))}

            <Route path="/p/:pageId" element={<PageRenderer />} />
          </Route>

          {/* Default redirect */}
          <Route
            path="/"
            element={
              isAuthenticated ?
                <Navigate to="/workspace" replace /> :
                (isKeycloakEnabled ?
                  <Navigate to="/workspace" replace /> :
                  <Navigate to="/login" replace />)
            }
          />

          {/* Catch-all redirect */}
          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Routes>
      </BrowserRouter>
    </BreadcrumbProvider>
  );
}

export default App;
