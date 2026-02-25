import React, { useState } from 'react';

/**
 * UimTable - renders a mock LIST with placeholder rows.
 * Props:
 *   title       - optional table heading
 *   columns     - array of column header strings
 *   rowCount    - number of mock rows to display (default 3)
 *   onRowClick  - optional (rowIndex) => void handler
 *   actionLabel - optional label for the row action button (default "View")
 */
export function UimTable({
  title,
  columns = [],
  rowCount = 3,
  onRowClick,
  actionLabel = 'View',
}) {
  const [page, setPage] = useState(1);
  const cols = columns.length > 0 ? columns : ['Column 1', 'Column 2', 'Column 3'];

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
              {cols.map(c => <th key={c}>{c}</th>)}
              {onRowClick && <th>Action</th>}
            </tr>
          </thead>
          <tbody>
            {rowCount === 0 ? (
              <tr className="empty-row"><td colSpan={cols.length + (onRowClick ? 1 : 0)}>No records found</td></tr>
            ) : (
              Array.from({ length: rowCount }).map((_, i) => (
                <tr key={i}>
                  {cols.map((c, ci) => (
                    <td key={ci}>{`${c} value ${i + 1}`}</td>
                  ))}
                  {onRowClick && (
                    <td className="link-cell">
                      <button onClick={() => onRowClick(i)}>{actionLabel}</button>
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

export default UimTable;
