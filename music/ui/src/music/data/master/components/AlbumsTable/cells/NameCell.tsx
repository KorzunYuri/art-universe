import { useState, useEffect } from 'react';
import { EditableText } from '@/shared/components';
import { useNotifications } from '@/shared/hooks';
import { saveAlbum, type AlbumSaveRequest } from '@/music/data/master/api/music-data-albums';
import type { Album } from '@/music/shared/types/entities';
import styles from '../AlbumsTable.module.css';

interface NameCellProps {
    album: Album;
    onSaved: () => void;
}

export const NameCell = ({ album, onSaved }: NameCellProps) => {
    const { showNotification } = useNotifications();
    const [editedName, setEditedName] = useState(album.name);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        setEditedName(album.name);
        setIsDirty(false);
    }, [album.name]);

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== album.name);
    };

    const handleSave = async (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: AlbumSaveRequest = { id: album.id, name: editedName.trim() };
            await saveAlbum(saveRequest);
            setIsDirty(false);
            onSaved();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save album');
            setEditedName(album.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }} onClick={(e) => e.stopPropagation()}>
            <EditableText
                value={editedName}
                onChange={handleNameChange}
                placeholder="Album name"
                disabled={isSaving}
            />
            {isDirty && (
                <button
                    onClick={handleSave}
                    disabled={isSaving || !editedName.trim()}
                    className={styles.saveButton}
                >
                    {isSaving ? '...' : 'Save'}
                </button>
            )}
        </span>
    );
};
