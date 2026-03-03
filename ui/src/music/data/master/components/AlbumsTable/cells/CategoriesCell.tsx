import { useState } from 'react';
import { useNotifications } from '@/shared/hooks';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { unbindAlbumFromCategory } from '@/music/data/master/api/music-data-albums';
import type { Album } from '@/music/shared/types/entities';

interface CategoriesCellProps {
    album: Album;
    onChanged: () => void;
}

export const CategoriesCell = ({ album, onChanged }: CategoriesCellProps) => {
    const { showNotification } = useNotifications();
    const [processingCategories, setProcessingCategories] = useState<Set<number>>(new Set());

    const handleCategoryRemoved = async (categoryId: number) => {
        setProcessingCategories(prev => new Set(prev).add(categoryId));
        try {
            await unbindAlbumFromCategory(album.id, categoryId);
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
                entities={album.categories ?? []}
                onEntityRemoved={handleCategoryRemoved}
                processingEntities={processingCategories}
                emptyMessage="No categories"
                removeTitle="Remove category"
            />
        </span>
    );
};
