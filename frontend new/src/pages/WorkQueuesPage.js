/**
 * WorkQueuesPage - Lists all work queues
 * Matches legacy CMIPS "Work Queues" screen
 * Columns: Action (View), Name, Administrator, User Subscription
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import http from '../api/httpClient';
import './WorkQueues.css';

export const WorkQueuesPage = () => {
  const navigate = useNavigate();
  const [queues, setQueues] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    http.get('/work-queues')
      .then(res => {
        const d = res?.data;
        setQueues(Array.isArray(d) ? d : (d?.content || []));
      })
      .catch(() => setQueues([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="wq-page"><p>Loading work queues...</p></div>;
  }

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Work Queues</h2>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Work Queues</h4>
        </div>
        <div className="wq-panel-body">
          {queues.length === 0 ? (
            <p className="wq-empty">No work queues available.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Name</th>
                  <th>Administrator</th>
                  <th>User Subscription</th>
                </tr>
              </thead>
              <tbody>
                {queues.map((q, i) => (
                  <tr key={q.id || i}>
                    <td>
                      <button
                        className="action-link"
                        onClick={() => navigate(`/work-queues/${q.id}`)}
                      >
                        View
                      </button>
                    </td>
                    <td>{q.displayName || q.name || '—'}</td>
                    <td>
                      {q.administrator || '—'}
                    </td>
                    <td>{q.subscriptionAllowed !== false ? 'Yes' : 'No'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
