/**
 * Type definitions for UIM Page Specifications
 * Maps Cúram UIM declarative structure to React-renderable specs
 * 
 * Note: These are JSDoc type hints for IDE support in JavaScript
 */

// export interface PageSpec {
  pageId: string;
  title: PageTitle;
  serverInterfaces?: ServerInterface[];
  parameters?: PageParameter[];
  content: LayoutNode[];
}

export interface PageTitle {
  text?: string;
  textKeys?: string[]; // Property keys from .properties file
}

export interface ServerInterface {
  name: string;
  className: string;
  operation: string;
  phase?: 'DISPLAY' | 'ACTION';
}

export interface PageParameter {
  name: string;
  required?: boolean;
}

export type LayoutNode = ClusterNode | ListNode | FieldNode | ContainerNode | ActionControlNode;

export interface ClusterNode {
  type: 'CLUSTER';
  numCols?: number;
  showLabels?: boolean;
  title?: string;
  style?: string;
  children: LayoutNode[];
}

export interface ListNode {
  type: 'LIST';
  title?: string;
  style?: string;
  fields: ListField[];
}

export interface ListField {
  label: string;
  width?: string;
  connect: DataConnect;
  link?: LinkSpec;
}

export interface FieldNode {
  type: 'FIELD';
  label: string;
  connect: DataConnect;
  link?: LinkSpec;
}

export interface ContainerNode {
  type: 'CONTAINER';
  children: LayoutNode[];
}

export interface ActionControlNode {
  type: 'ACTION_CONTROL';
  label: string;
  link?: LinkSpec;
  condition?: Condition;
}

export interface DataConnect {
  source: string; // SERVER_INTERFACE name
  property: string;
}

export interface LinkSpec {
  pageId: string;
  params?: LinkParam[];
}

export interface LinkParam {
  source: string; // SERVER_INTERFACE name or 'CONSTANT'
  sourceProperty: string;
  targetProperty: string;
}

export interface Condition {
  type: 'IS_TRUE' | 'IS_FALSE';
  source: string;
  property: string;
}
