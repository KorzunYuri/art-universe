import { useState } from 'react';
import { useNotifications } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components';
import { deleteCategory } from '@/music/data/master/api/music-data-categories';
import type { Category } from '@/music/shared/types/entities';
import rowStyles from '../../CategoriesTableRow/CategoriesTableRow.module.css';

interface ActionsCellProps {
    category: Category;
    onDeleted: () => void;
}

export const ActionsCell = ({ category, onDeleted }: ActionsCellProps) => {
    const { showNotification } = useNotifications();
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteCategory(category.id);
            onDeleted();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete category');
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirm(false);
        }
    };

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <button
                onClick={() => setShowDeleteConfirm(true)}
                disabled={isDeleting}
                className={rowStyles.deleteButton}
            >
                {isDeleting ? '...' : 'Delete'}
            </button>
            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Category"
                message={`Are you sure you want to delete category "${category.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </span>
    );
};
