import { useCallback, useState } from 'react';
import { type Node, type Edge, ReactFlowProvider, useReactFlow } from '@xyflow/react';
import { useQuery } from '@tanstack/react-query';
import { fetchCategoryDag, saveCategory } from '@/music-universe/music-data/api/music-data-categories';
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
    
    const { data: dagData, isLoading } = useQuery({
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
        const sourceIdNum = parseInt(sourceId);
        const targetIdNum = parseInt(targetId);
        const targetNode = nodes.find(n => n.id === targetId);

        try {
            await saveCategory({
                id: targetIdNum,
                name: targetNode?.data.name || '',
                parentId: sourceIdNum,
            });
        } catch (error) {
            console.error('Failed to create category relation:', error);
        }
    }, [nodes]);

    const handleEdgeDelete = useCallback(async (edgeId: string) => {
        const [, targetId] = edgeId.split('-').map(Number);
        const targetNode = nodes.find(n => n.id === targetId.toString());

        try {
            await saveCategory({
                id: targetId,
                name: targetNode?.data.name || '',
                parentId: null,
            });
        } catch (error) {
            console.error('Failed to remove category relation:', error);
        }
    }, [nodes]);

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
