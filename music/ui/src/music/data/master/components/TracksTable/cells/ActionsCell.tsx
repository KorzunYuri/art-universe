import { useState } from 'react';
import { useNotifications } from '@/music/shared/hooks';
import { ConfirmDialog } from '@/music/shared/components';
import { deleteTrack } from '@/music/data/master/api/music-data-tracks';
import type { Track } from '@/music/shared/types/entities';
import styles from '../TracksTable.module.css';

interface ActionsCellProps {
    track: Track;
    onDeleted: () => void;
}

export const ActionsCell = ({ track, onDeleted }: ActionsCellProps) => {
    const { showNotification } = useNotifications();
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteTrack(track.id);
            onDeleted();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete track');
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
                header="Delete Track"
                message={`Are you sure you want to delete track "${track.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </span>
    );
};
