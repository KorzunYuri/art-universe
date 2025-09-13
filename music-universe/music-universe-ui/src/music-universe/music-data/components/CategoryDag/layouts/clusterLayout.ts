import { type LayoutEngine, type CategoryNode, type LayoutResult } from './types';
import { type Edge } from '@xyflow/react';

interface Cluster {
    id: string;
    nodes: CategoryNode[];
    size: number;
    center: { x: number, y: number };
    radius: number;
}

export const clusterLayout: LayoutEngine = {
    name: 'Cluster Layout',
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

        // Find root nodes
        const roots = nodes.filter(n => !parents.has(n.id));
        
        // Create clusters based on root subtrees
        const clusters: Cluster[] = [];
        const visited = new Set<string>();
        
        const buildCluster = (rootId: string): string[] => {
            const clusterNodes: string[] = [];
            const queue = [rootId];
            
            while (queue.length > 0) {
                const nodeId = queue.shift()!;
                if (visited.has(nodeId)) continue;
                
                visited.add(nodeId);
                clusterNodes.push(nodeId);
                
                const nodeChildren = children.get(nodeId) || [];
                queue.push(...nodeChildren);
            }
            
            return clusterNodes;
        };

        // Build clusters from roots
        roots.forEach(root => {
            const clusterNodeIds = buildCluster(root.id);
            const clusterNodes = clusterNodeIds.map(id => nodeMap.get(id)!);
            
            clusters.push({
                id: root.id,
                nodes: clusterNodes,
                size: clusterNodes.length,
                center: { x: 0, y: 0 },
                radius: Math.sqrt(clusterNodes.length) * 80 + 100
            });
        });

        // Add remaining nodes to singleton clusters
        nodes.forEach(node => {
            if (!visited.has(node.id)) {
                clusters.push({
                    id: node.id,
                    nodes: [node],
                    size: 1,
                    center: { x: 0, y: 0 },
                    radius: 100
                });
            }
        });

        // Sort clusters by size (largest first)
        clusters.sort((a, b) => b.size - a.size);

        // Position clusters using circle packing
        positionClusters(clusters);

        // Position nodes within each cluster
        const result: CategoryNode[] = [];
        
        clusters.forEach(cluster => {
            const clusterNodes = positionNodesInCluster(cluster, children, parents);
            result.push(...clusterNodes);
        });

        return { nodes: result, edges };
    }
};

function positionClusters(clusters: Cluster[]) {
    if (clusters.length === 0) return;
    
    // Separate large and small clusters
    const largeThreshold = 5;
    const largeClusters = clusters.filter(c => c.size >= largeThreshold);
    const smallClusters = clusters.filter(c => c.size < largeThreshold);
    
    // Position large clusters in a grid with ample spacing
    const gridSpacing = 1200;
    const cols = Math.ceil(Math.sqrt(largeClusters.length));
    
    largeClusters.forEach((cluster, index) => {
        const row = Math.floor(index / cols);
        const col = index % cols;
        cluster.center = {
            x: (col - (cols - 1) / 2) * gridSpacing,
            y: row * gridSpacing
        };
    });
    
    // Position small clusters around large ones
    smallClusters.forEach((cluster, index) => {
        const angle = (index * 2 * Math.PI) / smallClusters.length;
        const radius = 600 + index * 50;
        cluster.center = {
            x: radius * Math.cos(angle),
            y: radius * Math.sin(angle)
        };
    });
}

function positionNodesInCluster(
    cluster: Cluster,
    children: Map<string, string[]>,
    parents: Map<string, string[]>
): CategoryNode[] {
    if (cluster.nodes.length === 1) {
        return [{
            ...cluster.nodes[0],
            position: cluster.center
        }];
    }
    
    // Find root of this cluster
    const clusterRoot = cluster.nodes.find(n => !parents.has(n.id));
    if (!clusterRoot) {
        // No clear root, use radial layout
        return cluster.nodes.map((node, index) => {
            const angle = (index * 2 * Math.PI) / cluster.nodes.length;
            const radius = Math.min(cluster.radius * 0.7, 150);
            
            return {
                ...node,
                position: {
                    x: cluster.center.x + radius * Math.cos(angle),
                    y: cluster.center.y + radius * Math.sin(angle)
                }
            };
        });
    }
    
    // Use hierarchical layout within cluster
    const positioned = new Map<string, { x: number, y: number }>();
    const result: CategoryNode[] = [];
    
    const positionSubtree = (nodeId: string, x: number, y: number, level: number) => {
        positioned.set(nodeId, { x, y });
        
        const nodeChildren = children.get(nodeId) || [];
        const clusterChildren = nodeChildren.filter(childId => 
            cluster.nodes.some(n => n.id === childId)
        );
        
        if (clusterChildren.length === 0) return;
        
        const childY = y + 150;
        const spacing = Math.max(200, cluster.radius * 1.5 / clusterChildren.length);
        const startX = x - (clusterChildren.length - 1) * spacing / 2;
        
        clusterChildren.forEach((childId, index) => {
            positionSubtree(childId, startX + index * spacing, childY, level + 1);
        });
    };
    
    positionSubtree(clusterRoot.id, cluster.center.x, cluster.center.y, 0);
    
    // Create result nodes
    cluster.nodes.forEach(node => {
        const pos = positioned.get(node.id) || cluster.center;
        result.push({
            ...node,
            position: pos
        });
    });
    
    return result;
}
