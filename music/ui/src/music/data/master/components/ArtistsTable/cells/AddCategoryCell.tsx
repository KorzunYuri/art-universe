import { useNotifications } from '@/shared/hooks';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { bindArtistToCategory } from '@/music/data/master/api/music-data-artists';

interface AddCategoryCellProps {
    artistId: number;
    onAdded: () => void;
}

export const AddCategoryCell = ({ artistId, onAdded }: AddCategoryCellProps) => {
    const { showNotification } = useNotifications();

    const handleCategoryAdded = async (entity: any) => {
        try {
            await bindArtistToCategory(artistId, entity.id);
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
