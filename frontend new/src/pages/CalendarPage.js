import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const CalendarPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [currentDate, setCurrentDate] = useState(new Date());
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState('month');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Calendar' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await http.get('/tasks');
        const d = res?.data;
        const list = Array.isArray(d) ? d : (d?.content || []);
        setTasks(list);
      } catch (err) {
        console.warn('[Calendar] Error loading tasks:', err?.message);
        setTasks([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDayOfWeek = new Date(year, month, 1).getDay();
  const monthName = currentDate.toLocaleString('default', { month: 'long', year: 'numeric' });

  const prevMonth = () => setCurrentDate(new Date(year, month - 1, 1));
  const nextMonth = () => setCurrentDate(new Date(year, month + 1, 1));
  const goToday = () => setCurrentDate(new Date());

  const getTasksForDate = (day) => {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return tasks.filter(t => {
      const due = t.dueDate || t.deadline || '';
      return due.startsWith(dateStr);
    });
  };

  const today = new Date();
  const isToday = (day) =>
    today.getFullYear() === year && today.getMonth() === month && today.getDate() === day;

  const calendarDays = [];
  for (let i = 0; i < firstDayOfWeek; i++) calendarDays.push(null);
  for (let d = 1; d <= daysInMonth; d++) calendarDays.push(d);

  const upcomingTasks = tasks
    .filter(t => {
      const due = t.dueDate || t.deadline || '';
      return due >= new Date().toISOString().split('T')[0];
    })
    .sort((a, b) => (a.dueDate || a.deadline || '').localeCompare(b.dueDate || b.deadline || ''))
    .slice(0, 10);

  if (loading) return <div className="wq-page"><p>Loading calendar...</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Calendar</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className={`wq-btn ${viewMode === 'month' ? 'wq-btn-primary' : 'wq-btn-outline'}`} onClick={() => setViewMode('month')}>Month</button>
          <button className={`wq-btn ${viewMode === 'list' ? 'wq-btn-primary' : 'wq-btn-outline'}`} onClick={() => setViewMode('list')}>List</button>
        </div>
      </div>

      {viewMode === 'month' && (
        <div className="wq-panel">
          <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button className="wq-btn wq-btn-outline" onClick={prevMonth} style={{ padding: '0.25rem 0.75rem' }}>&lt;</button>
            <h4 style={{ margin: 0 }}>{monthName}</h4>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button className="wq-btn wq-btn-outline" onClick={goToday} style={{ padding: '0.25rem 0.75rem', fontSize: '0.8rem' }}>Today</button>
              <button className="wq-btn wq-btn-outline" onClick={nextMonth} style={{ padding: '0.25rem 0.75rem' }}>&gt;</button>
            </div>
          </div>
          <div className="wq-panel-body" style={{ padding: '0.5rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '1px', background: '#e2e8f0' }}>
              {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                <div key={d} style={{ background: '#edf2f7', padding: '0.5rem', textAlign: 'center', fontWeight: 600, fontSize: '0.8rem' }}>{d}</div>
              ))}
              {calendarDays.map((day, i) => {
                const dayTasks = day ? getTasksForDate(day) : [];
                return (
                  <div key={i} style={{
                    background: day ? (isToday(day) ? '#ebf8ff' : '#fff') : '#f7fafc',
                    padding: '0.25rem',
                    minHeight: '70px',
                    fontSize: '0.8rem',
                    border: isToday(day) ? '2px solid #3182ce' : 'none'
                  }}>
                    {day && (
                      <>
                        <div style={{ fontWeight: isToday(day) ? 700 : 400, color: isToday(day) ? '#3182ce' : '#4a5568', marginBottom: '2px' }}>{day}</div>
                        {dayTasks.slice(0, 3).map((t, ti) => (
                          <div key={ti}
                            onClick={() => navigate(`/tasks/${t.id || t.taskId}`)}
                            style={{ background: '#bee3f8', borderRadius: '2px', padding: '1px 4px', marginBottom: '1px', fontSize: '0.7rem', cursor: 'pointer', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {t.taskName || t.name || t.subject || 'Task'}
                          </div>
                        ))}
                        {dayTasks.length > 3 && (
                          <div style={{ fontSize: '0.65rem', color: '#718096' }}>+{dayTasks.length - 3} more</div>
                        )}
                      </>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {viewMode === 'list' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Upcoming Tasks ({upcomingTasks.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {upcomingTasks.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No upcoming tasks with due dates.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Due Date</th><th>Task</th><th>Status</th><th>Priority</th><th>Case</th></tr>
                </thead>
                <tbody>
                  {upcomingTasks.map((t, i) => (
                    <tr key={i} className="wq-clickable-row" onClick={() => navigate(`/tasks/${t.id || t.taskId}`)}>
                      <td>{t.dueDate || t.deadline || '\u2014'}</td>
                      <td>{t.taskName || t.name || t.subject || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(t.status || '').toLowerCase()}`}>{t.status || '\u2014'}</span></td>
                      <td>{t.priority || '\u2014'}</td>
                      <td>{t.caseNumber || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
