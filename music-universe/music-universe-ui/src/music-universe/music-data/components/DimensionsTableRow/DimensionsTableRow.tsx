// hooks
import { useState, useEffect } from "react";
// components
import {type BaseEntityTableRow, EditableText, type LegacyEntityTableRow} from "@/music-universe/shared/components";
// types
import type { DimensionSaveRequest } from "@/music-universe/music-data/api/music-data-dimensions";
// api
import { saveDimension } from "@/music-universe/music-data/api/music-data-dimensions";
// styles
import sharedStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import styles from "./DimensionsTableRow.module.css";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useMasterEntity} from "@/music-universe/music-data/hooks/useMasterEntity.ts";
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import artistTableStyles
    from "@/music-universe/sources/lastfm/components/LastfmArtistsTable/LastfmArtistsTable.module.css";

interface DimensionsTableRowProps extends BaseEntityTableRow {
}

export const DimensionsTableRow = (
    {
        entityId
    }: DimensionsTableRowProps
) => {

    const entityType: MasterEntityType = 'dimension';

    const {
        entity,
        isLoading: isLoadingEntity,
        isError,
        error
    } = useMasterEntity(entityType, entityId);

    const [editedName, setEditedName] = useState(entity?.name || '');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    // If entity is loading, show loading state
    if (isLoadingEntity) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${artistTableStyles.name}`}>
                    Loading...
                </div>
            </div>
        )
    }

    if (!entity) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${artistTableStyles.name}`}>
                    {isError && error ? error.message : 'No entity found'}
                </div>
            </div>
        )
    }

    const handleSave = async () => {
        console.log('💾 DimensionsTableRow handleSave called:', { isDirty, editedName: editedName.trim() });
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: DimensionSaveRequest = {
                id: entity.id,
                name: editedName.trim()
            };

            const savedDimension = await saveDimension(saveRequest);
            if (savedDimension) {
                console.log('✅ Dimension saved successfully:', savedDimension.id);
            } else {
                console.error('❌ Failed to save dimension');
                // Reset to original value on failure
                setEditedName(entity.name);
            }
        } catch (error) {
            console.error('❌ Error saving dimension:', error);
            // Reset to original value on error
            setEditedName(entity.name);
        } finally {
            setIsSaving(false);
        }
    };

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
    };

    const handleDirtyChange = (newIsDirty: boolean) => {
        setIsDirty(newIsDirty);
    };

    return (
        <div className={sharedStyles.row}>
            <div className={`${sharedStyles.cell} ${styles.name}`}>
                <EditableText
                    value={entity.name}
                    onChange={handleNameChange}
                    onDirtyChange={handleDirtyChange}
                    placeholder="Dimension name"
                    disabled={isSaving}
                />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.actions}`}>
                <button
                    onClick={handleSave}
                    disabled={!isDirty || isSaving || !editedName.trim()}
                    className={styles.saveButton}
                >
                    {isSaving ? "..." : "Save"}
                </button>
            </div>
        </div>
    );
};
