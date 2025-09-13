import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types';
import { type Edge } from '@xyflow/react';

const MIN_DISTANCE = 250;
const LEVEL_SPACING = 500;
const SUPERHUB_THRESHOLD = 15;
const MEDIUM_HUB_THRESHOLD = 3;

interface NodeInfo {
    node: CategoryNode;
    level: number;
    children: string[];
    parents: string[];
    primaryParent?: string;
}

export const hybridLayout: LayoutEngine = {
    name: 'Hybrid Layout',
    layout: (nodes: CategoryNode[], edges: Edge[]): LayoutResult => {
        const nodeMap = new Map(nodes.map(n => [n.id, n]));
        const children = new Map<string, string[]>();
        const parents = new Map<string, string[]>();
        
        // Build relationships
        edges.forEach(edge => {
            const parentChildren = children.get(edge.source) || [];
            parentChildren.push(edge.target);
            children.set(edge.source, parentChildren);
            
            const childParents = parents.get(edge.target) || [];
            childParents.push(edge.source);
            parents.set(edge.target, childParents);
        });

        // Find roots and calculate levels
        const roots = nodes.filter(n => !parents.has(n.id));
        const nodeInfos = new Map<string, NodeInfo>();
        
        const calculateLevels = (nodeId: string, level: number, primaryParent?: string) => {
            if (nodeInfos.has(nodeId)) {
                // Handle diamond inheritance - keep shortest path
                const existing = nodeInfos.get(nodeId)!;
                if (level < existing.level) {
                    existing.level = level;
                    existing.primaryParent = primaryParent;
                }
                return;
            }
            
            const node = nodeMap.get(nodeId)!;
            const nodeChildren = children.get(nodeId) || [];
            const nodeParents = parents.get(nodeId) || [];
            
            nodeInfos.set(nodeId, {
                node,
                level,
                children: nodeChildren,
                parents: nodeParents,
                primaryParent
            });
            
            nodeChildren.forEach(childId => calculateLevels(childId, level + 1, nodeId));
        };

        roots.forEach(root => calculateLevels(root.id, 0));

        // Group nodes by level
        const levelGroups = new Map<number, NodeInfo[]>();
        nodeInfos.forEach(info => {
            const group = levelGroups.get(info.level) || [];
            group.push(info);
            levelGroups.set(info.level, group);
        });

        const positioned = new Map<string, { x: number, y: number }>();
        const result: CategoryNode[] = [];

        // Position roots
        const rootGroup = levelGroups.get(0) || [];
        if (rootGroup.length === 1) {
            positioned.set(rootGroup[0].node.id, { x: 0, y: 0 });
        } else {
            rootGroup.forEach((info, index) => {
                const x = (index - (rootGroup.length - 1) / 2) * 800;
                positioned.set(info.node.id, { x, y: 0 });
            });
        }

        // Position other levels
        for (let level = 1; level <= Math.max(...levelGroups.keys()); level++) {
            const currentGroup = levelGroups.get(level) || [];
            
            currentGroup.forEach(info => {
                const primaryParentPos = positioned.get(info.primaryParent!);
                if (!primaryParentPos) return;

                const siblings = currentGroup.filter(n => n.primaryParent === info.primaryParent);
                const siblingIndex = siblings.indexOf(info);
                const parentChildrenCount = info.parents.length > 0 ? 
                    (children.get(info.primaryParent!) || []).length : 0;

                let pos: { x: number, y: number };

                if (parentChildrenCount >= SUPERHUB_THRESHOLD) {
                    // Radial layout for superhubs
                    pos = positionRadially(primaryParentPos, siblings, siblingIndex, level);
                } else if (parentChildrenCount >= MEDIUM_HUB_THRESHOLD) {
                    // Horizontal spread for medium hubs
                    pos = positionHorizontally(primaryParentPos, siblings, siblingIndex, level);
                } else {
                    // Vertical for small hubs
                    pos = positionVertically(primaryParentPos, siblings, siblingIndex, level);
                }

                // Handle diamond inheritance offset
                if (info.parents.length > 1) {
                    pos.x += (info.parents.length - 1) * 30;
                }

                // Collision detection
                pos = findNonCollidingPosition(pos, Array.from(positioned.values()));
                positioned.set(info.node.id, pos);
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

function positionRadially(
    parentPos: { x: number, y: number },
    siblings: NodeInfo[],
    siblingIndex: number,
    level: number
): { x: number, y: number } {
    const radius = 200 + siblings.length * 15;
    const angleStep = (2 * Math.PI) / siblings.length;
    const angle = siblingIndex * angleStep;
    
    return {
        x: parentPos.x + radius * Math.cos(angle),
        y: parentPos.y + radius * Math.sin(angle)
    };
}

function positionHorizontally(
    parentPos: { x: number, y: number },
    siblings: NodeInfo[],
    siblingIndex: number,
    level: number
): { x: number, y: number } {
    const spacing = Math.max(200, siblings.length * 30);
    const totalWidth = (siblings.length - 1) * spacing;
    const startX = parentPos.x - totalWidth / 2;
    
    return {
        x: startX + siblingIndex * spacing,
        y: parentPos.y + LEVEL_SPACING
    };
}

function positionVertically(
    parentPos: { x: number, y: number },
    siblings: NodeInfo[],
    siblingIndex: number,
    level: number
): { x: number, y: number } {
    const offset = (siblingIndex - (siblings.length - 1) / 2) * 150;
    
    return {
        x: parentPos.x + offset,
        y: parentPos.y + LEVEL_SPACING
    };
}

function findNonCollidingPosition(
    preferredPos: { x: number, y: number },
    existingPositions: Array<{ x: number, y: number }>
): { x: number, y: number } {
    let pos = { ...preferredPos };
    let attempts = 0;
    const maxAttempts = 8;
    
    while (attempts < maxAttempts) {
        const hasCollision = existingPositions.some(existing => {
            const dx = pos.x - existing.x;
            const dy = pos.y - existing.y;
            return Math.sqrt(dx * dx + dy * dy) < MIN_DISTANCE;
        });
        
        if (!hasCollision) return pos;
        
        const angle = (attempts * Math.PI * 2) / maxAttempts;
        const offset = 60 + attempts * 25;
        pos = {
            x: preferredPos.x + offset * Math.cos(angle),
            y: preferredPos.y + offset * Math.sin(angle)
        };
        
        attempts++;
    }
    
    return pos;
}
