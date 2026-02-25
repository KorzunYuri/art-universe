import { useNotifications } from '@/music/shared/hooks';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { bindTrackToCategory } from '@/music/data/master/api/music-data-tracks';

interface AddCategoryCellProps {
    trackId: number;
    onAdded: () => void;
}

export const AddCategoryCell = ({ trackId, onAdded }: AddCategoryCellProps) => {
    const { showNotification } = useNotifications();

    const handleCategoryAdded = async (entity: any) => {
        try {
            await bindTrackToCategory(trackId, entity.id);
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
