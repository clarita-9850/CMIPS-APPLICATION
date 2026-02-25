import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Eligibility', route: '/case/view-eligibility' }
  ];

export function MediCalViewShareOfCostPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"MediCal_viewShareOfCost"}
      title={"Share of Cost Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Eligibility Month"} value={record && record['eligibilityMonth']} />
          <UimField label={"Medi-Cal SOC"} value={record && record['mediCalSOC']} />
          <UimField label={"Medi-Cal Case Number"} value={record && record['mediCalCaseNumber']} />
          <UimField label={"FFP"} value={record && record['fFP']} />
          <UimField label={"Medi-Cal Eligibility Status"} value={record && record['mediCalEligibilityStatus']} />
          <UimField label={"IHSS Funding Program"} value={record && record['iHSSFundingProgram']} />
          <UimField label={"Non-Reversed SOC Amount"} value={record && record['nonReversedSOCAmount']} />
          <UimField label={"Related IHSS Cases"} value={record && record['relatedIHSSCases']} />
          <UimField label={"Date"} value={record && record['date']} />
          <UimField label={"Requested Amount"} value={record && record['requestedAmount']} />
          <UimField label={"MEDS SOC"} value={record && record['mEDSSOC']} />
        </div>
      </UimSection>
      <UimSection title={"Initial Buy-Out"}>
        <div className="uim-form-grid">
          <UimField label={"IHSS SOC"} value={record && record['iHSSSOC']} />
          <UimField label={"IHSS AUTH"} value={record && record['iHSSAUTH']} />
          <UimField label={"Applied Amount"} value={record && record['appliedAmount']} />
          <UimField label={"Error"} value={record && record['error']} />
          <UimField label={"Transaction Date"} value={record && record['transactionDate']} />
          <UimField label={"Transaction"} value={record && record['transaction']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"User"} value={record && record['user']} />
          <UimField label={"Void Date"} value={record && record['voidDate']} />
          <UimField label={"User"} value={record && record['user']} />
        </div>
      </UimSection>
      <UimSection title={"Recipient Reimbursement History"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default MediCalViewShareOfCostPage;
