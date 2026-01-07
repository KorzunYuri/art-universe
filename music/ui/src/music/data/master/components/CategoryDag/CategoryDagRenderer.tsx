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
    Position, MiniMap,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import styles from './CategoryDag.module.css';
import { type CategoryNode, type CategoryNodeData } from './layouts';

const CategoryNodeComponent = ({ data }: { data: CategoryNodeData }) => {
    const getRankStyle = (rank: number = 1) => {
        const baseSize = 12;
        const sizeMultiplier = 1 + (rank - 1) * 0.3; // 1.0 to 2.2
        const opacity = 0.5 + (rank - 1) * 0.15; // 0.4 to 1.0
        
        return {
            containerStyle: {
                fontWeight: rank >= 4 ? 'bold' : 'normal',
                backgroundColor: `rgba(0, 200, 125, ${opacity})`,
                minWidth: `${80 + (rank - 1) * 20}px`,
                minHeight: `${40 + (rank - 1) * 10}px`,
                borderWidth: `${Math.max(1, rank)}px`,
            },
            nameStyle: {
                fontSize: `${baseSize * sizeMultiplier}px`,
            },
            valueStyle: {
                fontSize: `${baseSize * sizeMultiplier * 0.75}px`,
            }
        };
    };

    const { containerStyle, nameStyle, valueStyle } = getRankStyle(data.rank);

    return (
        <div 
            className={`${styles.dagNode} ${data.isRoot ? styles.rootNode : ''}`}
            style={containerStyle}
        >
            <div className={styles.categoryName} style={nameStyle}>{data.name}</div>
            {data.value !== undefined && (
                <div className={styles.categoryValue} style={valueStyle}>({data.value})</div>
            )}
            <Handle type="target" position={Position.Top} className={styles.inputHandle} />
            <Handle type="source" position={Position.Bottom} className={styles.outputHandle} />
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
                connectionRadius={20}
                fitView
                attributionPosition="bottom-left"
            >
                <MiniMap />
                <Background />
            </ReactFlow>
        </div>
    );
}
