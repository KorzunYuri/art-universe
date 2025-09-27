import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types.ts';
import { type Edge } from '@xyflow/react';

interface ClusterInfo {
    nodeId: string;
    children: ClusterInfo[];
    radius: number;
    position: { x: number, y: number };
    subtreeNodes: string[];
    isStyleCluster: boolean;
}

export const stylePriorityLayout: LayoutEngine = {
    name: 'Style Priority Layout',
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

        // Find style root (category named "style")
        const styleRoot = nodes.find(n => nodeMap.get(n.id)?.data.name.toLowerCase() === 'style');
        const styleRootId = styleRoot?.id;

        // Find all roots
        const roots = nodes.filter(n => !parents.has(n.id));
        
        // Build cluster hierarchy for each root
        const rootClusters: ClusterInfo[] = roots.map(root => 
            buildClusterHierarchy(root.id, children, new Set(), styleRootId)
        );
        
        // Calculate space requirements
        rootClusters.forEach(calculateRadialSpaceRequirements);
        
        // Position root clusters with style priority
        positionRootClustersWithStylePriority(rootClusters);
        
        // Position nodes with style priority for multi-parent nodes
        const positioned = new Map<string, { x: number, y: number }>();
        rootClusters.forEach(cluster => positionClusterNodesWithStylePriority(
            cluster, positioned, parents, rootClusters, styleRootId
        ));
        
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
    visited: Set<string>,
    styleRootId?: string
): ClusterInfo {
    if (visited.has(nodeId)) {
        return {
            nodeId,
            children: [],
            radius: 50,
            position: { x: 0, y: 0 },
            subtreeNodes: [nodeId],
            isStyleCluster: nodeId === styleRootId
        };
    }
    
    visited.add(nodeId);
    const nodeChildren = children.get(nodeId) || [];
    
    const childClusters = nodeChildren.map(childId => 
        buildClusterHierarchy(childId, children, visited, styleRootId)
    );
    
    const subtreeNodes = [nodeId, ...childClusters.flatMap(c => c.subtreeNodes)];
    const isStyleCluster = nodeId === styleRootId || childClusters.some(c => c.isStyleCluster);
    
    return {
        nodeId,
        children: childClusters,
        radius: 0,
        position: { x: 0, y: 0 },
        subtreeNodes,
        isStyleCluster
    };
}

function calculateRadialSpaceRequirements(cluster: ClusterInfo): void {
    cluster.children.forEach(calculateRadialSpaceRequirements);
    
    if (cluster.children.length === 0) {
        cluster.radius = 50;
        return;
    }
    
    const maxChildRadius = Math.max(...cluster.children.map(c => c.radius));
    const childrenCircumference = cluster.children.length * 200;
    const radiusFromCircumference = childrenCircumference / (2 * Math.PI);
    
    cluster.radius = Math.max(150, radiusFromCircumference + maxChildRadius + 100);
}

function positionRootClustersWithStylePriority(rootClusters: ClusterInfo[]): void {
    if (rootClusters.length === 0) return;
    
    // Separate style and non-style clusters
    const styleClusters = rootClusters.filter(c => c.isStyleCluster);
    const otherClusters = rootClusters.filter(c => !c.isStyleCluster);
    
    // Place style cluster(s) at center
    styleClusters.forEach((cluster, index) => {
        cluster.position = {
            x: index * 400, // If multiple style clusters, spread horizontally
            y: 0
        };
    });
    
    // Place other clusters around style clusters
    otherClusters.forEach((cluster, index) => {
        const angle = (index * 2 * Math.PI) / otherClusters.length;
        const distance = 1000 + cluster.radius;
        
        cluster.position = {
            x: distance * Math.cos(angle),
            y: distance * Math.sin(angle)
        };
    });
}

function positionClusterNodesWithStylePriority(
    cluster: ClusterInfo,
    positioned: Map<string, { x: number, y: number }>,
    parents: Map<string, string[]>,
    allClusters: ClusterInfo[],
    styleRootId?: string
): void {
    // Position the current node at cluster center
    positioned.set(cluster.nodeId, cluster.position);
    
    if (cluster.children.length === 0) return;
    
    // Position children radially
    const childPlacementRadius = cluster.radius * 0.7;
    const angleStep = (2 * Math.PI) / cluster.children.length;
    
    cluster.children.forEach((child, index) => {
        const angle = index * angleStep;
        
        // Check if this child has multiple parents
        const childParents = parents.get(child.nodeId) || [];
        
        if (childParents.length > 1) {
            // Multi-parent node - try to position closer to style cluster
            const styleParent = findStyleParent(child.nodeId, parents, styleRootId);
            const styleCluster = allClusters.find(c => c.isStyleCluster);
            
            if (styleParent && styleCluster) {
                // Position closer to style cluster
                const directionToStyle = {
                    x: styleCluster.position.x - cluster.position.x,
                    y: styleCluster.position.y - cluster.position.y
                };
                const distance = Math.sqrt(directionToStyle.x ** 2 + directionToStyle.y ** 2);
                
                if (distance > 0) {
                    const normalizedDirection = {
                        x: directionToStyle.x / distance,
                        y: directionToStyle.y / distance
                    };
                    
                    // Position child towards style cluster
                    child.position = {
                        x: cluster.position.x + childPlacementRadius * normalizedDirection.x,
                        y: cluster.position.y + childPlacementRadius * normalizedDirection.y
                    };
                } else {
                    // Fallback to radial positioning
                    child.position = {
                        x: cluster.position.x + childPlacementRadius * Math.cos(angle),
                        y: cluster.position.y + childPlacementRadius * Math.sin(angle)
                    };
                }
            } else {
                // No style parent found, use regular radial positioning
                child.position = {
                    x: cluster.position.x + childPlacementRadius * Math.cos(angle),
                    y: cluster.position.y + childPlacementRadius * Math.sin(angle)
                };
            }
        } else {
            // Single parent - use regular radial positioning
            child.position = {
                x: cluster.position.x + childPlacementRadius * Math.cos(angle),
                y: cluster.position.y + childPlacementRadius * Math.sin(angle)
            };
        }
        
        // Recursively position child's subtree
        positionClusterNodesWithStylePriority(child, positioned, parents, allClusters, styleRootId);
    });
}

function findStyleParent(nodeId: string, parents: Map<string, string[]>, styleRootId?: string): string | null {
    if (!styleRootId) return null;
    
    const nodeParents = parents.get(nodeId) || [];
    
    // Check if any parent is in the style subtree
    for (const parentId of nodeParents) {
        if (isInStyleSubtree(parentId, parents, styleRootId)) {
            return parentId;
        }
    }
    
    return null;
}

function isInStyleSubtree(nodeId: string, parents: Map<string, string[]>, styleRootId: string): boolean {
    if (nodeId === styleRootId) return true;
    
    const nodeParents = parents.get(nodeId) || [];
    return nodeParents.some(parentId => isInStyleSubtree(parentId, parents, styleRootId));
}
