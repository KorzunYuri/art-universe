import { useCallback, useState } from 'react';
import { type Edge, ReactFlowProvider, useReactFlow } from '@xyflow/react';
import { useQuery } from '@tanstack/react-query';
import { useNotifications } from '@/music-universe/shared/hooks';
import { fetchCategoryDag, createCategoryRelation, deleteCategoryRelation } from '@/music-universe/music-data/api/music-data-categories';
import { CategoryDagInteractive } from './CategoryDagInteractive';
import { treeLayout, radialLayout, radialWithCollisionDetectionLayout, type LayoutEngine, type CategoryNode } from './layouts';
import { rankCategories, type MetricType } from './utils/categoryRanking';

function CategoriesDagFlow() {
    const [layoutEngine, setLayoutEngine] = useState<LayoutEngine>(radialWithCollisionDetectionLayout);
    const [selectedMetric, setSelectedMetric] = useState<MetricType>('childrenCount');
    const { fitView } = useReactFlow();
    const { showNotification } = useNotifications();

    const { data: dagData, isLoading, refetch } = useQuery({
        queryKey: ['categoryDag'],
        queryFn: fetchCategoryDag,
    });

    const rankedCategories = dagData ? rankCategories(
        dagData.nodes.map(node => ({
            id: node.id,
            name: node.name,
            isRoot: node.isRoot,
            metrics: {
                childrenCount: node.childrenCount,
                artistsCount: node.artistsCount,
                tracksCount: node.tracksCount,
            }
        })),
        selectedMetric
    ) : [];

    const nodes: CategoryNode[] = rankedCategories.map((category) => ({
        id: category.id.toString(),
        type: 'category',
        position: { x: 0, y: 0 },
        data: {
            id: category.id,
            name: category.name,
            isRoot: category.isRoot,
            childrenCount: category.metrics.childrenCount,
            artistsCount: category.metrics.artistsCount,
            tracksCount: category.metrics.tracksCount,
            rank: category.rank,
            value: category.value,
        },
    }));

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
            <div style={{ marginBottom: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                    <strong>Layout:</strong>
                    <button 
                        onClick={() => setLayoutEngine(radialWithCollisionDetectionLayout)}
                        disabled={layoutEngine === radialWithCollisionDetectionLayout}
                        style={{ marginLeft: '5px' }}
                    >
                        Radial+
                    </button>
                    <button 
                        onClick={() => setLayoutEngine(radialLayout)}
                        disabled={layoutEngine === radialLayout}
                        style={{ marginLeft: '5px' }}
                    >
                        Radial
                    </button>
                    <button 
                        onClick={() => setLayoutEngine(treeLayout)}
                        disabled={layoutEngine === treeLayout}
                        style={{ marginLeft: '5px' }}
                    >
                        Tree
                    </button>
                </div>
                
                <div>
                    <strong>Ranking:</strong>
                    <button
                        onClick={() => setSelectedMetric('childrenCount')}
                        disabled={selectedMetric === 'childrenCount'}
                        style={{ marginLeft: '5px' }}
                    >
                        Children
                    </button>
                    <button
                        onClick={() => setSelectedMetric('artistsCount')}
                        disabled={selectedMetric === 'artistsCount'}
                        style={{ marginLeft: '5px' }}
                    >
                        Artists
                    </button>
                    <button
                        onClick={() => setSelectedMetric('tracksCount')}
                        disabled={selectedMetric === 'tracksCount'}
                        style={{ marginLeft: '5px' }}
                    >
                        Tracks
                    </button>
                </div>
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
                key={`${layoutEngine.name}-${selectedMetric}`}
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
