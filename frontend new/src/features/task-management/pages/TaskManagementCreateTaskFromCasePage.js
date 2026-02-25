import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function TaskManagementCreateTaskFromCasePage() {
  const navigate = useNavigate();
  const tasksApi = getDomainApi('task-management');
  return (
    <UimPageLayout
      pageId={"TaskManagement_createTaskFromCase"}
      title={"Create Task From Case"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      <UimSection title={"Concerning"}>
        <div className="uim-form-grid">
          <UimField label={"Case Participant"} />
          <UimField label={"Case Number"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { tasksApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default TaskManagementCreateTaskFromCasePage;
