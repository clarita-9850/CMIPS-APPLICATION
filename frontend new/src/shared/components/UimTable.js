import React, { useState } from 'react';

/**
 * UimTable - renders a data table with real or mock data.
 * Props:
 *   title       - optional table heading
 *   columns     - array of column headers (strings) OR array of {key, label} objects
 *   data        - array of row objects (if provided, renders real data)
 *   rowCount    - number of mock rows when no data prop (default 3)
 *   onRowClick  - (rowIndex | rowObject) => void handler
 *   onRowAction - (rowObject) => void handler for action button per row
 *   actionLabel - label for the row action button (default "View")
 *   actions     - array of {label, onClick: (row) => void, visible?: (row) => bool} for multiple actions per row
 *   pageSize    - rows per page (default 50)
 *   emptyMessage - message when no data (default "No records found")
 */
export function UimTable({
  title,
  columns = [],
  data,
  rowCount = 3,
  onRowClick,
  onRowAction,
  actionLabel = 'View',
  actions = [],
  pageSize = 50,
  emptyMessage = 'No records found',
}) {
  const [page, setPage] = useState(1);

  const hasRealData = Array.isArray(data);
  const hasStructuredColumns = columns.length > 0 && typeof columns[0] === 'object';

  // Normalize columns to [{key, label}] format
  const cols = hasStructuredColumns
    ? columns
    : columns.length > 0
      ? columns.map((c, i) => ({ key: `col${i}`, label: c }))
      : [{ key: 'col0', label: 'Column 1' }, { key: 'col1', label: 'Column 2' }, { key: 'col2', label: 'Column 3' }];

  const hasActions = onRowAction || onRowClick || actions.length > 0;

  // Real data rendering
  if (hasRealData) {
    const totalPages = Math.max(1, Math.ceil(data.length / pageSize));
    const start = (page - 1) * pageSize;
    const pageData = data.slice(start, start + pageSize);

    return (
      <div>
        {title && (
          <h4 style={{ color: 'var(--cdss-blue)', marginBottom: '0.5rem', fontWeight: 600 }}>
            {title}
          </h4>
        )}
        <div className="uim-table-wrapper">
          <table className="uim-table">
            <thead>
              <tr>
                {cols.map(c => <th key={c.key}>{c.label}</th>)}
                {hasActions && <th>Action</th>}
              </tr>
            </thead>
            <tbody>
              {pageData.length === 0 ? (
                <tr className="empty-row">
                  <td colSpan={cols.length + (hasActions ? 1 : 0)}>{emptyMessage}</td>
                </tr>
              ) : (
                pageData.map((row, i) => (
                  <tr key={row.id || i}>
                    {cols.map(c => (
                      <td key={c.key}>{formatCell(row[c.key])}</td>
                    ))}
                    {hasActions && (
                      <td className="link-cell">
                        {onRowAction && (
                          <button onClick={() => onRowAction(row)}>{actionLabel}</button>
                        )}
                        {onRowClick && !onRowAction && (
                          <button onClick={() => onRowClick(row)}>{actionLabel}</button>
                        )}
                        {actions.map((act, ai) => {
                          if (act.visible && !act.visible(row)) return null;
                          return (
                            <button
                              key={ai}
                              onClick={() => act.onClick(row)}
                              style={ai > 0 || onRowAction ? { marginLeft: '0.5rem' } : undefined}
                            >
                              {act.label}
                            </button>
                          );
                        })}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        {data.length > pageSize && (
          <div className="uim-pagination">
            <button className="uim-btn uim-btn-secondary" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>&lsaquo;</button>
            <span style={{ padding: '0.4rem 0.75rem', fontSize: '0.875rem' }}>
              Page {page} of {totalPages} ({data.length} records)
            </span>
            <button className="uim-btn uim-btn-secondary" onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>&rsaquo;</button>
          </div>
        )}
      </div>
    );
  }

  // Mock data rendering (backward compatible)
  const mockCols = cols.map(c => c.label);
  return (
    <div>
      {title && (
        <h4 style={{ color: 'var(--cdss-blue)', marginBottom: '0.5rem', fontWeight: 600 }}>
          {title}
        </h4>
      )}
      <div className="uim-table-wrapper">
        <table className="uim-table">
          <thead>
            <tr>
              {mockCols.map(c => <th key={c}>{c}</th>)}
              {(onRowClick || onRowAction) && <th>Action</th>}
            </tr>
          </thead>
          <tbody>
            {rowCount === 0 ? (
              <tr className="empty-row"><td colSpan={mockCols.length + ((onRowClick || onRowAction) ? 1 : 0)}>{emptyMessage}</td></tr>
            ) : (
              Array.from({ length: rowCount }).map((_, i) => (
                <tr key={i}>
                  {mockCols.map((c, ci) => (
                    <td key={ci}>{`${c} value ${i + 1}`}</td>
                  ))}
                  {(onRowClick || onRowAction) && (
                    <td className="link-cell">
                      <button onClick={() => (onRowAction || onRowClick)(i)}>{actionLabel}</button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {rowCount > 0 && (
        <div className="uim-pagination">
          <button className="uim-btn uim-btn-secondary" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>&lsaquo;</button>
          <span style={{ padding: '0.4rem 0.75rem', fontSize: '0.875rem' }}>Page {page}</span>
          <button className="uim-btn uim-btn-secondary" onClick={() => setPage(p => p + 1)}>&rsaquo;</button>
        </div>
      )}
    </div>
  );
}

function formatCell(val) {
  if (val === null || val === undefined) return '\u2014';
  if (typeof val === 'boolean') return val ? 'Yes' : 'No';
  return String(val);
}

export default UimTable;
