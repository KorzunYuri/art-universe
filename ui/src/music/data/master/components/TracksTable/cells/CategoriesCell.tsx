import { useState } from 'react';
import { useNotifications } from '@/shared/hooks';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { unbindTrackFromCategory } from '@/music/data/master/api/music-data-tracks';
import type { Track } from '@/music/shared/types/entities';

interface CategoriesCellProps {
    track: Track;
    onChanged: () => void;
    readOnly?: boolean;
}

export const CategoriesCell = ({ track, onChanged, readOnly = false }: CategoriesCellProps) => {
    const { showNotification } = useNotifications();
    const [processingCategories, setProcessingCategories] = useState<Set<number>>(new Set());

    const handleCategoryRemoved = async (categoryId: number) => {
        setProcessingCategories(prev => new Set(prev).add(categoryId));
        try {
            await unbindTrackFromCategory(track.id, categoryId);
            onChanged();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove category');
        } finally {
            setProcessingCategories(prev => {
                const next = new Set(prev);
                next.delete(categoryId);
                return next;
            });
        }
    };

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <MasterEntityPanel
                entities={track.categories ?? []}
                onEntityRemoved={readOnly ? undefined : handleCategoryRemoved}
                processingEntities={processingCategories}
                emptyMessage="No categories"
                removeTitle="Remove category"
                readOnly={readOnly}
            />
        </span>
    );
};
