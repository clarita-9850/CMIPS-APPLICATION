/**
 * SideNavPanel - Collapsible shortcut panel
 * Based on DSD Section 20 - Shortcut Panel Navigation
 * 
 * Features:
 * - Expandable/collapsible panel
 * - Multiple shortcut tabs: My Workspace, Search, Wage Rate, County Contractor, Homemaker
 * - Organization and User Search tabs for administrators
 * - Supports controlled state via isExpanded and onToggle props
 */

import React, { useState } from 'react';
import './SideNavPanel.css';

export const SideNavPanel = ({
  userRole = 'standard',
  roles = [],
  isSupervisor = false,
  isPayroll = false,
  onNavigate,
  isExpanded: controlledIsExpanded,
  onToggle
}) => {
  // Use controlled state if provided, otherwise use internal state
  const [internalIsExpanded, setInternalIsExpanded] = useState(false);
  const isExpanded = controlledIsExpanded !== undefined ? controlledIsExpanded : internalIsExpanded;
  
  const [activeTab, setActiveTab] = useState('my-workspace');

  const togglePanel = () => {
    if (onToggle) {
      onToggle();
    } else {
      setInternalIsExpanded(!internalIsExpanded);
    }
  };

  const handleNavigate = (link) => {
    console.log('Navigate to:', link);
    if (onNavigate) {
      onNavigate(link);
    }
  };

  // Standard user shortcuts - IHSS Program Icons
  const myWorkspaceLinks = [
    { id: 'new-referral', label: 'New Referral', icon: '🏠' },
    { id: 'new-application', label: 'New Application', icon: '📋' },
    { id: 'message-center', label: 'Message Center', icon: '💬' },
    { id: 'inbox', label: 'Inbox', icon: '📨' },
    { id: 'assigned-tasks', label: 'Assigned Tasks', icon: '📝' },
    { id: 'reserved-tasks', label: 'Reserved Tasks', icon: '📌' },
    { id: 'deferred-tasks', label: 'Deferred Tasks', icon: '⏸️' },
    { id: 'task-search', label: 'Task Search', icon: '🔎' },
    { id: 'work-queues', label: 'Work Queues', icon: '📥' }
  ];

  const searchLinks = [
    { id: 'person-search', label: 'Person Search', icon: '👤' },
    { id: 'case-search', label: 'Case Search', icon: '🗂️' },
    { id: 'state-hearing-search', label: 'State Hearing Search', icon: '⚖️' },
    { id: 'provider-search', label: 'Provider Search', icon: '🩺' },
    { id: 'helpdesk-person-search', label: 'Help Desk Person Search', icon: '🔎' },
    { id: 'helpdesk-timesheet-search', label: 'Help Desk Timesheet Search', icon: '🕐' },
    { id: 'helpdesk-payment-search', label: 'Help Desk Payment Search', icon: '💳' }
  ];

  // Case domain shortcuts
  const caseLinks = [
    { id: 'new-case', label: 'Create Case', icon: '➕' },
    { id: 'case-search', label: 'Case Search', icon: '🗂️' },
    { id: 'case-contacts', label: 'Contacts', icon: '📞' },
    { id: 'case-service-plans', label: 'Service Plans', icon: '📋' },
    { id: 'case-assessments', label: 'Assessments', icon: '📝' },
    { id: 'case-authorizations', label: 'Authorizations', icon: '✅' },
    { id: 'case-notes', label: 'Notes & Correspondence', icon: '📒' },
    { id: 'case-forms', label: 'Forms', icon: '📄' }
  ];

  // Evidence domain shortcuts
  const evidenceLinks = [
    { id: 'evidence-home', label: 'Evidence Home', icon: '📁' },
    { id: 'evidence-household', label: 'Household Evidence', icon: '🏠' },
    { id: 'evidence-companion', label: 'Companion Cases', icon: '🔗' },
    { id: 'evidence-income', label: 'Income', icon: '💰' },
    { id: 'evidence-soc', label: 'Share of Cost', icon: '📊' }
  ];

  // Person/Recipient domain shortcuts
  const personLinks = [
    { id: 'person-search', label: 'Person Search', icon: '👤' },
    { id: 'new-referral', label: 'New Referral', icon: '🏠' },
    { id: 'person-forms', label: 'Electronic Forms', icon: '📄' },
    { id: 'person-addresses', label: 'Addresses', icon: '📍' },
    { id: 'person-cp-claims', label: 'CP Claims Search', icon: '🔎' },
    { id: 'person-merge', label: 'Person Merge', icon: '🔀' }
  ];

  const wageRateLinks = [
    { id: 'public-authority-wage-rate', label: 'Public Authority Wage Rate', icon: '💰' },
    { id: 'county-contractor-rate', label: 'County Contractor Rate', icon: '💵' }
  ];

  const countyContractorLinks = [
    { id: 'county-contractor-home', label: 'County Contractor Home', icon: '🏢' },
    { id: 'county-contractor-search', label: 'County Contractor Search', icon: '🔍' }
  ];

  const homemakerLinks = [
    { id: 'homemaker-home', label: 'Homemaker Home', icon: '🧹' },
    { id: 'homemaker-search', label: 'Homemaker Search', icon: '🔍' }
  ];

  // Administrator shortcuts - IHSS Program Icons
  const organizationLinks = [
    { id: 'org-home', label: 'Home', icon: '🏛️' },
    { id: 'child-org-units', label: 'Child Organization Units', icon: '🏢' },
    { id: 'positions', label: 'Positions', icon: '👔' },
    { id: 'users', label: 'Users', icon: '👥' },
    { id: 'data-pipeline', label: 'Data Pipeline', icon: '🔀' },
    { id: 'admin-work-queues', label: 'Work Queue Admin', icon: '📋' }
  ];

  const userSearchLinks = [
    { id: 'user-home', label: 'Home', icon: '🔧' }
  ];

  const workflowMenuLinks = [
    { id: 'work-queues', label: 'Work Queues', icon: '📥' }
  ];

  // Supervisor-specific shortcuts
  const supervisorLinks = [
    { id: 'supervisor-dashboard', label: 'Supervisor Dashboard', icon: '📊' },
    { id: 'approval-queue', label: 'Approval Queue', icon: '✅' },
    { id: 'supervisor-tasks', label: 'Team Tasks', icon: '👥' },
    { id: 'work-queues', label: 'Work Queue Admin', icon: '⚙️' }
  ];

  // Payment-specific shortcuts
  const paymentLinks = [
    { id: 'timesheets', label: 'Timesheets', icon: '⏱️' },
    { id: 'warrant-replacements', label: 'Warrant Replacements', icon: '📄' },
    { id: 'bvi-timesheet', label: 'BVI Timesheet Reissue', icon: '🔄' },
    { id: 'travel-claim', label: 'Travel Claim', icon: '🚗' },
    { id: 'direct-deposit', label: 'Direct Deposit', icon: '🏦' },
    { id: 'sick-leave', label: 'Sick Leave Claim', icon: '🏥' }
  ];

  // Services shortcuts (EVV, Waivers)
  const servicesLinks = [
    { id: 'evv', label: 'Electronic Visit Verification', icon: '📍' },
    { id: 'waivers', label: 'Waivers', icon: '📜' },
    { id: 'analytics', label: 'Analytics & Reports', icon: '📊' },
    { id: 'bi-reports', label: 'BI Reports', icon: '📈' }
  ];

  const tabs = [
    { id: 'my-workspace', label: 'My Workspace', links: myWorkspaceLinks },
    { id: 'search', label: 'Search', links: searchLinks },
    { id: 'case', label: 'Case', links: caseLinks },
    { id: 'person', label: 'Person', links: personLinks },
    { id: 'evidence', label: 'Evidence', links: evidenceLinks },
    { id: 'wage-rate', label: 'Wage Rate', links: wageRateLinks },
    { id: 'county-contractor', label: 'County Contractor', links: countyContractorLinks },
    { id: 'homemaker', label: 'Homemaker', links: homemakerLinks }
  ];

  // Add supervisor tab if user has supervisor roles
  if (isSupervisor || userRole === 'supervisor') {
    tabs.push({ id: 'supervisor', label: 'Supervisor', links: supervisorLinks });
  }

  // Add payment tab if user has payroll/timesheet roles
  if (isPayroll || roles.some(r => r.includes('PAYROLL') || r.includes('TIMESHEET') || r.includes('WARRANT'))) {
    tabs.push({ id: 'payments', label: 'Payments', links: paymentLinks });
  }

  // Add Services tab for EVV, Waivers, Analytics, BI Reports
  tabs.push({ id: 'services', label: 'Services', links: servicesLinks });

  // Add admin tabs if user is admin
  const isAdminRole = userRole === 'admin' || userRole === 'county-admin' ||
    roles.some(r => r === 'ADMIN');
  if (isAdminRole) {
    tabs.push(
      { id: 'organization', label: 'Organization', links: organizationLinks },
      { id: 'user-search', label: 'User Search', links: userSearchLinks },
      { id: 'workflow-menu', label: 'Workflow Menu', links: workflowMenuLinks }
    );
  }

  const currentLinks = tabs.find(tab => tab.id === activeTab)?.links || [];

  return (
    <div className={`side-nav-panel ${isExpanded ? 'expanded' : 'collapsed'}`}>
      {/* Toggle button */}
      <button className="side-nav-toggle" onClick={togglePanel} title={isExpanded ? 'Collapse' : 'Expand'}>
        {isExpanded ? '«' : '»'}
      </button>

      {isExpanded ? (
        <div className="side-nav-content">
          {/* Shortcut Panel Title */}
          <div className="side-nav-header">
            <h3>Shortcuts</h3>
          </div>

          {/* Shortcut Tabs */}
          <div className="side-nav-tabs">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`side-nav-tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Shortcut Links */}
          <div className="side-nav-links">
            {currentLinks.map((link) => (
              <button
                key={link.id}
                className="side-nav-link"
                onClick={() => handleNavigate(link.id)}
              >
                <span className="link-icon">{link.icon}</span>
                <span className="link-label">{link.label}</span>
              </button>
            ))}
          </div>
        </div>
      ) : (
        <div className="side-nav-icons">
          {currentLinks.map((link) => (
            <button
              key={link.id}
              className="side-nav-icon-button"
              onClick={() => handleNavigate(link.id)}
              title={link.label}
            >
              <span className="icon-only">{link.icon}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
};
