import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types';
import { type Edge } from '@xyflow/react';

interface ClusterInfo {
    nodeId: string;
    children: ClusterInfo[];
    radius: number;
    position: { x: number, y: number };
    subtreeNodes: string[];
}

export const radialRecursiveLayout: LayoutEngine = {
    name: 'Radial Recursive Layout',
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
        rootClusters.forEach(calculateRadialSpaceRequirements);
        
        // Position root clusters optimally
        positionRootClusters(rootClusters);
        
        // Position all nodes within their clusters
        const positioned = new Map<string, { x: number, y: number }>();
        rootClusters.forEach(cluster => positionClusterNodesRadially(cluster, positioned));
        
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
            radius: 50,
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
        radius: 0, // Will be calculated
        position: { x: 0, y: 0 },
        subtreeNodes
    };
}

function calculateRadialSpaceRequirements(cluster: ClusterInfo): void {
    // First calculate space for all children
    cluster.children.forEach(calculateRadialSpaceRequirements);
    
    if (cluster.children.length === 0) {
        // Leaf node
        cluster.radius = 50;
        return;
    }
    
    // Calculate radius needed to fit all children radially
    const maxChildRadius = Math.max(...cluster.children.map(c => c.radius));
    const childrenCircumference = cluster.children.length * 200; // 200px spacing between children
    const radiusFromCircumference = childrenCircumference / (2 * Math.PI);
    
    // Radius must accommodate both child spacing and child sizes
    const requiredRadius = Math.max(
        150, // Minimum radius
        radiusFromCircumference + maxChildRadius + 100 // Child placement radius + child size + buffer
    );
    
    cluster.radius = requiredRadius;
}

function positionRootClusters(rootClusters: ClusterInfo[]): void {
    if (rootClusters.length === 0) return;
    
    // Sort by radius (largest first for better packing)
    rootClusters.sort((a, b) => b.radius - a.radius);
    
    // Simple grid layout with spacing based on cluster radius
    const cols = Math.ceil(Math.sqrt(rootClusters.length));
    
    rootClusters.forEach((cluster, index) => {
        const row = Math.floor(index / cols);
        const col = index % cols;
        
        // Calculate grid position with spacing based on cluster radius
        const spacing = cluster.radius * 2.5 + 300;
        
        cluster.position = {
            x: (col - (cols - 1) / 2) * spacing,
            y: row * spacing
        };
    });
}

function positionClusterNodesRadially(
    cluster: ClusterInfo, 
    positioned: Map<string, { x: number, y: number }>
): void {
    // Position the current node at cluster center
    positioned.set(cluster.nodeId, cluster.position);
    
    if (cluster.children.length === 0) return;
    
    // Position children radially around parent
    const childPlacementRadius = cluster.radius * 0.7; // Place children at 70% of cluster radius
    const angleStep = (2 * Math.PI) / cluster.children.length;
    
    cluster.children.forEach((child, index) => {
        const angle = index * angleStep;
        
        // Position child on circle around parent
        child.position = {
            x: cluster.position.x + childPlacementRadius * Math.cos(angle),
            y: cluster.position.y + childPlacementRadius * Math.sin(angle)
        };
        
        // Recursively position child's subtree
        positionClusterNodesRadially(child, positioned);
    });
}
