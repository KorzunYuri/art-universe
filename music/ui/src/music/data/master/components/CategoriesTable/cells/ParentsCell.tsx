import { useState } from 'react';
import { useNotifications } from '@/shared/hooks';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { deleteCategoryRelation } from '@/music/data/master/api/music-data-categories';
import type { Category } from '@/music/shared/types/entities';

interface ParentsCellProps {
    category: Category;
    onChanged: () => void;
}

export const ParentsCell = ({ category, onChanged }: ParentsCellProps) => {
    const { showNotification } = useNotifications();
    const [processingParents, setProcessingParents] = useState<Set<number>>(new Set());

    const handleParentRemoved = async (parentId: number) => {
        setProcessingParents(prev => new Set(prev).add(parentId));
        try {
            await deleteCategoryRelation({ sourceId: parentId, targetId: category.id });
            onChanged();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove parent');
        } finally {
            setProcessingParents(prev => {
                const next = new Set(prev);
                next.delete(parentId);
                return next;
            });
        }
    };

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <MasterEntityPanel
                entities={category.parents ?? []}
                onEntityRemoved={handleParentRemoved}
                processingEntities={processingParents}
                emptyMessage="No parents"
                removeTitle="Remove parent"
            />
        </span>
    );
};
