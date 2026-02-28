import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useNotifications } from '@/shared/hooks';
import { EditableText, ConfirmDialog } from '@/shared/components';
import { MasterEntityPanel } from '@/music/data/master/components/MasterEntityPanel';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { RelatedEntitiesSection } from '@/music/data/master/components/RelatedEntitiesSection';
import type { RelatedEntityDTO } from '@/music/data/master/api/music-data-relations';
import { useMasterEntity } from '@/music/data/master/hooks/useMasterEntity';
import { useAlbumWithCategories } from '@/music/data/master/hooks/useAlbumWithCategories';
import {
    saveAlbum,
    deleteAlbum,
    bindAlbumToCategory,
    unbindAlbumFromCategory,
    copyAlbumTracklist,
    reorderAlbumTracks,
    type AlbumSaveRequest,
} from '@/music/data/master/api/music-data-albums';
import { relationKeys } from '@/music/shared/utils/query-keys';
import styles from '@/music/data/master/styles/MasterDetailPage.module.scss';

export const AlbumDetail = () => {
    const { albumId } = useParams<{ albumId: string }>();
    const id = Number(albumId);
    const { showNotification } = useNotifications();
    const queryClient = useQueryClient();

    const {
        album,
        invalidateAlbum,
        isLoading,
        isError,
        error,
    } = useAlbumWithCategories(id);

    // Fetch primary artist name
    const { entity: primaryArtist } = useMasterEntity(
        'artist',
        album?.primaryArtistId ?? 0
    );

    const [editedName, setEditedName] = useState('');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [processingCategories, setProcessingCategories] = useState<Set<number>>(new Set());
    const [copyingAlbumIds, setCopyingAlbumIds] = useState<Set<number>>(new Set());
    const [swappingTrackRelationIds, setSwappingTrackRelationIds] = useState<Set<number>>(new Set());

    useEffect(() => {
        if (album) {
            setEditedName(album.name);
            setIsDirty(false);
        }
    }, [album]);

    if (isLoading) {
        return <div className={styles.loading}>Loading album...</div>;
    }

    if (isError || !album) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Album not found'}
            </div>
        );
    }

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== album.name);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: AlbumSaveRequest = { id: album.id, name: editedName.trim() };
            await saveAlbum(saveRequest);
            setIsDirty(false);
            invalidateAlbum();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save album');
            setEditedName(album.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleCategoryAdded = async (entity: any) => {
        try {
            await bindAlbumToCategory(album.id, entity.id);
            invalidateAlbum();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add category');
        }
    };

    const handleCategoryRemoved = async (categoryId: number) => {
        setProcessingCategories(prev => new Set(prev).add(categoryId));
        try {
            await unbindAlbumFromCategory(album.id, categoryId);
            invalidateAlbum();
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

    const handleSwapTracks = async (entityA: RelatedEntityDTO, entityB: RelatedEntityDTO) => {
        setSwappingTrackRelationIds(prev => new Set([...prev, entityA.relationId, entityB.relationId]));
        try {
            await reorderAlbumTracks(album.id, [
                { albumTrackId: entityA.relationId, newOrder: entityB.trackOrder! },
                { albumTrackId: entityB.relationId, newOrder: entityA.trackOrder! },
            ]);
            queryClient.invalidateQueries({
                queryKey: relationKeys.entities('album', album.id, 'track'),
            });
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to reorder tracks');
        } finally {
            setSwappingTrackRelationIds(prev => {
                const next = new Set(prev);
                next.delete(entityA.relationId);
                next.delete(entityB.relationId);
                return next;
            });
        }
    };

    const handleCopyTracklist = async (entity: RelatedEntityDTO) => {
        setCopyingAlbumIds(prev => new Set(prev).add(entity.id));
        try {
            const result = await copyAlbumTracklist(album.id, entity.id);
            queryClient.invalidateQueries({
                queryKey: relationKeys.entities('album', album.id, 'track'),
            });
            showNotification('success', `Copied ${result.copiedCount} track(s) from "${entity.name}"`);
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to copy tracklist');
        } finally {
            setCopyingAlbumIds(prev => { const next = new Set(prev); next.delete(entity.id); return next; });
        }
    };

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteAlbum(album.id);
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete album');
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
                    placeholder="Album name"
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
                    {primaryArtist?.name ?? `Artist #${album.primaryArtistId}`}
                </div>
            </section>

            {/* Categories */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Categories</h3>
                <MasterEntityPanel
                    entities={album.categories ?? []}
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
                    sourceEntityType="album"
                    sourceEntityId={album.id}
                    targetEntityType="artist"
                />
                <RelatedEntitiesSection
                    title="Related Albums"
                    sourceEntityType="album"
                    sourceEntityId={album.id}
                    targetEntityType="album"
                    onEntityAction={handleCopyTracklist}
                    entityActionLabel="Copy tracklist"
                    actionInProgressIds={copyingAlbumIds}
                />
                <RelatedEntitiesSection
                    title="Related Tracks"
                    sourceEntityType="album"
                    sourceEntityId={album.id}
                    targetEntityType="track"
                    onEntitySwap={handleSwapTracks}
                    swappingRelationIds={swappingTrackRelationIds}
                />
                <RelatedEntitiesSection
                    title="Related Persons"
                    sourceEntityType="album"
                    sourceEntityId={album.id}
                    targetEntityType="person"
                />
            </section>

            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Album"
                message={`Are you sure you want to delete album "${album.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </div>
    );
};
