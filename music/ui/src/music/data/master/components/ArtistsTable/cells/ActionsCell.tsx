import { useState } from 'react';
import { useNotifications } from '@/shared/hooks';
import { ConfirmDialog } from '@/shared/components';
import { deleteArtist } from '@/music/data/master/api/music-data-artists';
import type { Artist } from '@/music/shared/types/entities';
import rowStyles from '../../ArtistsTableRow/ArtistsTableRow.module.css';

interface ActionsCellProps {
    artist: Artist;
    onDeleted: () => void;
}

export const ActionsCell = ({ artist, onDeleted }: ActionsCellProps) => {
    const { showNotification } = useNotifications();
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteArtist(artist.id);
            onDeleted();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete artist');
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
                header="Delete Artist"
                message={`Are you sure you want to delete artist "${artist.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </span>
    );
};
