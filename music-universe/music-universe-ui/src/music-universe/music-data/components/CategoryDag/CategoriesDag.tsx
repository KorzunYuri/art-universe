import { useCallback, useState } from 'react';
import { type Node, type Edge, ReactFlowProvider, useReactFlow } from '@xyflow/react';
import { useQuery } from '@tanstack/react-query';
import { useNotifications } from '@/music-universe/shared/hooks';
import { fetchCategoryDag, createCategoryRelation, deleteCategoryRelation } from '@/music-universe/music-data/api/music-data-categories';
import { CategoryDagInteractive } from './CategoryDagInteractive';
import { treeLayout, radialLayout, radialWithCollisionDetectionLayout, type LayoutEngine } from './layouts';

interface CategoryNodeData extends Record<string, unknown> {
    id: number;
    name: string;
    isRoot: boolean;
}

type CategoryNode = Node<CategoryNodeData>;

function CategoriesDagFlow() {
    const [layoutEngine, setLayoutEngine] = useState<LayoutEngine>(radialWithCollisionDetectionLayout);
    const { fitView } = useReactFlow();
    const { showNotification } = useNotifications();

    const { data: dagData, isLoading, refetch } = useQuery({
        queryKey: ['categoryDag'],
        queryFn: fetchCategoryDag,
    });

    const nodes: CategoryNode[] = dagData?.nodes.map((node) => ({
        id: node.id.toString(),
        type: 'category',
        position: { x: 0, y: 0 },
        data: {
            id: node.id,
            name: node.name,
            isRoot: node.isRoot,
        },
    })) || [];

    const edges: Edge[] = dagData?.edges.map((edge) => ({
        id: `${edge.source}-${edge.target}`,
        source: edge.source.toString(),
        target: edge.target.toString(),
        type: 'straight',
    })) || [];

    const handleEdgeCreate = useCallback(async (sourceId: string, targetId: string) => {
        try {
            await createCategoryRelation({
                sourceId: parseInt(sourceId),
                targetId: parseInt(targetId)
            });
            
            // Refresh data only after successful creation
            await refetch();
            console.log(`✅ Successfully created relation: ${sourceId} -> ${targetId}`);
        } catch (error: any) {
            console.error('Failed to create category relation:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create relation');
        }
    }, [refetch, showNotification]);

    const handleEdgeDelete = useCallback(async (edgeId: string) => {
        const [sourceId, targetId] = edgeId.split('-').map(Number);

        try {
            await deleteCategoryRelation({
                sourceId,
                targetId
            });
            
            // Refresh data only after successful deletion
            await refetch();
            console.log(`✅ Successfully deleted relation: ${sourceId} -> ${targetId}`);
        } catch (error: any) {
            console.error('Failed to remove category relation:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove relation');
        }
    }, [refetch, showNotification]);

    const handleLayoutChange = useCallback(() => {
        setTimeout(() => fitView(), 150);
    }, [fitView]);

    return (
        <div>
            <div style={{ marginBottom: '10px' }}>
                <button 
                    onClick={() => setLayoutEngine(radialWithCollisionDetectionLayout)}
                    disabled={layoutEngine === radialWithCollisionDetectionLayout}
                >
                    Radial+
                </button>
                <button 
                    onClick={() => setLayoutEngine(radialLayout)}
                    disabled={layoutEngine === radialLayout}
                    style={{ marginLeft: '10px' }}
                >
                    Radial
                </button>
                <button 
                    onClick={() => setLayoutEngine(treeLayout)}
                    disabled={layoutEngine === treeLayout}
                    style={{ marginLeft: '10px' }}
                >
                    Tree
                </button>
            </div>
            <CategoryDagInteractive
                nodes={nodes}
                edges={edges}
                isLoading={isLoading}
                layoutEngine={layoutEngine}
                onEdgeCreate={handleEdgeCreate}
                onEdgeDelete={handleEdgeDelete}
                onLayoutChange={handleLayoutChange}
                autoArrange={true}
            />
        </div>
    );
}

export const CategoriesDag = () => {
    return (
        <ReactFlowProvider>
            <CategoriesDagFlow />
        </ReactFlowProvider>
    );
};
