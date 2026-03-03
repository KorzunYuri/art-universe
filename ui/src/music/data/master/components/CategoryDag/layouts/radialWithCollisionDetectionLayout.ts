import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types.ts';
import { type Edge } from '@xyflow/react';

const MIN_DISTANCE = 180; // Minimum distance between nodes

const checkCollision = (x1: number, y1: number, x2: number, y2: number): boolean => {
    const dx = x1 - x2;
    const dy = y1 - y2;
    const distance = Math.sqrt(dx * dx + dy * dy);
    return distance < MIN_DISTANCE;
};

const findNonCollidingPosition = (
    centerX: number, 
    centerY: number, 
    radius: number, 
    angle: number, 
    existingPositions: Array<{x: number, y: number}>
): {x: number, y: number} => {
    let currentRadius = radius;
    let attempts = 0;
    const maxAttempts = 20;
    
    while (attempts < maxAttempts) {
        const x = centerX + currentRadius * Math.cos(angle);
        const y = centerY + currentRadius * Math.sin(angle);
        
        const hasCollision = existingPositions.some(pos => 
            checkCollision(x, y, pos.x, pos.y)
        );
        
        if (!hasCollision) {
            return { x, y };
        }
        
        // Try increasing radius
        currentRadius += 30;
        attempts++;
    }
    
    // Fallback to original position if no solution found
    return {
        x: centerX + radius * Math.cos(angle),
        y: centerY + radius * Math.sin(angle)
    };
};

export const radialWithCollisionDetectionLayout: LayoutEngine = {
    name: 'Improved Radial Layout',
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
        const allPositions: Array<{x: number, y: number}> = [];
        
        const positionNode = (nodeId: string, centerX: number, centerY: number, baseRadius: number) => {
            if (positioned.has(nodeId)) return;
            
            const node = nodeMap.get(nodeId)!;
            const nodeChildren = children.get(nodeId) || [];
            
            // Position current node
            const nodePosition = { x: centerX, y: centerY };
            result.push({
                ...node,
                position: nodePosition
            });
            positioned.add(nodeId);
            allPositions.push(nodePosition);
            
            // Position children in circle around parent
            if (nodeChildren.length > 0) {
                const angleStep = (2 * Math.PI) / nodeChildren.length;
                // Dynamic radius based on number of children
                const childRadius = Math.max(250, nodeChildren.length * 30);
                
                nodeChildren.forEach((childId, index) => {
                    const angle = index * angleStep;
                    const childPos = findNonCollidingPosition(
                        centerX, 
                        centerY, 
                        childRadius, 
                        angle, 
                        allPositions
                    );
                    
                    positionNode(childId, childPos.x, childPos.y, childRadius * 0.7);
                });
            }
        };
        
        // Find root nodes and position them with more spacing
        const roots = nodes.filter(n => !edges.some(e => e.target === n.id));
        const rootSpacing = 800;
        
        roots.forEach((root, index) => {
            const rootX = (index % 2) * rootSpacing - (roots.length > 1 ? rootSpacing / 2 : 0);
            const rootY = Math.floor(index / 2) * rootSpacing;
            positionNode(root.id, rootX, rootY, 300);
        });
        
        return { nodes: result, edges };
    }
};
