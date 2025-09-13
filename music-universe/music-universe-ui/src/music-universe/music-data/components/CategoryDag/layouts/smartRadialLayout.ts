import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types';
import { type Edge } from '@xyflow/react';

const MIN_DISTANCE = 200;
const LEVEL_SPACING = 300;

interface NodeInfo {
    node: CategoryNode;
    level: number;
    children: string[];
    parent?: string;
}

export const smartRadialLayout: LayoutEngine = {
    name: 'Smart Radial Layout',
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

        // Find roots and calculate levels
        const roots = nodes.filter(n => !parents.has(n.id));
        const nodeInfos = new Map<string, NodeInfo>();
        
        const calculateLevels = (nodeId: string, level: number) => {
            const node = nodeMap.get(nodeId)!;
            const nodeChildren = children.get(nodeId) || [];
            
            nodeInfos.set(nodeId, {
                node,
                level,
                children: nodeChildren,
                parent: parents.get(nodeId)
            });
            
            nodeChildren.forEach(childId => calculateLevels(childId, level + 1));
        };

        roots.forEach(root => calculateLevels(root.id, 0));

        // Group nodes by level
        const levelGroups = new Map<number, NodeInfo[]>();
        nodeInfos.forEach(info => {
            const group = levelGroups.get(info.level) || [];
            group.push(info);
            levelGroups.set(info.level, group);
        });

        // Sort nodes within each level by number of children (descending)
        levelGroups.forEach(group => {
            group.sort((a, b) => b.children.length - a.children.length);
        });

        const positioned = new Map<string, { x: number, y: number }>();
        const result: CategoryNode[] = [];

        // Position roots first
        const rootGroup = levelGroups.get(0) || [];
        if (rootGroup.length === 1) {
            positioned.set(rootGroup[0].node.id, { x: 0, y: 0 });
        } else {
            const rootSpacing = 800;
            rootGroup.forEach((info, index) => {
                const angle = (index * 2 * Math.PI) / rootGroup.length;
                const x = rootSpacing * Math.cos(angle);
                const y = rootSpacing * Math.sin(angle);
                positioned.set(info.node.id, { x, y });
            });
        }

        // Position other levels
        for (let level = 1; level <= Math.max(...levelGroups.keys()); level++) {
            const currentGroup = levelGroups.get(level) || [];
            
            currentGroup.forEach(info => {
                const parentPos = positioned.get(info.parent!);
                if (!parentPos) return;

                // Calculate siblings (nodes with same parent)
                const siblings = currentGroup.filter(n => n.parent === info.parent);
                const siblingIndex = siblings.indexOf(info);
                
                if (siblings.length === 1) {
                    // Single child - place directly below parent
                    const pos = {
                        x: parentPos.x,
                        y: parentPos.y + LEVEL_SPACING
                    };
                    positioned.set(info.node.id, pos);
                } else {
                    // Multiple siblings - arrange in arc
                    const arcAngle = Math.min(Math.PI, siblings.length * 0.4);
                    const startAngle = -arcAngle / 2;
                    const angleStep = arcAngle / (siblings.length - 1);
                    const angle = startAngle + siblingIndex * angleStep;
                    
                    // Dynamic radius based on number of siblings and their children
                    const baseRadius = LEVEL_SPACING;
                    const childrenFactor = Math.max(1, info.children.length / 5);
                    const radius = baseRadius * (1 + childrenFactor * 0.3);
                    
                    const pos = {
                        x: parentPos.x + radius * Math.sin(angle),
                        y: parentPos.y + radius * Math.cos(angle)
                    };
                    
                    // Check for collisions and adjust
                    const finalPos = findNonCollidingPosition(pos, Array.from(positioned.values()));
                    positioned.set(info.node.id, finalPos);
                }
            });
        }

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

function findNonCollidingPosition(
    preferredPos: { x: number, y: number },
    existingPositions: Array<{ x: number, y: number }>
): { x: number, y: number } {
    let pos = { ...preferredPos };
    let attempts = 0;
    const maxAttempts = 10;
    
    while (attempts < maxAttempts) {
        const hasCollision = existingPositions.some(existing => {
            const dx = pos.x - existing.x;
            const dy = pos.y - existing.y;
            return Math.sqrt(dx * dx + dy * dy) < MIN_DISTANCE;
        });
        
        if (!hasCollision) {
            return pos;
        }
        
        // Try slight adjustments
        const angle = (attempts * Math.PI * 2) / maxAttempts;
        const offset = 50 + attempts * 20;
        pos = {
            x: preferredPos.x + offset * Math.cos(angle),
            y: preferredPos.y + offset * Math.sin(angle)
        };
        
        attempts++;
    }
    
    return pos;
}
