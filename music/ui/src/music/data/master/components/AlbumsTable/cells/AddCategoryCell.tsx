import { useNotifications } from '@/music/shared/hooks';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { bindAlbumToCategory } from '@/music/data/master/api/music-data-albums';

interface AddCategoryCellProps {
    albumId: number;
    onAdded: () => void;
}

export const AddCategoryCell = ({ albumId, onAdded }: AddCategoryCellProps) => {
    const { showNotification } = useNotifications();

    const handleCategoryAdded = async (entity: any) => {
        try {
            await bindAlbumToCategory(albumId, entity.id);
            onAdded();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add category');
        }
    };

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <MasterEntityPicker
                entityType="category"
                buttonLabel="Add Category"
                onEntitySelected={handleCategoryAdded}
            />
        </span>
    );
};
