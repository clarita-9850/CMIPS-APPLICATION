/**
 * PageRenderer - Main UIM page rendering engine
 * Converts PageSpec into React components using CA State Web Template
 */

import React from 'react';
import { ClusterRenderer } from './ClusterRenderer';
import { ListRenderer } from './ListRenderer';
import { ActionControlRenderer } from './ActionControlRenderer';

export const PageRenderer = ({ 
  spec, 
  data = {}, 
  strings,
  onNavigate 
}) => {
  
  // Resolve text keys to actual strings
  const resolveText = (key) => {
    return strings[key] || key;
  };

  // Evaluate conditions based on data
  const evaluateCondition = (condition) => {
    if (!condition) return true;
    
    const sourceData = data[condition.source] || {};
    const value = sourceData[condition.property];
    
    if (condition.type === 'IS_TRUE') {
      return value === true || value === 'true';
    } else if (condition.type === 'IS_FALSE') {
      return value === false || value === 'false' || !value;
    }
    
    return true;
  };

  // Render page title
  const renderTitle = () => {
    if (spec.title.textKeys) {
      return spec.title.textKeys.map(key => resolveText(key)).join(' ');
    }
    return spec.title.text || '';
  };

  // Render layout nodes recursively
  const renderNode = (node, index) => {
    switch (node.type) {
      case 'CLUSTER':
        return (
          <ClusterRenderer
            key={index}
            node={node}
            data={data}
            strings={strings}
            onNavigate={onNavigate}
            evaluateCondition={evaluateCondition}
          />
        );
      
      case 'LIST':
        return (
          <ListRenderer
            key={index}
            node={node}
            data={data}
            strings={strings}
            onNavigate={onNavigate}
          />
        );
      
      case 'ACTION_CONTROL':
        const actionNode = node;
        if (!evaluateCondition(actionNode.condition)) {
          return null;
        }
        return (
          <ActionControlRenderer
            key={index}
            node={actionNode}
            strings={strings}
            onNavigate={onNavigate}
          />
        );
      
      case 'CONTAINER':
        return (
          <div key={index} className="container-node">
            {node.children?.map((child, childIndex) => renderNode(child, childIndex))}
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div className="page-renderer" data-page-id={spec.pageId}>
      <div className="main-content">
        <main className="main-primary">
          <div className="container">
            <h1 className="page-title">{renderTitle()}</h1>
            
            <div className="page-content">
              {spec.content.map((node, index) => renderNode(node, index))}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};
