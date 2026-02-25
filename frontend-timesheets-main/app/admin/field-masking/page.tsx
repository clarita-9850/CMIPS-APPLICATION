'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/lib/contexts/AuthContext';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fieldMaskingService,
  type FieldMaskingRule,
  type FieldMetadata,
} from '@/lib/services/fieldMasking.service';

export default function FieldMaskingPage() {
  const { t } = useTranslation();
  const { user, isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const [selectedReportType, setSelectedReportType] = useState<string>('TIMESHEET_REPORT');
  const [selectedRole, setSelectedRole] = useState<string>('');
  const [localRules, setLocalRules] = useState<FieldMaskingRule[]>([]);
  const [selectedFields, setSelectedFields] = useState<string[]>([]);
  const [catalogFields, setCatalogFields] = useState<FieldMetadata[]>([]);
  const [hasChanges, setHasChanges] = useState(false);

  const { data: interfaceData, isLoading: isInterfaceLoading } = useQuery({
    queryKey: ['fieldMasking', 'interface', selectedRole, selectedReportType],
    queryFn: () => fieldMaskingService.getInterface(selectedRole, selectedReportType),
    enabled: isAuthenticated && !!selectedRole && !!selectedReportType,
  });

  const {
    data: availableFields,
    isLoading: isLoadingFields,
  } = useQuery({
    queryKey: ['fieldMasking', 'available-fields'],
    queryFn: () => fieldMaskingService.getAvailableFields(),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
  });

  const {
    data: availableRoles,
    isLoading: isLoadingRoles,
    error: availableRolesError,
  } = useQuery({
    queryKey: ['fieldMasking', 'available-roles'],
    queryFn: () => fieldMaskingService.getAvailableRoles(),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
  });

  useEffect(() => {
    if (availableRoles && availableRoles.length) {
      if (!selectedRole || !availableRoles.includes(selectedRole)) {
        setSelectedRole(availableRoles[0]);
      }
    }
  }, [availableRoles, selectedRole]);

  const mergeRulesWithCatalog = (
    catalog: FieldMetadata[],
    rules: FieldMaskingRule[] | undefined
  ): FieldMaskingRule[] => {
    const ruleMap = new Map<string, FieldMaskingRule>((rules || []).map((rule) => [rule.fieldName, rule]));
    if (catalog?.length) {
      return catalog.map((field) => {
        const existing = ruleMap.get(field.name);
        return (
          existing || {
            fieldName: field.name,
            maskingType: 'NONE',
            accessLevel: 'FULL_ACCESS',
          }
        );
      });
    }
    return rules || [];
  };

  useEffect(() => {
    if (!interfaceData) return;

    const catalog =
      (availableFields && availableFields.length ? availableFields : interfaceData.availableFields) || [];
    const merged = mergeRulesWithCatalog(catalog, interfaceData.rules);

    setCatalogFields(catalog);
    setLocalRules(merged);
    setSelectedFields(
      interfaceData.selectedFields && interfaceData.selectedFields.length
        ? interfaceData.selectedFields
        : merged.map((rule) => rule.fieldName)
    );
    setHasChanges(false);
  }, [interfaceData, availableFields]);

  const updateRulesMutation = useMutation({
    mutationFn: (payload: { rules: FieldMaskingRule[]; selectedFields: string[] }) =>
      fieldMaskingService.updateRules(selectedRole, payload.rules, payload.selectedFields),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fieldMasking'] });
      alert('Field masking rules updated successfully!');
      setHasChanges(false);
    },
    onError: (error: any) => {
      alert(`Failed to update rules: ${error.message || 'Unknown error'}`);
    },
  });

  const handleRuleChange = (fieldName: string, maskingType: FieldMaskingRule['maskingType']) => {
    const updatedRules = localRules.map((rule) => {
      if (rule.fieldName === fieldName) {
        // Update accessLevel based on maskingType (only if field is visible)
        const isVisible = selectedFields.includes(fieldName);
        let accessLevel: 'FULL_ACCESS' | 'MASKED_ACCESS' | 'HIDDEN_ACCESS' = rule.accessLevel;
        
        if (isVisible) {
          if (maskingType === 'NONE') {
            accessLevel = 'FULL_ACCESS';
          } else if (maskingType === 'HIDDEN') {
            accessLevel = 'HIDDEN_ACCESS';
          } else {
            // PARTIAL_MASK, HASH_MASK, ANONYMIZE, AGGREGATE
            accessLevel = 'MASKED_ACCESS';
          }
        } else {
          // Field is hidden, keep HIDDEN_ACCESS
          accessLevel = 'HIDDEN_ACCESS';
        }
        
        return { ...rule, maskingType, accessLevel };
      }
      return rule;
    });

    setLocalRules(updatedRules);
    setHasChanges(true);
  };

  const toggleFieldAccess = (fieldName: string) => {
    const isSelected = selectedFields.includes(fieldName);
    const updatedSelected = isSelected
      ? selectedFields.filter((field) => field !== fieldName)
      : [...selectedFields, fieldName];

    // Update accessLevel in rules based on visibility
    const updatedRules = localRules.map((rule) => {
      if (rule.fieldName === fieldName) {
        if (isSelected) {
          // Field is being hidden - set to HIDDEN_ACCESS
          return { ...rule, accessLevel: 'HIDDEN_ACCESS' as const };
        } else {
          // Field is being shown - set accessLevel based on maskingType
          const accessLevel = rule.maskingType === 'NONE' ? 'FULL_ACCESS' : 
                             rule.maskingType === 'PARTIAL_MASK' || rule.maskingType === 'HASH_MASK' || 
                             rule.maskingType === 'ANONYMIZE' || rule.maskingType === 'AGGREGATE' ? 'MASKED_ACCESS' : 
                             'FULL_ACCESS';
          return { ...rule, accessLevel: accessLevel as 'FULL_ACCESS' | 'MASKED_ACCESS' | 'HIDDEN_ACCESS' };
        }
      }
      return rule;
    });

    setSelectedFields(updatedSelected);
    setLocalRules(updatedRules);
    setHasChanges(true);
  };

  if (!isAuthenticated) {
    return (
      <>
        <Breadcrumb path={['Home', 'Admin']} currentPage="Field Masking" />
        <div className="text-center">
          <h1>Please log in to configure field masking</h1>
          <a href="/login" className="btn btn-primary mt-3">
            Go to Login
          </a>
        </div>
      </>
    );
  }

  const isAdmin = user?.role?.toUpperCase() === 'ADMIN';
  
  // Debug logging
  useEffect(() => {
    if (typeof window !== 'undefined') {
      console.log('🔍 Field Masking Page - User check:', {
        user: user,
        userRole: user?.role,
        roleUpperCase: user?.role?.toUpperCase(),
        isAdmin: isAdmin,
        isAuthenticated: isAuthenticated
      });
    }
  }, [user, isAdmin, isAuthenticated]);

  if (!isAdmin) {
    return (
      <>
        <Breadcrumb path={['Home', 'Admin']} currentPage="Field Masking" />
        <div className="container">
          <div className="alert alert-danger">
            <h2>Access Denied</h2>
            <p>Only ADMIN users can access field masking configuration.</p>
            <p>Your current role: <strong>{user?.role || 'Unknown'}</strong></p>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Breadcrumb path={['Home', 'Admin']} currentPage="Field Masking" />
      <div className="container">
        <h1>Field Masking Configuration</h1>
        <p className="mb-4">Configure field masking rules for different user roles and report types</p>

        <div className="row mb-4">
          <div className="col-md-6 mb-3">
            <label htmlFor="role-select" className="form-label">User Role:</label>
            <select
              id="role-select"
              className="form-select"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              disabled={isLoadingRoles || !availableRoles || !availableRoles.length}
            >
              {isLoadingRoles || !availableRoles || !availableRoles.length ? (
                <option value="">Loading roles...</option>
              ) : (
                availableRoles.map((role) => (
                  <option key={role} value={role}>
                    {role.replace(/_/g, ' ')}
                  </option>
                ))
              )}
            </select>
          </div>
          <div className="col-md-6 mb-3">
            <label htmlFor="report-type-select" className="form-label">Report Type:</label>
            <select
              id="report-type-select"
              className="form-select"
              value={selectedReportType}
              onChange={(e) => setSelectedReportType(e.target.value)}
            >
              <option value="TIMESHEET_REPORT">Timesheet Report</option>
              <option value="BI_REPORT">BI Report</option>
              <option value="ANALYTICS_REPORT">Analytics Report</option>
            </select>
          </div>
        </div>

        {isInterfaceLoading || isLoadingFields ? (
          <div className="text-center">
            <p>Loading field masking rules...</p>
          </div>
        ) : localRules.length > 0 ? (
          <div className="card">
            <div className="card-body">
              <h5 className="card-title mb-4">Field Masking Rules</h5>
              <div className="table-responsive">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Field Name</th>
                      <th>Data Type</th>
                      <th>Visible?</th>
                      <th>Current Masking Type</th>
                      <th>Access Level</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {localRules.map((rule, index) => {
                      const fieldMeta: FieldMetadata | undefined = catalogFields?.find(
                        (field) => field.name === rule.fieldName
                      );

                      return (
                      <tr key={index}>
                          <td>
                            <div className="fw-semibold">{fieldMeta?.displayName ?? rule.fieldName}</div>
                            <div className="text-muted small">{rule.fieldName}</div>
                          </td>
                          <td>{fieldMeta?.type ?? '—'}</td>
                          <td>
                            <div className="form-check form-switch">
                              <input
                                className="form-check-input"
                                type="checkbox"
                                role="switch"
                                id={`visible-toggle-${rule.fieldName}`}
                                checked={selectedFields.includes(rule.fieldName)}
                                onChange={() => toggleFieldAccess(rule.fieldName)}
                                disabled={updateRulesMutation.isPending}
                              />
                              <label className="form-check-label" htmlFor={`visible-toggle-${rule.fieldName}`}>
                                {selectedFields.includes(rule.fieldName) ? 'Visible' : 'Hidden'}
                              </label>
                            </div>
                          </td>
                          <td>
                            <span className="badge bg-secondary">{rule.maskingType}</span>
                          </td>
                          <td>
                            <span className={`badge ${
                              rule.accessLevel === 'HIDDEN_ACCESS' ? 'bg-danger' :
                              rule.accessLevel === 'MASKED_ACCESS' ? 'bg-warning' :
                              'bg-success'
                            }`}>
                              {rule.accessLevel}
                            </span>
                          </td>
                          <td>
                            <select
                              className="form-select form-select-sm"
                              value={rule.maskingType}
                              onChange={(e) =>
                                handleRuleChange(rule.fieldName, e.target.value as FieldMaskingRule['maskingType'])
                              }
                              disabled={updateRulesMutation.isPending}
                            >
                              <option value="NONE">None</option>
                              <option value="HIDDEN">Hidden</option>
                              <option value="PARTIAL_MASK">Partial Mask</option>
                              <option value="HASH_MASK">Hash Mask</option>
                              <option value="ANONYMIZE">Anonymize</option>
                              <option value="AGGREGATE">Aggregate</option>
                            </select>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        ) : (
          <div className="alert alert-danger">
            No field masking rules found for the selected role and report type.
          </div>
        )}
        {localRules.length > 0 && (
          <div className="d-flex justify-content-end mt-4">
            <button
              className="btn btn-primary"
              disabled={!hasChanges || updateRulesMutation.isPending}
              onClick={() => updateRulesMutation.mutate({ rules: localRules, selectedFields })}
            >
              {updateRulesMutation.isPending ? 'Saving...' : hasChanges ? 'Save Changes' : 'Saved'}
            </button>
          </div>
        )}
      </div>
    </>
  );
}

