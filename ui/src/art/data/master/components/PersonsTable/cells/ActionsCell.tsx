import { useState } from 'react';
import { useNotifications } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components';
import { deletePerson, type PersonDto } from '@/art/data/master/api/art-data-persons';
import rowStyles from '../../PersonsTableRow/PersonsTableRow.module.css';

interface ActionsCellProps {
    person: PersonDto;
    onDeleted: () => void;
}

export const ActionsCell = ({ person, onDeleted }: ActionsCellProps) => {
    const { showNotification } = useNotifications();
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deletePerson(person.id);
            onDeleted();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete person');
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
                header="Delete Person"
                message={`Are you sure you want to delete person "${person.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </span>
    );
};
