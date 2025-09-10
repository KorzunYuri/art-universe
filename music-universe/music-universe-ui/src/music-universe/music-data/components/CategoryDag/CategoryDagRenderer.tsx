import { useEffect } from 'react';
import {
    ReactFlow,
    useReactFlow,
    type Edge,
    type Connection,
    type OnNodesChange,
    type OnEdgesChange,
    Background,
    Handle,
    Position,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import styles from './CategoryDag.module.css';
import { type CategoryNode } from './layouts';

interface CategoryNodeData extends Record<string, unknown> {
    id: number;
    name: string;
    isRoot: boolean;
}

const CategoryNodeComponent = ({ data }: { data: CategoryNodeData }) => {
    return (
        <div className={`${styles.dagNode} ${data.isRoot ? styles.rootNode : ''}`}>
            <div className={styles.categoryName}>{data.name}</div>
            <Handle type="target" position={Position.Top} />
            <Handle type="source" position={Position.Bottom} />
        </div>
    );
};

// Move nodeTypes outside component to avoid recreation warning
const nodeTypes = {
    category: CategoryNodeComponent,
};

interface CategoryDagRendererProps {
    nodes: CategoryNode[];
    edges: Edge[];
    selectedEdge?: string;
    selectedNode?: string;
    isLoading?: boolean;
    onNodesChange?: OnNodesChange<CategoryNode>;
    onEdgesChange?: OnEdgesChange;
    onNodeDoubleClick?: (event: React.MouseEvent, node: CategoryNode) => void;
    onEdgeClick?: (event: React.MouseEvent, edge: Edge) => void;
    onConnect?: (connection: Connection) => void;
    readonly?: boolean;
    onLayoutChange?: () => void;
}

export function CategoryDagRenderer({
    nodes,
    edges,
    selectedEdge,
    selectedNode,
    isLoading,
    onNodesChange,
    onEdgesChange,
    onNodeDoubleClick,
    onEdgeClick,
    onConnect,
    readonly = false,
    onLayoutChange,
}: CategoryDagRendererProps) {
    const { fitView } = useReactFlow();

    // Fit view when nodes change
    useEffect(() => {
        if (nodes.length > 0) {
            setTimeout(() => {
                fitView();
                onLayoutChange?.();
            }, 100);
        }
    }, [nodes.length, fitView, onLayoutChange]);

    if (isLoading) {
        return <div className={styles.loading}>Loading category graph...</div>;
    }

    return (
        <div className={styles.dagContainer}>
            <ReactFlow
                nodes={nodes.map(node => ({
                    ...node,
                    selected: node.id === selectedNode,
                }))}
                edges={edges.map(edge => ({
                    ...edge,
                    selected: edge.id === selectedEdge,
                }))}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onNodeDoubleClick={onNodeDoubleClick}
                onEdgeClick={onEdgeClick}
                onConnect={readonly ? undefined : onConnect}
                nodeTypes={nodeTypes}
                nodesDraggable={!readonly}
                nodesConnectable={!readonly}
                elementsSelectable={!readonly}
                fitView
                attributionPosition="bottom-left"
            >
                <Background />
            </ReactFlow>
        </div>
    );
}
