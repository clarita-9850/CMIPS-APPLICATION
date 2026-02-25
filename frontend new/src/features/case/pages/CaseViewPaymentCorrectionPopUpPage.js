import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'submitapprove Payment Correction', route: '/case/submitapprove-payment-correction' },
    { label: 'cancel Payment Correction Submission', route: '/case/cancel-payment-correction-submission' },
    { label: 'approve Payment Correction', route: '/case/approve-payment-correction' },
    { label: 'reject Payment Correction', route: '/case/reject-payment-correction' },
    { label: 'cancel Payment Correction', route: '/case/cancel-payment-correction' },
    { label: 'view Timesheet Details For Payment', route: '/case/view-timesheet-details-for-payment' },
    { label: 'edit Payment Correction', route: '/case/edit-payment-correction' },
    { label: 'view Payment Correction List', route: '/case/view-payment-correction-list' }
  ];

export function CaseViewPaymentCorrectionPopUpPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewPaymentCorrectionPopUp"}
      title={"View Payment Correction:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Timesheet Type"} value={record && record['timesheetType']} />
          <UimField label={"Submitted By"} value={record && record['submittedBy']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Related Special Transactions"} value={record && record['relatedSpecialTransactions']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Correction Type"} value={record && record['correctionType']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Approved By/Rejected By"} value={record && record['approvedByRejectedBy']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
          <UimField label={"Code"} value={record && record['code']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <UimSection title={"Exceptions"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Submit For Approval')}>Submit For Approval</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cancel Submission')}>Cancel Submission</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, { status: 'approved' }).then(() => { alert('Approved successfully'); navigate(-1); }).catch(err => alert('Approve failed: ' + err.message)); }}>Approve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, { status: 'denied' }).then(() => { alert('Denied successfully'); navigate(-1); }).catch(err => alert('Action failed: ' + err.message)); }}>Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/cancel-payment-correction-submission')}>Cancel Payment Correction</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-timesheet-details-for-payment')}>View Timesheet Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewPaymentCorrectionPopUpPage;
