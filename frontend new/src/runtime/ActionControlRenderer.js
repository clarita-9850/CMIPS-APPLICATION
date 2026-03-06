/**
 * ActionControlRenderer - Renders UIM ACTION_CONTROL as buttons/links
 */

import React from 'react';

/**
 * IHSS-specific icons for Action Controls
 */
const getIHSSIcon = (label) => {
  const iconMap = {
    'ActionControl.Label.NewReferral': '🏠', // Home care referral
    'ActionControl.Label.NewApplication': '📋', // Application form
    'ActionControl.Label.FindAPerson': '👤', // Person search
    'ActionControl.Label.FindStateHearingCase': '⚖️', // Legal/hearing
    'ActionControl.Label.RegisterProvider': '🧑', // IHSS home care provider registration
    'ActionControl.Label.MergeDuplicateSSN': '🔗', // Merge/link records
    'ActionControl.Label.EnterWarrantReplacement': '💵', // Payment replacement
    'ActionControl.Label.BVITimesheetReissue': '⏰', // Timesheet
    'ActionControl.Label.TravelClaimManualEntry': '🚗', // Travel reimbursement
    'ActionControl.Label.LiveInProvider': '🏡', // IRS Live-in certification
    'ActionControl.Label.DirectDepoist': '🏦', // Bank deposit
    'ActionControl.Label.SickLeaveClaimManualEntry': '🏥' // Sick leave
  };
  
  return iconMap[label] || '📌';
};

export const ActionControlRenderer = ({
  node,
  strings,
  onNavigate
}) => {
  const resolveText = (key: string): string => strings[key] || key;
  
  const handleClick = () => {
    if (node.link && onNavigate) {
      const params: Record<string, any> = {};
      
      node.link.params?.forEach(param => {
        // Handle CONSTANT source
        if (param.source === 'CONSTANT') {
          params[param.targetProperty] = param.sourceProperty;
        }
      });
      
      onNavigate(node.link.pageId, params);
    }
  };

  const icon = getIHSSIcon(node.label);
  const labelText = resolveText(node.label);

  return (
    <button
      className="btn btn-primary action-control"
      onClick={handleClick}
      style={{ 
        width: '100%', 
        marginBottom: '0.5rem',
        textAlign: 'left',
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem'
      }}
    >
      <span style={{ fontSize: '1.5rem' }}>{icon}</span>
      <span>{labelText}</span>
    </button>
  );
};
