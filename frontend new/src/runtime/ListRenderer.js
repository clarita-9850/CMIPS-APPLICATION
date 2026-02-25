/**
 * ListRenderer - Renders UIM LIST as table/grid component
 */

import React from 'react';

export const ListRenderer = ({
  node,
  data,
  strings,
  onNavigate
}) => {
  const resolveText = (key: string): string => strings[key] || key;
  
  const title = node.title ? resolveText(node.title) : '';
  
  // Extract data from server interface
  const extractData = (sourceName: string, property: string) => {
    const sourceData = data[sourceName] || {};
    
    // Handle nested properties (e.g., "reservedTasks$dtlsList$dtls$subject")
    const parts = property.split('$');
    let value = sourceData;
    
    for (const part of parts) {
      if (value && typeof value === 'object') {
        value = value[part];
      } else {
        break;
      }
    }
    
    return value;
  };

  const rows = data.DISPLAY?.tasks || [];

  const handleRowClick = (row: any, field: any) => {
    if (field.link && onNavigate) {
      const params: Record<string, any> = {};
      
      field.link.params?.forEach((param: any) => {
        const value = extractData(param.source, param.sourceProperty);
        params[param.targetProperty] = value || row[param.sourceProperty];
      });
      
      onNavigate(field.link.pageId, params);
    }
  };

  return (
    <div className={`list-renderer ${node.style || ''}`}>
      {title && <h3 className="list-title">{title}</h3>}
      
      <table className="table table-striped">
        <thead>
          <tr>
            {node.fields.map((field, index) => (
              <th key={index} style={{ width: field.width ? `${field.width}%` : 'auto' }}>
                {resolveText(field.label)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={node.fields.length} className="text-center text-muted">
                No items to display
              </td>
            </tr>
          ) : (
            rows.map((row: any, rowIndex: number) => (
              <tr key={rowIndex}>
                {node.fields.map((field, fieldIndex) => {
                  const value = extractData(field.connect.source, field.connect.property) || 
                                row[field.connect.property.split('$').pop() || ''];
                  
                  return (
                    <td key={fieldIndex}>
                      {field.link ? (
                        <button
                          className="btn btn-link"
                          onClick={() => handleRowClick(row, field)}
                        >
                          {value}
                        </button>
                      ) : (
                        value
                      )}
                    </td>
                  );
                })}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};
