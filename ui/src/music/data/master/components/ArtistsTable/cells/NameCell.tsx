import { useState, useEffect } from 'react';
import { EditableText } from '@/shared/components';
import { useNotifications } from '@/shared/hooks';
import { saveArtist, type ArtistSaveRequest } from '@/music/data/master/api/music-data-artists';
import type { Artist } from '@/music/shared/types/entities';
import rowStyles from '../../ArtistsTableRow/ArtistsTableRow.module.css';

interface NameCellProps {
    artist: Artist;
    onSaved: () => void;
}

export const NameCell = ({ artist, onSaved }: NameCellProps) => {
    const { showNotification } = useNotifications();
    const [editedName, setEditedName] = useState(artist.name);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        setEditedName(artist.name);
        setIsDirty(false);
    }, [artist.name]);

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== artist.name);
    };

    const handleSave = async (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: ArtistSaveRequest = { id: artist.id, name: editedName.trim() };
            await saveArtist(saveRequest);
            setIsDirty(false);
            onSaved();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save artist');
            setEditedName(artist.name);
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
                placeholder="Artist name"
                disabled={isSaving}
            />
            {isDirty && (
                <button
                    onClick={handleSave}
                    disabled={isSaving || !editedName.trim()}
                    className={rowStyles.saveButton}
                >
                    {isSaving ? '...' : 'Save'}
                </button>
            )}
        </span>
    );
};
