import { useState } from 'react';
import { useNotifications } from '@/music/shared/hooks';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { unbindArtistFromCategory } from '@/music/data/master/api/music-data-artists';
import type { Artist } from '@/music/shared/types/entities';

interface CategoriesCellProps {
    artist: Artist;
    onChanged: () => void;
}

export const CategoriesCell = ({ artist, onChanged }: CategoriesCellProps) => {
    const { showNotification } = useNotifications();
    const [processingCategories, setProcessingCategories] = useState<Set<number>>(new Set());

    const handleCategoryRemoved = async (categoryId: number) => {
        setProcessingCategories(prev => new Set(prev).add(categoryId));
        try {
            await unbindArtistFromCategory(artist.id, categoryId);
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
                entities={artist.categories ?? []}
                onEntityRemoved={handleCategoryRemoved}
                processingEntities={processingCategories}
                emptyMessage="No categories"
                removeTitle="Remove category"
            />
        </span>
    );
};
