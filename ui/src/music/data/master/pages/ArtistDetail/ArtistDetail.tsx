import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNotifications } from '@/shared/hooks';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { EditableText, ConfirmDialog } from '@/shared/components';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { RelatedEntitiesSection } from '@/music/data/master/components/RelatedEntitiesSection';
import { useArtistWithCategories } from '@/music/data/master/hooks/useArtistWithCategories';
import {
    saveArtist,
    deleteArtist,
    bindArtistToCategory,
    unbindArtistFromCategory,
    type ArtistSaveRequest,
} from '@/music/data/master/api/music-data-artists';
import styles from '@/music/data/master/styles/MasterDetailPage.module.scss';

export const ArtistDetail = () => {
    const { artistId } = useParams<{ artistId: string }>();
    const id = Number(artistId);
    const { showNotification } = useNotifications();
    const permissions = usePermissions();
    const readOnly = permissions.masterEntityAccess !== 'full';

    const {
        artist,
        invalidateArtist,
        isLoading,
        isError,
        error,
    } = useArtistWithCategories(id);

    const [editedName, setEditedName] = useState('');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [processingCategories, setProcessingCategories] = useState<Set<number>>(new Set());

    useEffect(() => {
        if (artist) {
            setEditedName(artist.name);
            setIsDirty(false);
        }
    }, [artist]);

    if (isLoading) {
        return <div className={styles.loading}>Loading artist...</div>;
    }

    if (isError || !artist) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Artist not found'}
            </div>
        );
    }

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== artist.name);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: ArtistSaveRequest = { id: artist.id, name: editedName.trim() };
            await saveArtist(saveRequest);
            setIsDirty(false);
            invalidateArtist();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save artist');
            setEditedName(artist.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteArtist(artist.id);
            // Navigation back will happen via the parent closing the panel
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete artist');
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirm(false);
        }
    };

    const handleCategoryAdded = async (entity: any) => {
        try {
            await bindArtistToCategory(artist.id, entity.id);
            invalidateArtist();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add category');
        }
    };

    const handleCategoryRemoved = async (categoryId: number) => {
        setProcessingCategories(prev => new Set(prev).add(categoryId));
        try {
            await unbindArtistFromCategory(artist.id, categoryId);
            invalidateArtist();
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

    return (
        <div className={styles.detail}>
            {/* Header: name + actions */}
            <div className={styles.header}>
                {readOnly ? (
                    <h3 className={styles.nameInput}>{artist.name}</h3>
                ) : (
                    <EditableText
                        value={editedName}
                        onChange={handleNameChange}
                        placeholder="Artist name"
                        disabled={isSaving}
                        className={styles.nameInput}
                    />
                )}
                {!readOnly && (
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
                )}
            </div>

            {/* Categories */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Categories</h3>
                <MasterEntityPanel
                    entities={artist.categories ?? []}
                    onEntityRemoved={handleCategoryRemoved}
                    processingEntities={processingCategories}
                    emptyMessage="No categories"
                    removeTitle="Remove category"
                    readOnly={readOnly}
                />
                {!readOnly && (
                    <div className={styles.addCategory}>
                        <MasterEntityPicker
                            entityType="category"
                            buttonLabel="Add Category"
                            onEntitySelected={handleCategoryAdded}
                        />
                    </div>
                )}
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
                    sourceEntityType="artist"
                    sourceEntityId={artist.id}
                    targetEntityType="artist"
                    readOnly={readOnly}
                />
                <RelatedEntitiesSection
                    title="Related Albums"
                    sourceEntityType="artist"
                    sourceEntityId={artist.id}
                    targetEntityType="album"
                    readOnly={readOnly}
                />
                <RelatedEntitiesSection
                    title="Related Tracks"
                    sourceEntityType="artist"
                    sourceEntityId={artist.id}
                    targetEntityType="track"
                    readOnly={readOnly}
                />
                <RelatedEntitiesSection
                    title="Related Persons"
                    sourceEntityType="artist"
                    sourceEntityId={artist.id}
                    targetEntityType="person"
                    readOnly={readOnly}
                />
            </section>

            {!readOnly && (
                <ConfirmDialog
                    isOpen={showDeleteConfirm}
                    header="Delete Artist"
                    message={`Are you sure you want to delete artist "${artist.name}"?`}
                    onConfirm={handleDelete}
                    onCancel={() => setShowDeleteConfirm(false)}
                />
            )}
        </div>
    );
};
