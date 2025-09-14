import { useState, useEffect } from "react";
import { useNotifications } from "@/music-universe/shared/hooks";
import { EditableText, ConfirmDialog, type BaseEntityTableRow } from "@/music-universe/shared/components";
import { MasterEntityPanel } from "../MasterEntityPanel";
import { MasterEntityPicker } from "../MasterEntityPicker";
import { useMasterEntity } from "@/music-universe/music-data/hooks/useMasterEntity";
import { saveArtist, deleteArtist, type ArtistSaveRequest } from "@/music-universe/music-data/api/music-data-artists";
import styles from "./ArtistsTableRow.module.css";
import tableStyles from "../ArtistsTable/ArtistsTable.module.css";
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";

interface ArtistsTableRowProps extends BaseEntityTableRow {
    onDeleted?: () => void;
}

export const ArtistsTableRow = ({ entityId, onDeleted }: ArtistsTableRowProps) => {
    const { showNotification } = useNotifications();
    const {
        entity: artist,
        invalidateEntity,
        isLoading: isLoadingArtist,
        isError,
        error
    } = useMasterEntity('artist', entityId);

    const [editedName, setEditedName] = useState('');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [processingCategories] = useState<Set<number>>(new Set());

    useEffect(() => {
        if (artist) {
            setEditedName(artist.name);
        }
    }, [artist]);

    if (isLoadingArtist) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${tableStyles.name}`}>
                    Loading...
                </div>
            </div>
        );
    }

    if (!artist) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${tableStyles.name}`}>
                    {isError && error ? error.message : 'No artist found'}
                </div>
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
            const saveRequest: ArtistSaveRequest = {
                id: artist.id,
                name: editedName.trim()
            };

            const savedArtist = await saveArtist(saveRequest);
            if (savedArtist) {
                console.log('✅ Artist saved successfully:', savedArtist.id);
                setIsDirty(false);
                invalidateEntity();
            } else {
                console.error('❌ Failed to save artist');
                setEditedName(artist.name);
                setIsDirty(false);
            }
        } catch (error: any) {
            console.error('❌ Error saving artist:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save artist');
            setEditedName(artist.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleCategoryRemoved = (categoryId: number) => {
        console.log(`Category ${categoryId} removed from artist ${artist.id}`);
        // TODO: Implement when backend endpoint is ready
    };

    const handleCategoryAdded = () => {
        console.log(`Category added to artist ${artist.id}`);
        // TODO: Implement when backend endpoint is ready
    };

    const handleDelete = async () => {
        if (!artist || isDeleting) return;

        setIsDeleting(true);
        try {
            const deleted = await deleteArtist(artist.id);
            if (deleted) {
                console.log('✅ Artist deleted successfully:', artist.id);
                onDeleted?.();
            } else {
                console.error('❌ Failed to delete artist');
            }
        } catch (error: any) {
            console.error('❌ Error deleting artist:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete artist');
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirm(false);
        }
    };

    return (
        <div className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell} ${tableStyles.name}`}>
                <EditableText
                    value={editedName}
                    onChange={handleNameChange}
                    placeholder="Artist name"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tableStyles.categories}`}>
                <MasterEntityPanel
                    entities={[]} // TODO: Load from backend when endpoint is ready
                    onEntityRemoved={handleCategoryRemoved}
                    processingEntities={processingCategories}
                    emptyMessage="No categories"
                    removeTitle="Remove category"
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tableStyles.addCategory}`}>
                <MasterEntityPicker
                    entityType="category"
                    buttonLabel="Add Category"
                    onEntitySelected={handleCategoryAdded}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tableStyles.actions}`}>
                <button
                    onClick={handleSave}
                    disabled={!isDirty || isSaving || !editedName.trim()}
                    className={styles.saveButton}
                >
                    {isSaving ? "..." : "Save"}
                </button>
                <button
                    onClick={() => setShowDeleteConfirm(true)}
                    disabled={isDeleting}
                    className={styles.deleteButton}
                >
                    {isDeleting ? "..." : "Delete"}
                </button>
            </div>

            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Artist"
                message={`Are you sure you want to delete artist "${artist.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </div>
    );
};
