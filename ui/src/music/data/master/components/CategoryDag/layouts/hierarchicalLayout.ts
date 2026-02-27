import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types.ts';
import { type Edge } from '@xyflow/react';

export const hierarchicalLayout: LayoutEngine = {
    name: 'Hierarchical Layout',
    layout: (nodes: CategoryNode[], edges: Edge[]): LayoutResult => {
        const nodeMap = new Map(nodes.map(n => [n.id, n]));
        const children = new Map<string, string[]>();
        const parents = new Map<string, string>();
        
        // Build relationships
        edges.forEach(edge => {
            const parentChildren = children.get(edge.source) || [];
            parentChildren.push(edge.target);
            children.set(edge.source, parentChildren);
            parents.set(edge.target, edge.source);
        });

        // Find roots and calculate subtree sizes
        const roots = nodes.filter(n => !parents.has(n.id));
        const subtreeSizes = new Map<string, number>();
        
        const calculateSubtreeSize = (nodeId: string): number => {
            const nodeChildren = children.get(nodeId) || [];
            let size = 1;
            nodeChildren.forEach(childId => {
                size += calculateSubtreeSize(childId);
            });
            subtreeSizes.set(nodeId, size);
            return size;
        };

        roots.forEach(root => calculateSubtreeSize(root.id));

        const positioned = new Map<string, { x: number, y: number }>();
        const result: CategoryNode[] = [];
        
        // Position nodes using hierarchical approach
        const positionSubtree = (
            nodeId: string, 
            centerX: number, 
            y: number, 
            availableWidth: number
        ) => {
            const nodeChildren = children.get(nodeId) || [];
            
            // Position current node
            positioned.set(nodeId, { x: centerX, y });
            
            if (nodeChildren.length === 0) return;
            
            // Sort children by subtree size (largest first)
            const sortedChildren = nodeChildren.sort((a, b) => 
                (subtreeSizes.get(b) || 0) - (subtreeSizes.get(a) || 0)
            );
            
            // Calculate positions for children
            const childY = y + 300;
            const totalChildrenSize = sortedChildren.reduce((sum, childId) => 
                sum + (subtreeSizes.get(childId) || 1), 0
            );
            
            let currentX = centerX - (availableWidth / 2);
            
            sortedChildren.forEach(childId => {
                const childSize = subtreeSizes.get(childId) || 1;
                const childWidth = (childSize / totalChildrenSize) * availableWidth;
                const childCenterX = currentX + childWidth / 2;
                
                positionSubtree(childId, childCenterX, childY, childWidth * 0.9);
                currentX += childWidth;
            });
        };

        // Position each root tree
        const rootSpacing = 1000;
        roots.forEach((root, index) => {
            const rootX = index * rootSpacing;
            const rootSize = subtreeSizes.get(root.id) || 1;
            const treeWidth = Math.max(800, rootSize * 100);
            
            positionSubtree(root.id, rootX, 0, treeWidth);
        });

        // Create result nodes
        positioned.forEach((pos, nodeId) => {
            const node = nodeMap.get(nodeId)!;
            result.push({
                ...node,
                position: pos
            });
        });

        return { nodes: result, edges };
    }
};
