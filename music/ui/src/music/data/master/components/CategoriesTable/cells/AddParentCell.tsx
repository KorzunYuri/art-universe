import { useNotifications } from '@/music/shared/hooks';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { createCategoryRelation } from '@/music/data/master/api/music-data-categories';

interface AddParentCellProps {
    categoryId: number;
    onAdded: () => void;
}

export const AddParentCell = ({ categoryId, onAdded }: AddParentCellProps) => {
    const { showNotification } = useNotifications();

    const handleParentAdded = async (entity: any) => {
        try {
            await createCategoryRelation({ sourceId: entity.id, targetId: categoryId });
            onAdded();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add parent');
        }
    };

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <MasterEntityPicker
                entityType="category"
                buttonLabel="Add Parent"
                onEntitySelected={handleParentAdded}
            />
        </span>
    );
};
