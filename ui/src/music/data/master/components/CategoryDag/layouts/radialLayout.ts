import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types.ts';
import { type Edge } from '@xyflow/react';

export const radialLayout: LayoutEngine = {
    name: 'Radial Layout',
    layout: (nodes: CategoryNode[], edges: Edge[]): LayoutResult => {
        const nodeMap = new Map(nodes.map(n => [n.id, n]));
        const children = new Map<string, string[]>();
        
        // Build parent-child relationships
        edges.forEach(edge => {
            const parentChildren = children.get(edge.source) || [];
            parentChildren.push(edge.target);
            children.set(edge.source, parentChildren);
        });
        
        const positioned = new Set<string>();
        const result: CategoryNode[] = [];
        
        const positionNode = (nodeId: string, centerX: number, centerY: number, radius: number) => {
            if (positioned.has(nodeId)) return;
            
            const node = nodeMap.get(nodeId)!;
            const nodeChildren = children.get(nodeId) || [];
            
            // Position current node
            result.push({
                ...node,
                position: { x: centerX, y: centerY }
            });
            positioned.add(nodeId);
            
            // Position children in circle around parent
            if (nodeChildren.length > 0) {
                const angleStep = (2 * Math.PI) / nodeChildren.length;
                // Increase base radius and scale with number of children
                const childRadius = Math.max(200, nodeChildren.length * 25);
                
                nodeChildren.forEach((childId, index) => {
                    const angle = index * angleStep;
                    const childX = centerX + childRadius * Math.cos(angle);
                    const childY = centerY + childRadius * Math.sin(angle);
                    positionNode(childId, childX, childY, childRadius * 0.8);
                });
            }
        };
        
        // Find root nodes and position them
        const roots = nodes.filter(n => !edges.some(e => e.target === n.id));
        const rootSpacing = 600;
        
        roots.forEach((root, index) => {
            const rootX = (index % 3) * rootSpacing;
            const rootY = Math.floor(index / 3) * rootSpacing;
            positionNode(root.id, rootX, rootY, 200);
        });
        
        return { nodes: result, edges };
    }
};
