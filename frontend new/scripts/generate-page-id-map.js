#!/usr/bin/env node
/**
 * Generates src/routes/pageIdToRoute.js
 * Maps every UIM pageId to its frontend route path.
 * Used by getRouteForPageId() for navigation between generated pages.
 *
 * Usage: node scripts/generate-page-id-map.js
 */

'use strict';
const fs   = require('fs');
const path = require('path');

const ROOT       = path.resolve(__dirname, '..');
const GRAPH_FILE = path.resolve(ROOT, '..', '..', 'CMIPS3.0-main', 'frontend', 'docs', 'uim-graph.json');
const OUT_FILE   = path.join(ROOT, 'src', 'routes', 'pageIdToRoute.js');

if (!fs.existsSync(GRAPH_FILE)) {
  console.error(`[Error] uim-graph.json not found at: ${GRAPH_FILE}`);
  process.exit(1);
}

const graph = JSON.parse(fs.readFileSync(GRAPH_FILE, 'utf8'));

// Collect all nodes (both stub and converted) that have a route
const entries = graph.nodes
  .filter(n => n.route && n.pageId)
  .map(n => ({ pageId: n.pageId, route: n.route }));

// Sort for stable output
entries.sort((a, b) => a.pageId.localeCompare(b.pageId));

const lines = entries.map(e =>
  `  '${e.pageId.replace(/'/g, "\\'")}': '${e.route.replace(/'/g, "\\'")}'`
);

const content = `/**
 * AUTO-GENERATED — do not edit manually.
 * Maps every UIM pageId to its frontend route path.
 * Generated from uim-graph.json (${entries.length} entries).
 *
 * Usage:
 *   import { PAGE_ID_TO_ROUTE, resolvePageId } from './pageIdToRoute';
 *   const route = resolvePageId('Case_viewCase', { id: '123' });
 */

export const PAGE_ID_TO_ROUTE = {
${lines.join(',\n')}
};

/**
 * Resolve a pageId to a route path, substituting any URL parameters.
 * Falls back to /p/:pageId for unknown pageIds.
 */
export function resolvePageId(pageId, params = {}) {
  let route = PAGE_ID_TO_ROUTE[pageId];
  if (!route) return '/p/' + pageId;

  // Substitute :param placeholders if the route has them
  Object.keys(params).forEach(k => {
    route = route.replace(':' + k, params[k] || '');
  });
  return route;
}

export default PAGE_ID_TO_ROUTE;
`;

fs.mkdirSync(path.dirname(OUT_FILE), { recursive: true });
fs.writeFileSync(OUT_FILE, content, 'utf8');
console.log(`[Done] Generated ${OUT_FILE} with ${entries.length} pageId mappings.`);
