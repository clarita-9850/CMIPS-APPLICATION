#!/usr/bin/env node
/**
 * UIM Page Generator for frontend new — Reference-Aligned Edition
 * ================================================================
 * For each stub page in uim-graph.json:
 *   1. Reads the corresponding reference file from CMIPS3.0-main/frontend/src
 *   2. Parses its exact structure (title, sections, fields, nav links, actions)
 *   3. Re-emits an enhanced version with:
 *      - Exact same titles, sections, fields, and nav links as the reference
 *      - Humanized titles for placeholder references (DUMMY, raw pageId)
 *      - useDomainData hook for backend API integration
 *      - Data binding for UimField values
 *      - Wired action buttons (Cancel→navigate, Save→domainApi.create, etc.)
 *      - hidePlaceholderBanner=true only for real references
 *      - Loading/error state banners
 *
 * Fallback: if no reference file exists, generates from uim-graph.json metadata.
 *
 * Usage:  node scripts/generate-pages.js [--force]
 */

'use strict';
const fs   = require('fs');
const path = require('path');

const ROOT        = path.resolve(__dirname, '..');
const GRAPH_FILE  = path.resolve(ROOT, '..', '..', 'CMIPS3.0-main', 'frontend', 'docs', 'uim-graph.json');
const REF_SRC     = path.resolve(ROOT, '..', '..', 'CMIPS3.0-main', 'frontend', 'src');
const SRC_ROOT    = path.join(ROOT, 'src');
const FORCE       = process.argv.includes('--force');

if (!fs.existsSync(GRAPH_FILE)) {
  console.error(`[Error] uim-graph.json not found at: ${GRAPH_FILE}`);
  process.exit(1);
}

const graph = JSON.parse(fs.readFileSync(GRAPH_FILE, 'utf8'));

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function escapeJsx(s) {
  return String(s).replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\n/g, '\\n');
}

function escapeJsStr(s) {
  return String(s).replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n/g, '\\n');
}

function sanitizeLabel(str) {
  if (!str) return '';
  const stripped = str.replace(/^.*\./, '');
  return stripped.replace(/\$/g, ' ').replace(/([A-Z])/g, ' $1').trim();
}

function labelToDataKey(label) {
  const stripped = label.replace(/^.*\./, '');
  const clean = stripped.replace(/[^a-zA-Z0-9]/g, ' ').trim();
  const words = clean.split(/\s+/);
  return words.map((w, i) => {
    if (i === 0) return w.charAt(0).toLowerCase() + w.slice(1);
    return w.charAt(0).toUpperCase() + w.slice(1);
  }).join('');
}

/**
 * Convert a raw pageId (e.g. "Organization_userSearch") into a human-readable
 * title (e.g. "User Search").
 */
function humanizePageId(pageId) {
  // Strip domain prefix: "Case_createCase" → "createCase"
  const afterUnderscore = pageId.includes('_') ? pageId.split('_').slice(1).join('_') : pageId;
  // Replace remaining underscores with spaces: "Job_Aid" → "Job Aid"
  const noUnderscores = afterUnderscore.replace(/_/g, ' ');
  // Insert spaces before uppercase letters: "createCase" → "create Case"
  const spaced = noUnderscores.replace(/([A-Z])/g, ' $1').trim();
  // Collapse multiple spaces and title-case each word
  return spaced.replace(/\s+/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

/**
 * Detect page type from pageId.
 * Returns 'list' | 'search' | 'create' | 'modify' | 'detail' | 'generic'
 */
function detectPageType(pageId) {
  const id = pageId.toLowerCase();
  if (id.includes('_list') || id.includes('list_') || id.startsWith('list')) return 'list';
  if (id.includes('_search') || id.includes('search_') || id.startsWith('search')) return 'search';
  if (id.includes('_create') || id.includes('create_') || id.startsWith('create')) return 'create';
  if (id.includes('_modify') || id.includes('modify_') || id.includes('_edit') || id.includes('edit_')) return 'modify';
  if (id.includes('_view') || id.includes('view_')) return 'detail';
  if (id.includes('_home') || id.includes('home_') || id.includes('Home')) return 'detail';
  return 'generic';
}

/**
 * Check if a parsed reference is a placeholder (DUMMY title, raw pageId title,
 * or only generic ID/Status/Date fields).
 */
function isPlaceholderReference(parsed, pageId) {
  // DUMMY title
  if (parsed.title && parsed.title.startsWith('DUMMY')) return true;
  // Title equals raw pageId (e.g. "Organization_userSearch")
  if (parsed.title && parsed.title === pageId) return true;
  // Only has a single "Details" section with generic ID/Status/Date fields
  if (parsed.sections.length === 1) {
    const s = parsed.sections[0];
    if (s.title === 'Details' && s.fields.length <= 3) {
      const generic = new Set(['ID', 'Status', 'Date']);
      const allGeneric = s.fields.every(f => generic.has(f));
      if (allGeneric) return true;
    }
  }
  return false;
}

// ---------------------------------------------------------------------------
// Domain API import names — used for wiring Save/Submit/Create buttons
// ---------------------------------------------------------------------------

const DOMAIN_API_IMPORTS = {
  'case':            'casesApi',
  'person':          'personsApi',
  'provider':        'providersApi',
  'evidence':        'eligibilityApi',
  'payment':         'timesheetsApi',
  'supervisor':      'supervisorApi',
  'task-management': 'tasksApi',
  'organization':    'organizationApi',
  'county':          'countyApi',
  'homemaker':       'homemakerApi',
  'help-desk':       'helpDeskApi',
  'back-office':     'backOfficeApi',
  'misc':            'miscApi',
};

// ---------------------------------------------------------------------------
// Reference file parser
// ---------------------------------------------------------------------------

/**
 * Parse a reference page file and extract its structure.
 * Returns { title, navLinks, sections, actions, hasTabs, tabLabels }
 */
function parseReferenceFile(content) {
  const result = {
    title: null,
    navLinks: [],
    sections: [],       // { title, fields[], hasList, listColumns[] }
    actions: [],        // action button labels
    hasTabs: false,
    tabLabels: [],
  };

  // Extract title from title={" ... "}
  const titleMatch = content.match(/title=\{"([^"]+)"\}/);
  if (titleMatch) result.title = titleMatch[1];

  // Extract NAV_LINKS
  const navRegex = /\{ label: '([^']*)', route: '([^']*)' \}/g;
  let navMatch;
  while ((navMatch = navRegex.exec(content)) !== null) {
    result.navLinks.push({ label: navMatch[1], route: navMatch[2] });
  }

  // Extract sections and their fields
  const sectionRegex = /<UimSection title=\{"([^"]+)"\}>/g;
  let sectionMatch;
  const sectionPositions = [];
  while ((sectionMatch = sectionRegex.exec(content)) !== null) {
    sectionPositions.push({
      title: sectionMatch[1],
      startIndex: sectionMatch.index,
    });
  }

  // Find the closing </UimSection> for each section to extract its content
  for (let i = 0; i < sectionPositions.length; i++) {
    const start = sectionPositions[i].startIndex;
    const nextStart = i + 1 < sectionPositions.length
      ? sectionPositions[i + 1].startIndex
      : content.length;
    const sectionContent = content.substring(start, nextStart);

    const fields = [];
    const fieldRegex = /<UimField label=\{"([^"]+)"\}/g;
    let fieldMatch;
    while ((fieldMatch = fieldRegex.exec(sectionContent)) !== null) {
      fields.push(fieldMatch[1]);
    }

    const hasList = sectionContent.includes('<UimTable');

    // Extract table columns if present
    const colMatch = sectionContent.match(/columns=\{\[([^\]]+)\]\}/);
    let listColumns = ['ID', 'Name', 'Status', 'Date'];
    if (colMatch) {
      listColumns = colMatch[1].split(',').map(c => c.trim().replace(/'/g, ''));
    }

    result.sections.push({
      title: sectionPositions[i].title,
      fields,
      hasList,
      listColumns,
    });
  }

  // Extract action button labels — match text between }}> and </button>
  const actionRegex = /\}\}>\s*([^<]+?)\s*<\/button>/g;
  let actionMatch;
  while ((actionMatch = actionRegex.exec(content)) !== null) {
    const label = actionMatch[1].trim();
    if (label) result.actions.push(label);
  }

  // Check for tabs
  if (content.includes('uim-tab-nav')) {
    result.hasTabs = true;
    const tabRegex = /setActiveTab\(\d+\)\}>([^<]+)<\/button>/g;
    let tabMatch;
    while ((tabMatch = tabRegex.exec(content)) !== null) {
      result.tabLabels.push(tabMatch[1]);
    }
  }

  return result;
}

// ---------------------------------------------------------------------------
// Code generation
// ---------------------------------------------------------------------------

/**
 * Map a common action label to wired onClick handler body.
 * Returns { code, needsNavigate, needsDomainApi, needsId }
 */
function wireAction(label, domain, navLinks, pageType) {
  const lower = label.toLowerCase().trim();
  const apiName = DOMAIN_API_IMPORTS[domain] || 'miscApi';

  // Helper: generate a domain API call with navigate-back on success
  function apiCall(method, args, verb) {
    return {
      code: `{ ${apiName}.${method}(${args}).then(() => { alert('${verb} successful'); navigate(-1); }).catch(err => alert('${verb} failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: method !== 'create',
    };
  }

  // Cancel / Back / Close / Return
  if (lower === 'cancel' || lower === 'back' || lower === 'close' || lower === 'return') {
    return { code: 'navigate(-1)', needsNavigate: true, needsDomainApi: false, needsId: false };
  }

  // Previous Page
  if (lower === 'previous page' || lower === '<<previous') {
    return { code: 'navigate(-1)', needsNavigate: true, needsDomainApi: false, needsId: false };
  }

  // Next Page / Continue
  if (lower === 'next page' || lower === 'next>>' || lower === 'continue') {
    return { code: `alert('Navigating to next step')`, needsNavigate: false, needsDomainApi: false, needsId: false };
  }

  // Reset / Clear
  if (lower === 'reset' || lower === 'clear') {
    return { code: 'window.location.reload()', needsNavigate: false, needsDomainApi: false, needsId: false };
  }

  // Search
  if (lower === 'search' || lower === 'find' || lower === 'lookup') {
    return {
      code: `{ ${apiName}.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }`,
      needsNavigate: false, needsDomainApi: true, needsId: false,
    };
  }

  // Delete / Remove
  if (lower === 'delete' || lower === 'remove') {
    return {
      code: `{ if (window.confirm('Are you sure you want to delete?')) { ${apiName}.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Save / Submit / Create / Add / Save and Close
  if (lower === 'save' || lower === 'submit' || lower === 'create' || lower === 'save and close' || lower === 'add') {
    if (pageType === 'create') {
      return apiCall('create', '{}', 'Save');
    }
    if (pageType === 'modify') {
      return {
        code: `{ ${apiName}.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }`,
        needsNavigate: true, needsDomainApi: true, needsId: true,
      };
    }
    return apiCall('create', '{}', 'Save');
  }

  // Edit / Modify — navigate to modify page
  if (lower === 'edit' || lower === 'modify') {
    return { code: `alert('Opening edit mode')`, needsNavigate: false, needsDomainApi: false, needsId: false };
  }

  // Approve
  if (lower === 'approve') {
    return {
      code: `{ ${apiName}.update(id, { status: 'approved' }).then(() => { alert('Approved successfully'); navigate(-1); }).catch(err => alert('Approve failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Deny / Reject
  if (lower === 'deny' || lower === 'reject') {
    return {
      code: `{ ${apiName}.update(id, { status: 'denied' }).then(() => { alert('Denied successfully'); navigate(-1); }).catch(err => alert('Action failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Reserve
  if (lower === 'reserve') {
    return {
      code: `{ ${apiName}.update(id, { action: 'reserve' }).then(() => { alert('Reserved successfully'); navigate(-1); }).catch(err => alert('Reserve failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Forward
  if (lower === 'forward') {
    return {
      code: `{ ${apiName}.update(id, { action: 'forward' }).then(() => { alert('Forwarded successfully'); navigate(-1); }).catch(err => alert('Forward failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Defer
  if (lower === 'defer') {
    return {
      code: `{ ${apiName}.update(id, { action: 'defer' }).then(() => { alert('Deferred successfully'); navigate(-1); }).catch(err => alert('Defer failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Assign
  if (lower === 'assign' || lower === 'reassign') {
    return {
      code: `{ ${apiName}.update(id, { action: 'assign' }).then(() => { alert('Assigned successfully'); navigate(-1); }).catch(err => alert('Assign failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Reallocate
  if (lower === 'reallocate') {
    return {
      code: `{ ${apiName}.update(id, { action: 'reallocate' }).then(() => { alert('Reallocated successfully'); navigate(-1); }).catch(err => alert('Reallocate failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Transfer
  if (lower === 'transfer') {
    return {
      code: `{ ${apiName}.update(id, { action: 'transfer' }).then(() => { alert('Transferred successfully'); navigate(-1); }).catch(err => alert('Transfer failed: ' + err.message)); }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Terminate / Inactivate / Deactivate
  if (lower === 'terminate' || lower === 'inactivate' || lower === 'deactivate') {
    return {
      code: `{ if (window.confirm('Are you sure?')) { ${apiName}.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }`,
      needsNavigate: true, needsDomainApi: true, needsId: true,
    };
  }

  // Print
  if (lower === 'print') {
    return { code: 'window.print()', needsNavigate: false, needsDomainApi: false, needsId: false };
  }

  // Refresh
  if (lower === 'refresh') {
    return { code: 'window.location.reload()', needsNavigate: false, needsDomainApi: false, needsId: false };
  }

  // Subscribe
  if (lower === 'subscribe') {
    return {
      code: `{ ${apiName}.update(id, { action: 'subscribe' }).then(() => alert('Subscribed')).catch(err => alert('Subscribe failed: ' + err.message)); }`,
      needsNavigate: false, needsDomainApi: true, needsId: true,
    };
  }

  // Validate / Verify
  if (lower === 'validate' || lower === 'verify') {
    return {
      code: `{ ${apiName}.update(id, { action: 'validate' }).then(() => alert('Validated successfully')).catch(err => alert('Validation failed: ' + err.message)); }`,
      needsNavigate: false, needsDomainApi: true, needsId: true,
    };
  }

  // OK / Confirm / Yes / Accept
  if (lower === 'ok' || lower === 'confirm' || lower === 'yes' || lower === 'accept') {
    return { code: 'navigate(-1)', needsNavigate: true, needsDomainApi: false, needsId: false };
  }

  // Navigation-like actions: look for matching nav link
  for (const nl of navLinks) {
    if (nl.label.toLowerCase().includes(lower) || lower.includes(nl.label.toLowerCase())) {
      return { code: `navigate('${escapeJsStr(nl.route)}')`, needsNavigate: true, needsDomainApi: false, needsId: false };
    }
  }

  // Default: alert with action name so the button is visibly functional
  return {
    code: `alert('Action: ${escapeJsStr(label)}')`,
    needsNavigate: false, needsDomainApi: false, needsId: false,
  };
}

/**
 * Generate the page file content.
 */
function generatePageContent({
  pageId, domain, component, pageType,
  title, navLinks, sections, actions, hasTabs, tabLabels,
  relToShared, relToHooks, relToApi, hasReference
}) {
  // Determine API operation
  let dataHook = '';
  let dataBindingSetup = '';

  if (pageType === 'create') {
    dataHook = '';
    dataBindingSetup = '';
  } else if (pageType === 'modify' || pageType === 'detail') {
    dataHook = `\n  const { data, loading, error } = useDomainData('${escapeJsStr(domain)}', 'list');`;
    dataBindingSetup = '  const record = Array.isArray(data) ? data[0] : data;';
  } else if (pageType === 'list' || pageType === 'search') {
    dataHook = `\n  const { data, loading, error } = useDomainData('${escapeJsStr(domain)}', 'list');`;
    dataBindingSetup = '  const record = Array.isArray(data) ? data[0] : data;';
  } else {
    dataHook = `\n  const { data, loading, error } = useDomainData('${escapeJsStr(domain)}', 'list');`;
    dataBindingSetup = '  const record = Array.isArray(data) ? data[0] : data;';
  }

  const hasDataHook = dataHook.length > 0;
  const tabState = hasTabs && tabLabels.length > 0
    ? '\n  const [activeTab, setActiveTab] = React.useState(0);'
    : '';

  // Check which wired features we need
  const wiredActions = actions.map(a => wireAction(a, domain, navLinks, pageType));
  const anyNeedsNavigate = wiredActions.some(w => w.needsNavigate);
  const anyNeedsDomainApi = wiredActions.some(w => w.needsDomainApi);
  const anyNeedsId = wiredActions.some(w => w.needsId);
  const apiName = DOMAIN_API_IMPORTS[domain] || 'miscApi';

  // Build imports
  const hasLists = sections.some(s => s.hasList);
  const hasFields = sections.some(s => s.fields.length > 0);

  const imports = [`import { UimPageLayout } from '${relToShared}';`];
  imports.push(`import { UimSection }    from '${relToShared}';`);
  if (hasLists)  imports.push(`import { UimTable }      from '${relToShared}';`);
  if (hasFields) imports.push(`import { UimField }      from '${relToShared}';`);
  if (hasDataHook) imports.push(`import { useDomainData } from '${relToHooks}/useDomainData';`);
  if (anyNeedsDomainApi) imports.push(`import { getDomainApi } from '${relToApi}/domainApi';`);

  // React Router imports
  const routerImports = [];
  if (anyNeedsNavigate) routerImports.push('useNavigate');
  if (anyNeedsId) routerImports.push('useParams');
  const navigateImport = routerImports.length > 0
    ? `\nimport { ${routerImports.join(', ')} } from 'react-router-dom';`
    : '';

  const navigateSetup = anyNeedsNavigate ? '\n  const navigate = useNavigate();' : '';
  const idSetup = anyNeedsId ? "\n  const { id } = useParams();" : '';
  const apiSetup = anyNeedsDomainApi
    ? `\n  const ${apiName} = getDomainApi('${escapeJsStr(domain)}');`
    : '';

  // Build NAV_LINKS
  const navLinksLiteral = navLinks.length > 0
    ? `[\n    ${navLinks.map(l => `{ label: '${escapeJsStr(l.label)}', route: '${escapeJsStr(l.route)}' }`).join(',\n    ')}\n  ]`
    : '[]';

  // Build loading/error banners
  const loadingBanner = hasDataHook
    ? `\n      {loading && <div className="uim-info-banner">Loading data...</div>}\n      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}`
    : '';

  // Build tab JSX
  const tabJsx = hasTabs && tabLabels.length > 0
    ? `\n      <div className="uim-tab-nav">\n        ${tabLabels.map((t, i) => `<button className={activeTab===${i} ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(${i})}>${t}</button>`).join('\n        ')}\n      </div>`
    : '';

  // Build sections JSX
  const sectionsJsx = sections.map(s => {
    let fieldJsx = '';
    if (s.fields.length > 0) {
      const fieldLines = s.fields.map(f => {
        const dataKey = labelToDataKey(f);
        if (hasDataHook && pageType !== 'create') {
          return `          <UimField label={"${escapeJsx(f)}"} value={record && record['${escapeJsStr(dataKey)}']} />`;
        }
        return `          <UimField label={"${escapeJsx(f)}"} />`;
      });
      fieldJsx = `\n        <div className="uim-form-grid">\n${fieldLines.join('\n')}\n        </div>`;
    }

    let listJsx = '';
    if (s.hasList) {
      const cols = s.listColumns.map(c => `'${escapeJsStr(c)}'`).join(', ');
      if (hasDataHook) {
        listJsx = `\n        <UimTable\n          columns={[${cols}]}\n          rows={Array.isArray(data) ? data : []}\n          onRowClick={() => {}}\n        />`;
      } else {
        listJsx = `\n        <UimTable\n          columns={[${cols}]}\n          rowCount={3}\n          onRowClick={() => {}}\n        />`;
      }
    }

    return `\n      <UimSection title={"${escapeJsx(s.title)}"}>${fieldJsx}${listJsx}\n      </UimSection>`;
  }).join('');

  // Build action bar
  let actionsJsx = '';
  if (actions.length > 0) {
    const dedupedActions = [];
    let prev = null;
    for (const a of actions) {
      if (a !== prev) {
        dedupedActions.push(a);
        prev = a;
      }
    }

    const actionButtons = dedupedActions.map(a => {
      const wired = wireAction(a, domain, navLinks, pageType);
      return `        <button className="uim-btn uim-btn-primary" onClick={() => ${wired.code}}>${a}</button>`;
    });
    actionsJsx = `\n      <div className="uim-action-bar">\n${actionButtons.join('\n')}\n      </div>`;
  }

  const bindingLine = dataBindingSetup ? `\n${dataBindingSetup}` : '';

  return `import React from 'react';${navigateImport}
${imports.join('\n')}

const NAV_LINKS = ${navLinksLiteral};

export function ${component}() {${navigateSetup}${idSetup}${apiSetup}${tabState}${dataHook}${bindingLine}
  return (
    <UimPageLayout
      pageId={"${escapeJsx(pageId)}"}
      title={"${escapeJsx(title || pageId)}"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={${hasReference}}
    >${loadingBanner}${tabJsx}${sectionsJsx}${actionsJsx}
    </UimPageLayout>
  );
}

export default ${component};
`;
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

let generated          = 0;
let skipped            = 0;
let errors             = 0;
let fromRef            = 0;
let fromRefReal        = 0;
let fromRefPlaceholder = 0;

const stubs = graph.nodes.filter(n => n.status === 'stub');
console.log(`[Generator] ${stubs.length} stub pages to process (force=${FORCE}) ...`);

for (const node of stubs) {
  const { pageId, domain, component } = node;
  if (!component) { skipped++; continue; }

  const outDir  = path.join(SRC_ROOT, 'features', domain, 'pages');
  const outFile = path.join(outDir, `${component}.js`);

  if (!FORCE && fs.existsSync(outFile)) { skipped++; continue; }
  fs.mkdirSync(outDir, { recursive: true });

  const relToShared = path.relative(outDir, path.join(SRC_ROOT, 'shared', 'components')).replace(/\\/g, '/');
  const relToHooks  = path.relative(outDir, path.join(SRC_ROOT, 'shared', 'hooks')).replace(/\\/g, '/');
  const relToApi    = path.relative(outDir, path.join(SRC_ROOT, 'api')).replace(/\\/g, '/');

  // Try to read the reference file
  const refFile = path.join(REF_SRC, 'features', domain, 'pages', `${component}.js`);
  let parsed = null;
  let hasReference = false;

  if (fs.existsSync(refFile)) {
    try {
      const refContent = fs.readFileSync(refFile, 'utf8');
      parsed = parseReferenceFile(refContent);
      fromRef++;

      // Check if it's a real reference or a placeholder
      if (isPlaceholderReference(parsed, pageId)) {
        hasReference = false;
        parsed.title = humanizePageId(pageId);
        fromRefPlaceholder++;
      } else {
        hasReference = true;
        fromRefReal++;
      }
    } catch (e) {
      // Fall through to metadata-based generation
    }
  }

  // Fallback: build from uim-graph.json metadata
  if (!parsed) {
    const { title, clusterTitles, listTitles, actionLabels, fieldLabels, tabTitles } = node;
    const clusters = clusterTitles && clusterTitles.length > 0 ? clusterTitles : ['Details'];
    const fields   = fieldLabels && fieldLabels.length > 0 ? fieldLabels : ['ID', 'Status', 'Date'];
    const lists    = listTitles && listTitles.length > 0 ? listTitles : [];
    const actions  = actionLabels && actionLabels.length > 0 ? actionLabels.map(a => sanitizeLabel(a)) : [];
    const tabs     = tabTitles && tabTitles.length > 0 ? tabTitles : [];

    // Build edge-derived nav links
    const routeByPageId = {};
    for (const n of graph.nodes) routeByPageId[n.pageId] = n.route;
    const outgoing = {};
    if (graph.edges) {
      for (const e of graph.edges) {
        if (!outgoing[e.source]) outgoing[e.source] = new Set();
        outgoing[e.source].add(e.target);
      }
    }
    const navLinks = [...(outgoing[pageId] || [])].map(targetId => ({
      label: sanitizeLabel(targetId.split('_').slice(1).join(' ') || targetId) || targetId,
      route: routeByPageId[targetId] || '/workspace',
    })).filter(l => l.route !== node.route);

    // Distribute fields across clusters
    const fieldsPerCluster = Math.max(1, Math.ceil(fields.length / clusters.length));
    const sections = clusters.map((ct, ci) => ({
      title: sanitizeLabel(ct),
      fields: fields.slice(ci * fieldsPerCluster, (ci + 1) * fieldsPerCluster).map(f => sanitizeLabel(f)),
      hasList: false,
      listColumns: ['ID', 'Name', 'Status', 'Date'],
    }));

    // Add list sections
    for (const lt of lists) {
      sections.push({
        title: sanitizeLabel(lt),
        fields: [],
        hasList: true,
        listColumns: fields.slice(0, 4).map(f => sanitizeLabel(f)),
      });
    }

    parsed = {
      title: humanizePageId(pageId),
      navLinks,
      sections,
      actions,
      hasTabs: tabs.length > 0,
      tabLabels: tabs.map(t => sanitizeLabel(t)),
    };
  }

  const pageType = detectPageType(pageId);

  const fileContent = generatePageContent({
    pageId, domain, component, pageType,
    title: parsed.title,
    navLinks: parsed.navLinks,
    sections: parsed.sections,
    actions: parsed.actions,
    hasTabs: parsed.hasTabs,
    tabLabels: parsed.tabLabels,
    relToShared, relToHooks, relToApi,
    hasReference,
  });

  try {
    fs.writeFileSync(outFile, fileContent, 'utf8');
    generated++;
    if (generated % 100 === 0) console.log(`  ... ${generated} generated`);
  } catch (e) {
    console.error(`  ERROR writing ${outFile}: ${e.message}`);
    errors++;
  }
}

console.log(`\n[Done] Generated: ${generated}  From reference: ${fromRef} (real: ${fromRefReal}, placeholder: ${fromRefPlaceholder})  Skipped: ${skipped}  Errors: ${errors}`);
