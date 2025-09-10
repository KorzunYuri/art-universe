import dagre from 'dagre';
import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types';
import { type Edge } from '@xyflow/react';

export const treeLayout: LayoutEngine = {
    name: 'Tree Layout',
    layout: (nodes: CategoryNode[], edges: Edge[]): LayoutResult => {
        const dagreGraph = new dagre.graphlib.Graph();
        dagreGraph.setDefaultEdgeLabel(() => ({}));
        dagreGraph.setGraph({ rankdir: 'TB' });

        nodes.forEach((node) => {
            dagreGraph.setNode(node.id, { width: 150, height: 50 });
        });

        edges.forEach((edge) => {
            dagreGraph.setEdge(edge.source, edge.target);
        });

        dagre.layout(dagreGraph);

        const layoutedNodes = nodes.map((node) => {
            const nodeWithPosition = dagreGraph.node(node.id);
            return {
                ...node,
                position: {
                    x: nodeWithPosition.x - 75,
                    y: nodeWithPosition.y - 25,
                },
            };
        });

        return { nodes: layoutedNodes, edges };
    }
};
