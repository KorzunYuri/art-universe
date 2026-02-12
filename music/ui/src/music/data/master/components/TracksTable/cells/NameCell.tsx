import { useState, useEffect } from 'react';
import { EditableText } from '@/music/shared/components';
import { useNotifications } from '@/music/shared/hooks';
import { saveTrack, type TrackSaveRequest } from '@/music/data/master/api/music-data-tracks';
import type { Track } from '@/music/shared/types/entities';
import styles from '../TracksTable.module.css';

interface NameCellProps {
    track: Track;
    onSaved: () => void;
}

export const NameCell = ({ track, onSaved }: NameCellProps) => {
    const { showNotification } = useNotifications();
    const [editedName, setEditedName] = useState(track.name);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        setEditedName(track.name);
        setIsDirty(false);
    }, [track.name]);

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== track.name);
    };

    const handleSave = async (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: TrackSaveRequest = { id: track.id, name: editedName.trim() };
            await saveTrack(saveRequest);
            setIsDirty(false);
            onSaved();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save track');
            setEditedName(track.name);
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
                placeholder="Track name"
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
