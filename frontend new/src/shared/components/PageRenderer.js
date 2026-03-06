/**
 * PageRenderer
 *
 * Spec-driven page renderer that can display any UIM page based on its pageId.
 * Looks up the page specification from the route metadata and renders it
 * using UimPageLayout + UimSection + UimField/UimTable components.
 *
 * Usage:
 *   <PageRenderer pageId="Case_viewCase" />
 *   -- or via route --
 *   <Route path="/p/:pageId" element={<PageRenderer />} />
 */

import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { UimPageLayout } from './UimPageLayout';
import { UimSection } from './UimSection';
import { UimTable } from './UimTable';
import { UimField } from './UimField';
import { useDomainData } from '../hooks/useDomainData';
import { PAGE_ID_TO_ROUTE } from '../../routes/pageIdToRoute';

/**
 * Sanitize a label key into human-readable text.
 */
function sanitizeLabel(str) {
  if (!str) return '';
  const stripped = str.replace(/^.*\./, '');
  return stripped.replace(/\$/g, ' ').replace(/([A-Z])/g, ' $1').trim();
}

/**
 * Infer the domain from a pageId prefix (e.g. "Case_viewCase" -> "case").
 */
function inferDomain(pageId) {
  if (!pageId) return 'misc';
  const prefix = pageId.split('_')[0].toLowerCase();
  const domainMap = {
    case: 'case',
    caseauthorization: 'case',
    medical: 'case',
    medcal: 'case',
    medicare: 'case',
    person: 'person',
    participant: 'person',
    recipient: 'person',
    provider: 'provider',
    evidence: 'evidence',
    payment: 'payment',
    supervisor: 'supervisor',
    workallocation: 'task-management',
    inbox: 'task-management',
    task: 'task-management',
    organization: 'organization',
    county: 'county',
    regionaladmin: 'county',
    recipworkweekagreement: 'homemaker',
    workweekagreement: 'homemaker',
    helpdesk: 'help-desk',
    system: 'back-office',
    backoffice: 'back-office',
  };
  return domainMap[prefix] || 'misc';
}

export function PageRenderer({ pageId: propPageId }) {
  const params = useParams();
  const navigate = useNavigate();
  const pageId = propPageId || params.pageId || '';
  const domain = inferDomain(pageId);

  // Fetch data from the domain API
  const { data, loading, error } = useDomainData(domain, 'list');
  const record = Array.isArray(data) ? data[0] : data;

  // Check if we have a dedicated route for this pageId
  const dedicatedRoute = PAGE_ID_TO_ROUTE[pageId];

  // Build nav links from the route map (find related pages in same domain)
  const navLinks = [];
  if (dedicatedRoute && dedicatedRoute !== `/p/${pageId}`) {
    // There's a real generated page — link to it
    navLinks.push({ label: 'Go to Full Page', route: dedicatedRoute });
  }

  const title = sanitizeLabel(pageId.split('_').slice(1).join(' ')) || pageId;

  return (
    <UimPageLayout
      pageId={pageId}
      title={title}
      navLinks={navLinks}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && (
        <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>
          Unable to load data. The backend may be unavailable.
        </div>
      )}

      <UimSection title="Details">
        <div className="uim-form-grid">
          {record && typeof record === 'object' ? (
            Object.entries(record).slice(0, 12).map(([key, value]) => (
              <UimField key={key} label={sanitizeLabel(key)} value={String(value ?? '')} />
            ))
          ) : (
            <>
              <UimField label="Page ID" value={pageId} />
              <UimField label="Domain" value={domain} />
              <UimField label="Status" value="Stub" />
            </>
          )}
        </div>
      </UimSection>

      {Array.isArray(data) && data.length > 0 && (
        <UimSection title="Records">
          <UimTable
            columns={Object.keys(data[0]).slice(0, 6)}
            rows={data}
            onRowClick={() => {}}
          />
        </UimSection>
      )}

      <div className="uim-action-bar">
        <button
          className="uim-btn uim-btn-secondary"
          onClick={() => navigate(-1)}
        >
          Back
        </button>
      </div>
    </UimPageLayout>
  );
}

export default PageRenderer;
