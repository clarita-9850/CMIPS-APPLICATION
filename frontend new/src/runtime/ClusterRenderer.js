/**
 * ClusterRenderer - Renders UIM CLUSTER as responsive grid/fieldset
 */

import React from 'react';
import { ListRenderer } from './ListRenderer';
import { ActionControlRenderer } from './ActionControlRenderer';

export const ClusterRenderer = ({
  node,
  data,
  strings,
  onNavigate,
  evaluateCondition
}) => {
  const resolveText = (key) => strings[key] || key;
  
  const title = node.title ? resolveText(node.title) : '';
  const numCols = node.numCols || 1;
  
  // Import sub-renderers dynamically to avoid circular dependencies
  const renderNode = (child, index) => {
    if (child.type === 'LIST') {
      return <ListRenderer key={index} node={child} data={data} strings={strings} onNavigate={onNavigate} />;
    }
    
    if (child.type === 'CLUSTER') {
      return (
        <ClusterRenderer
          key={index}
          node={child}
          data={data}
          strings={strings}
          onNavigate={onNavigate}
          evaluateCondition={evaluateCondition}
        />
      );
    }
    
    if (child.type === 'ACTION_CONTROL') {
      if (!evaluateCondition(child.condition)) return null;
      return <ActionControlRenderer key={index} node={child} strings={strings} onNavigate={onNavigate} />;
    }
    
    if (child.type === 'CONTAINER') {
      return (
        <div key={index} className="container-node">
          {child.children?.map((subChild, subIndex) => renderNode(subChild, subIndex))}
        </div>
      );
    }
    
    return null;
  };

  return (
    <div className={`cluster-renderer ${node.style || ''}`}>
      {title && <h2 className="cluster-title">{title}</h2>}
      
      <div 
        className="cluster-content" 
        style={{ 
          display: 'grid',
          gridTemplateColumns: `repeat(${numCols}, 1fr)`,
          gap: '1.5rem',
          alignItems: 'start'
        }}
      >
        {node.children.map((child, index) => renderNode(child, index))}
      </div>
    </div>
  );
};
