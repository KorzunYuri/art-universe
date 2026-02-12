import { useState } from 'react';
import { useNotifications } from '@/music/shared/hooks';
import { ConfirmDialog } from '@/music/shared/components';
import { deleteAlbum } from '@/music/data/master/api/music-data-albums';
import type { Album } from '@/music/shared/types/entities';
import styles from '../AlbumsTable.module.css';

interface ActionsCellProps {
    album: Album;
    onDeleted: () => void;
}

export const ActionsCell = ({ album, onDeleted }: ActionsCellProps) => {
    const { showNotification } = useNotifications();
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteAlbum(album.id);
            onDeleted();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete album');
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
                className={styles.deleteButton}
            >
                {isDeleting ? '...' : 'Delete'}
            </button>
            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Album"
                message={`Are you sure you want to delete album "${album.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </span>
    );
};
