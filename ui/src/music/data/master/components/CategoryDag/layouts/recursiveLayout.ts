import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types.ts';
import { type Edge } from '@xyflow/react';

interface ClusterInfo {
    nodeId: string;
    children: ClusterInfo[];
    width: number;
    height: number;
    position: { x: number, y: number };
    subtreeNodes: string[];
}

export const recursiveLayout: LayoutEngine = {
    name: 'Recursive Layout',
    layout: (nodes: CategoryNode[], edges: Edge[]): LayoutResult => {
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

        // Find roots
        const roots = nodes.filter(n => !parents.has(n.id));
        
        // Build cluster hierarchy for each root
        const rootClusters: ClusterInfo[] = roots.map(root => 
            buildClusterHierarchy(root.id, children, new Set())
        );
        
        // Calculate space requirements bottom-up
        rootClusters.forEach(calculateSpaceRequirements);
        
        // Position root clusters optimally
        positionRootClusters(rootClusters);
        
        // Position all nodes within their clusters
        const positioned = new Map<string, { x: number, y: number }>();
        rootClusters.forEach(cluster => positionClusterNodes(cluster, positioned));
        
        // Create result
        const result: CategoryNode[] = nodes.map(node => ({
            ...node,
            position: positioned.get(node.id) || { x: 0, y: 0 }
        }));

        return { nodes: result, edges };
    }
};

function buildClusterHierarchy(
    nodeId: string, 
    children: Map<string, string[]>,
    visited: Set<string>
): ClusterInfo {
    if (visited.has(nodeId)) {
        return {
            nodeId,
            children: [],
            width: 150,
            height: 50,
            position: { x: 0, y: 0 },
            subtreeNodes: [nodeId]
        };
    }
    
    visited.add(nodeId);
    const nodeChildren = children.get(nodeId) || [];
    
    const childClusters = nodeChildren.map(childId => 
        buildClusterHierarchy(childId, children, visited)
    );
    
    const subtreeNodes = [nodeId, ...childClusters.flatMap(c => c.subtreeNodes)];
    
    return {
        nodeId,
        children: childClusters,
        width: 0, // Will be calculated
        height: 0, // Will be calculated
        position: { x: 0, y: 0 },
        subtreeNodes
    };
}

function calculateSpaceRequirements(cluster: ClusterInfo): void {
    // First calculate space for all children
    cluster.children.forEach(calculateSpaceRequirements);
    
    if (cluster.children.length === 0) {
        // Leaf node
        cluster.width = 150;
        cluster.height = 50;
        return;
    }
    
    // Calculate required width to fit all children horizontally
    const childrenTotalWidth = cluster.children.reduce((sum, child) => sum + child.width, 0);
    const childrenSpacing = (cluster.children.length - 1) * 100; // 100px spacing between children
    const requiredWidth = Math.max(200, childrenTotalWidth + childrenSpacing);
    
    // Calculate required height (parent + level spacing + max child height)
    const maxChildHeight = Math.max(...cluster.children.map(c => c.height));
    const requiredHeight = 50 + 150 + maxChildHeight; // node height + level spacing + child height
    
    cluster.width = requiredWidth;
    cluster.height = requiredHeight;
}

function positionRootClusters(rootClusters: ClusterInfo[]): void {
    if (rootClusters.length === 0) return;
    
    // Sort by width (largest first for better packing)
    rootClusters.sort((a, b) => b.width - a.width);
    
    // Simple grid layout with adequate spacing
    const cols = Math.ceil(Math.sqrt(rootClusters.length));
    
    rootClusters.forEach((cluster, index) => {
        const row = Math.floor(index / cols);
        const col = index % cols;
        
        // Calculate grid position with spacing based on cluster size
        const baseSpacing = 800;
        const extraSpacing = cluster.width * 0.3;
        const totalSpacing = baseSpacing + extraSpacing;
        
        cluster.position = {
            x: (col - (cols - 1) / 2) * totalSpacing,
            y: row * totalSpacing
        };
    });
}

function positionClusterNodes(
    cluster: ClusterInfo, 
    positioned: Map<string, { x: number, y: number }>
): void {
    // Position the current node at cluster center
    positioned.set(cluster.nodeId, cluster.position);
    
    if (cluster.children.length === 0) return;
    
    // Position children horizontally below parent
    const childrenTotalWidth = cluster.children.reduce((sum, child) => sum + child.width, 0);
    const childrenSpacing = (cluster.children.length - 1) * 100;
    const totalWidth = childrenTotalWidth + childrenSpacing;
    
    let currentX = cluster.position.x - totalWidth / 2;
    const childY = cluster.position.y + 150;
    
    cluster.children.forEach(child => {
        // Position child at the start of its allocated space
        child.position = {
            x: currentX + child.width / 2,
            y: childY
        };
        
        // Recursively position child's subtree
        positionClusterNodes(child, positioned);
        
        // Move to next child position
        currentX += child.width + 100;
    });
}
