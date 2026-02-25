'use client';

import { useCallback, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import ReactFlow, {
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  MarkerType,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Loader2 } from 'lucide-react';
import { graphApi } from '@/lib/api';
import type { DependencyGraph } from '@/types';

const nodeColors: Record<string, string> = {
  ACTIVE: '#22c55e',
  INACTIVE: '#6b7280',
  ON_HOLD: '#eab308',
  ON_ICE: '#06b6d4',
};

export default function DependencyGraphPage() {
  const { data: graphData, isLoading } = useQuery<DependencyGraph>({
    queryKey: ['graph'],
    queryFn: () => graphApi.getFullGraph(),
  });

  const { nodes, edges } = useMemo(() => {
    if (!graphData) return { nodes: [], edges: [] };

    // Calculate positions using a simple layout algorithm
    const nodeMap = new Map<string, number>();
    graphData.nodes.forEach((node, index) => {
      nodeMap.set(node.id, index);
    });

    // Simple grid layout
    const cols = Math.ceil(Math.sqrt(graphData.nodes.length));
    const spacing = 200;

    const flowNodes: Node[] = graphData.nodes.map((node, index) => ({
      id: node.id,
      position: {
        x: (index % cols) * spacing + 50,
        y: Math.floor(index / cols) * spacing + 50,
      },
      data: {
        label: (
          <div className="text-center">
            <div className="font-semibold">{node.label}</div>
            {node.type && <div className="text-xs text-gray-500">{node.type}</div>}
          </div>
        ),
      },
      style: {
        backgroundColor: nodeColors[node.status || 'ACTIVE'] || '#6b7280',
        color: 'white',
        borderRadius: '8px',
        padding: '10px',
        border: node.enabled === false ? '2px dashed #9ca3af' : 'none',
        minWidth: '120px',
      },
    }));

    const flowEdges: Edge[] = graphData.edges.map((edge, index) => ({
      id: `edge-${index}`,
      source: edge.source,
      target: edge.target,
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: '#6b7280',
      },
      style: {
        stroke: '#6b7280',
        strokeWidth: 2,
      },
      animated: false,
    }));

    return { nodes: flowNodes, edges: flowEdges };
  }, [graphData]);

  const [flowNodes, setNodes, onNodesChange] = useNodesState(nodes);
  const [flowEdges, setEdges, onEdgesChange] = useEdgesState(edges);

  // Update nodes/edges when data changes
  useMemo(() => {
    setNodes(nodes);
    setEdges(edges);
  }, [nodes, edges, setNodes, setEdges]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-[600px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dependency Graph</h1>
        <p className="text-muted-foreground">Visualize job dependencies</p>
      </div>

      {/* Legend */}
      <Card>
        <CardHeader className="py-3">
          <CardTitle className="text-sm">Legend</CardTitle>
        </CardHeader>
        <CardContent className="py-2">
          <div className="flex items-center gap-6">
            {Object.entries(nodeColors).map(([status, color]) => (
              <div key={status} className="flex items-center gap-2">
                <div
                  className="w-4 h-4 rounded"
                  style={{ backgroundColor: color }}
                />
                <span className="text-sm">{status}</span>
              </div>
            ))}
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded border-2 border-dashed border-gray-400" />
              <span className="text-sm">Disabled</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Graph */}
      <Card>
        <CardContent className="p-0">
          <div style={{ height: '600px' }}>
            {flowNodes.length === 0 ? (
              <div className="flex items-center justify-center h-full text-muted-foreground">
                No jobs with dependencies found
              </div>
            ) : (
              <ReactFlow
                nodes={flowNodes}
                edges={flowEdges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                fitView
                attributionPosition="bottom-left"
              >
                <Background />
                <Controls />
                <MiniMap
                  nodeColor={(node) => node.style?.backgroundColor as string || '#6b7280'}
                  maskColor="rgba(0, 0, 0, 0.1)"
                />
              </ReactFlow>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
