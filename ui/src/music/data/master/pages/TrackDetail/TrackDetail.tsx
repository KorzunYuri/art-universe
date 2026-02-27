import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNotifications } from '@/shared/hooks';
import { EditableText, ConfirmDialog } from '@/shared/components';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { RelatedEntitiesSection } from '@/music/data/master/components/RelatedEntitiesSection';
import { useMasterEntity } from '@/music/data/master/hooks/useMasterEntity';
import { useTrackWithCategories } from '@/music/data/master/hooks/useTrackWithCategories';
import {
    saveTrack,
    deleteTrack,
    bindTrackToCategory,
    unbindTrackFromCategory,
    type TrackSaveRequest,
} from '@/music/data/master/api/music-data-tracks';
import styles from '@/music/data/master/styles/MasterDetailPage.module.scss';

export const TrackDetail = () => {
    const { trackId } = useParams<{ trackId: string }>();
    const id = Number(trackId);
    const { showNotification } = useNotifications();

    const {
        track,
        invalidateTrack,
        isLoading,
        isError,
        error,
    } = useTrackWithCategories(id);

    // Fetch primary artist name
    const { entity: primaryArtist } = useMasterEntity(
        'artist',
        track?.primaryArtistId ?? 0
    );

    const [editedName, setEditedName] = useState('');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [processingCategories, setProcessingCategories] = useState<Set<number>>(new Set());

    useEffect(() => {
        if (track) {
            setEditedName(track.name);
            setIsDirty(false);
        }
    }, [track]);

    if (isLoading) {
        return <div className={styles.loading}>Loading track...</div>;
    }

    if (isError || !track) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Track not found'}
            </div>
        );
    }

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== track.name);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: TrackSaveRequest = { id: track.id, name: editedName.trim() };
            await saveTrack(saveRequest);
            setIsDirty(false);
            invalidateTrack();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save track');
            setEditedName(track.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleCategoryAdded = async (entity: any) => {
        try {
            await bindTrackToCategory(track.id, entity.id);
            invalidateTrack();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add category');
        }
    };

    const handleCategoryRemoved = async (categoryId: number) => {
        setProcessingCategories(prev => new Set(prev).add(categoryId));
        try {
            await unbindTrackFromCategory(track.id, categoryId);
            invalidateTrack();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove category');
        } finally {
            setProcessingCategories(prev => {
                const next = new Set(prev);
                next.delete(categoryId);
                return next;
            });
        }
    };

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteTrack(track.id);
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete track');
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirm(false);
        }
    };

    return (
        <div className={styles.detail}>
            {/* Header: name + actions */}
            <div className={styles.header}>
                <EditableText
                    value={editedName}
                    onChange={handleNameChange}
                    placeholder="Track name"
                    disabled={isSaving}
                    className={styles.nameInput}
                />
                <div className={styles.actions}>
                    <button
                        onClick={handleSave}
                        disabled={!isDirty || isSaving || !editedName.trim()}
                        className={styles.saveButton}
                    >
                        {isSaving ? '...' : 'Save'}
                    </button>
                    <button
                        onClick={() => setShowDeleteConfirm(true)}
                        disabled={isDeleting}
                        className={styles.deleteButton}
                    >
                        {isDeleting ? '...' : 'Delete'}
                    </button>
                </div>
            </div>

            {/* Primary Artist */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Primary Artist</h3>
                <div className={styles.primaryArtist}>
                    {primaryArtist?.name ?? `Artist #${track.primaryArtistId}`}
                </div>
            </section>

            {/* Categories */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Categories</h3>
                <MasterEntityPanel
                    entities={track.categories ?? []}
                    onEntityRemoved={handleCategoryRemoved}
                    processingEntities={processingCategories}
                    emptyMessage="No categories"
                    removeTitle="Remove category"
                />
                <div className={styles.addCategory}>
                    <MasterEntityPicker
                        entityType="category"
                        buttonLabel="Add Category"
                        onEntitySelected={handleCategoryAdded}
                    />
                </div>
            </section>

            {/* Attributes stub */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Attributes</h3>
                <div className={styles.stub}>Attributes coming soon</div>
            </section>

            {/* Relations */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Relations</h3>
                <RelatedEntitiesSection
                    title="Related Artists"
                    sourceEntityType="track"
                    sourceEntityId={track.id}
                    targetEntityType="artist"
                />
                <RelatedEntitiesSection
                    title="Related Albums"
                    sourceEntityType="track"
                    sourceEntityId={track.id}
                    targetEntityType="album"
                />
                <RelatedEntitiesSection
                    title="Related Tracks"
                    sourceEntityType="track"
                    sourceEntityId={track.id}
                    targetEntityType="track"
                />
            </section>

            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Track"
                message={`Are you sure you want to delete track "${track.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </div>
    );
};
