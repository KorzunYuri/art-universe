import { useCallback, useEffect, useState } from 'react';
import {
    type Edge,
    type Connection,
    type NodeChange,
    type EdgeChange,
    applyNodeChanges,
    applyEdgeChanges
} from '@xyflow/react';
import { CategoryDagRenderer } from './CategoryDagRenderer';
import { treeLayout, type LayoutEngine, type CategoryNode } from './layouts';
import type {CategoryNodeData} from "@/music-universe/music-data/components/CategoryDag/layouts/types.ts";

interface CategoryDagInteractiveProps {
    nodes: CategoryNode[];
    edges: Edge[];
    isLoading?: boolean;
    readonly?: boolean;
    layoutEngine?: LayoutEngine;
    
    // Node events
    onNodeDoubleClick?: (nodeId: string, nodeData: CategoryNodeData) => void;
    
    // Edge events
    onEdgeClick?: (edgeId: string) => void;
    onEdgeCreate?: (sourceId: string, targetId: string) => void;
    onEdgeDelete?: (edgeId: string) => void;
    
    // Settings
    allowEdgeCreation?: boolean;
    allowEdgeDeletion?: boolean;
    autoArrange?: boolean;
    onLayoutChange?: () => void;
}

export function CategoryDagInteractive({
    nodes: initialNodes,
    edges: initialEdges,
    isLoading,
    readonly = false,
    layoutEngine = treeLayout,
    onNodeDoubleClick,
    onEdgeClick,
    onEdgeCreate,
    onEdgeDelete,
    allowEdgeCreation = true,
    allowEdgeDeletion = true,
    autoArrange = true,
    onLayoutChange,
}: CategoryDagInteractiveProps) {
    const [nodes, setNodes] = useState<CategoryNode[]>(initialNodes);
    const [edges, setEdges] = useState<Edge[]>(initialEdges);
    const [selectedEdge, setSelectedEdge] = useState<string | null>(null);
    const [selectedNode, setSelectedNode] = useState<string | null>(null);

    // Update internal state when props change
    useEffect(() => {
        if (initialNodes.length > 0 && autoArrange) {
            const { nodes: layoutedNodes } = layoutEngine.layout(initialNodes, initialEdges);
            setNodes(layoutedNodes);
        } else {
            setNodes(initialNodes);
        }
    }, [initialNodes, autoArrange, layoutEngine]);

    useEffect(() => {
        setEdges(initialEdges);
    }, [initialEdges]);

    const handleNodesChange = useCallback((changes: NodeChange[]) => {
        setNodes((nds) => {
            const updatedNodes = applyNodeChanges(changes, nds) as CategoryNode[];
            
            // Auto-arrange after node removal if autoArrange is true
            if (autoArrange && changes.some(change => change.type === 'remove')) {
                const { nodes: layoutedNodes } = layoutEngine.layout(updatedNodes, edges);
                return layoutedNodes;
            }
            
            return updatedNodes;
        });
        
        // Track node selection - find the last selected node
        const selectChanges = changes.filter(change => change.type === 'select');
        if (selectChanges.length > 0) {
            const selectedNode = selectChanges.find(change => change.selected);
            const newSelectedNodeId = selectedNode ? selectedNode.id : null;
            setSelectedNode(newSelectedNodeId);
        }
    }, [autoArrange, edges, layoutEngine]);

    const handleEdgesChange = useCallback((changes: EdgeChange[]) => {
        setEdges((eds) => applyEdgeChanges(changes, eds));
        
        // Clear selection if edge is removed
        changes.forEach((change) => {
            if (change.type === 'remove' && change.id === selectedEdge) {
                setSelectedEdge(null);
            }
        });
    }, [selectedEdge]);

    const handleNodeDoubleClick = useCallback((event: React.MouseEvent, node: CategoryNode) => {
        if (onNodeDoubleClick) {
            onNodeDoubleClick(node.id, node.data);
        }
    }, [onNodeDoubleClick]);

    const handleEdgeClick = useCallback((event: React.MouseEvent, edge: Edge) => {
        if (!readonly) {
            setSelectedEdge(edge.id);
        }
        if (onEdgeClick) {
            onEdgeClick(edge.id);
        }
    }, [readonly, onEdgeClick]);

    const handleConnect = useCallback((params: Connection) => {
        if (!allowEdgeCreation || readonly || !params.source || !params.target) return;
        
        // Add edge to local state immediately
        const newEdge: Edge = {
            id: `${params.source}-${params.target}`,
            source: params.source,
            target: params.target,
            type: 'straight',
        };
        
        setEdges((eds) => [...eds, newEdge]);
        
        if (onEdgeCreate) {
            onEdgeCreate(params.source, params.target);
        }
    }, [allowEdgeCreation, readonly, onEdgeCreate]);

    const handleKeyDown = useCallback((event: KeyboardEvent) => {
        if ((event.key === 'Delete' || event.key === 'Backspace') && !readonly) {
            if (selectedEdge && allowEdgeDeletion) {
                if (onEdgeDelete) {
                    onEdgeDelete(selectedEdge);
                }
                setSelectedEdge(null);
            }
        }
    }, [selectedEdge, allowEdgeDeletion, readonly, onEdgeDelete]);

    useEffect(() => {
        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [handleKeyDown]);

    return (
        <CategoryDagRenderer
            nodes={nodes}
            edges={edges}
            selectedEdge={selectedEdge || undefined}
            selectedNode={selectedNode || undefined}
            isLoading={isLoading}
            onNodesChange={handleNodesChange}
            onEdgesChange={handleEdgesChange}
            onNodeDoubleClick={handleNodeDoubleClick}
            onEdgeClick={handleEdgeClick}
            onConnect={handleConnect}
            readonly={readonly}
            onLayoutChange={onLayoutChange}
        />
    );
}
