import { useCallback } from 'react';
import { type Node, type Edge, ReactFlowProvider } from '@xyflow/react';
import { CategoryDagInteractive } from './CategoryDagInteractive.tsx';
import type { CategoryNodeData } from './layouts/types.ts';

type CategoryNode = Node<CategoryNodeData>;

interface CategoryDagReadonlyProps {
    nodes: CategoryNode[];
    edges: Edge[];
    isLoading?: boolean;
    onCategorySelect?: (categoryId: number, categoryData: CategoryNodeData) => void;
    selectedCategories?: number[];
}

function CategoryDagReadonlyFlow({
    nodes,
    edges,
    isLoading,
    onCategorySelect,
}: CategoryDagReadonlyProps) {
    const handleNodeDoubleClick = useCallback((nodeId: string, nodeData: CategoryNodeData) => {
        if (onCategorySelect) {
            onCategorySelect(nodeData.id, nodeData);
        }
    }, [onCategorySelect]);

    return (
        <CategoryDagInteractive
            nodes={nodes}
            edges={edges}
            isLoading={isLoading}
            readonly={true}
            onNodeDoubleClick={handleNodeDoubleClick}
            allowEdgeCreation={false}
            allowEdgeDeletion={false}
        />
    );
}

export const CategoryDagReadonly = (props: CategoryDagReadonlyProps) => {
    return (
        <ReactFlowProvider>
            <CategoryDagReadonlyFlow {...props} />
        </ReactFlowProvider>
    );
};
