/**
 * TaskSearchPage - Search tasks by Task ID, Case Number, or Case
 * Matches legacy CMIPS "Task Search" screen
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import http from '../api/httpClient';
import './WorkQueues.css';

export const TaskSearchPage = () => {
  const navigate = useNavigate();
  const [searchType, setSearchType] = useState('taskId');
  const [searchValue, setSearchValue] = useState('');
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSearch = () => {
    if (!searchValue.trim()) return;
    setLoading(true);
    setSearched(true);

    let url = '/tasks?';
    if (searchType === 'taskId') url += `taskId=${encodeURIComponent(searchValue)}`;
    else if (searchType === 'caseNumber') url += `caseNumber=${encodeURIComponent(searchValue)}`;
    else url += `caseName=${encodeURIComponent(searchValue)}`;

    http.get(url)
      .then(res => {
        const d = res?.data;
        setResults(Array.isArray(d) ? d : (d?.content || []));
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  };

  const handleReset = () => {
    setSearchValue('');
    setResults([]);
    setSearched(false);
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Task Search</h2>
      </div>

      {/* Search Form */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-form">
            <div className="wq-form-field">
              <label>Search By</label>
              <select value={searchType} onChange={e => setSearchType(e.target.value)}>
                <option value="taskId">Task ID</option>
                <option value="caseNumber">Case Number</option>
                <option value="caseName">Case</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>
                {searchType === 'taskId' ? 'Task ID' :
                 searchType === 'caseNumber' ? 'Case Number' : 'Case Name'}
              </label>
              <input
                type="text"
                value={searchValue}
                onChange={e => setSearchValue(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <div className="wq-form-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
                Search
              </button>
              <button className="wq-btn wq-btn-outline" onClick={handleReset}>Reset</button>
            </div>
          </div>
        </div>
      </div>

      {/* Results */}
      {loading ? (
        <p style={{ padding: '1rem' }}>Searching...</p>
      ) : searched && (
        <div className="wq-panel" style={{ marginTop: '1rem' }}>
          <div className="wq-panel-header"><h4>Search Results ({results.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {results.length === 0 ? (
              <p className="wq-empty">No tasks found matching the criteria.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Task ID</th>
                    <th>Subject</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Assigned To</th>
                    <th>Deadline</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((t, i) => {
                    const tid = t.id || t.taskId;
                    const status = t.status || 'OPEN';
                    return (
                      <tr key={tid || i}>
                        <td>
                          <button className="action-link" onClick={() => navigate(`/tasks/${tid}`)}>View</button>
                        </td>
                        <td>
                          <button className="action-link" onClick={() => navigate(`/tasks/${tid}`)}>{tid}</button>
                        </td>
                        <td>{t.subject || t.title || '\u2014'}</td>
                        <td>{t.priority || 'Medium'}</td>
                        <td>
                          <span className={`wq-badge wq-badge-${status.toLowerCase()}`}>
                            {status}
                          </span>
                        </td>
                        <td>{t.assignedTo || '\u2014'}</td>
                        <td>{t.deadline || t.dueDate || '\u2014'}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
