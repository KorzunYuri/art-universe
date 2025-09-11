// hooks
import {useState} from "react";
// components
import {
    EditableText,
    EntityLookup,
    type BaseEntityTableRow
} from "@/music-universe/shared/components";
// types
import type { CategorySaveRequest } from "@/music-universe/music-data/api/music-data-categories";
import type {LookupEntity} from "@/music-universe/shared/types/lookup.ts";
// api
import {saveCategory} from "@/music-universe/music-data/api/music-data-categories";
// styles
import styles from "./CategoriesTableRow.module.css";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useMasterEntity} from "@/music-universe/music-data/hooks/useMasterEntity.ts";
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import artistTableStyles
    from "@/music-universe/sources/lastfm/components/LastfmArtistsTable/LastfmArtistsTable.module.css";
import { LookupContextFactory } from "@/music-universe/shared/types/lookup-context";

interface CategoriesTableRowProps extends BaseEntityTableRow {
}
export const CategoriesTableRow = (
    {
        entityId
    }: CategoriesTableRowProps
) => {

    const entityType: MasterEntityType = 'category';

    const {
        entity,
        isLoading: isLoadingEntity,
        isError,
        error
    } = useMasterEntity(entityType, entityId);

    const [editedName, setEditedName] = useState(entity?.name || '');
    const [editedParentName, setEditedParentName] = useState(entity?.parentName || '');
    const [selectedParent, setSelectedParent] = useState<LookupEntity | null>(null);
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

    const checkDirty = (name: string, parentName: string) => {
        const isNameDirty = name !== entity.name;
        const isParentDirty = parentName !== (entity.parentName || '');
        const newIsDirty = isNameDirty || isParentDirty;

        setIsDirty(newIsDirty);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: CategorySaveRequest = {
                id: entity.id,
                name: editedName.trim(),
                parentId: selectedParent?.id || (editedParentName && editedParentName !== (entity.parentName || '') ? null : entity.parentId)
            };

            const savedCategory = await saveCategory(saveRequest);
            if (savedCategory) {
                console.log('✅ Category saved successfully:', savedCategory.id);
                // Note: No longer updating parent component directly
                // The entity will be updated in place via the class instance
            } else {
                console.error('❌ Failed to save category');
                // Reset to original values on failure
                setEditedName(entity.name);
                setEditedParentName(entity.parentName || '');
                setSelectedParent(null);
            }
        } catch (error) {
            console.error('❌ Error saving category:', error);
            // Reset to original values on error
            setEditedName(entity.name);
            setEditedParentName(entity.parentName || '');
            setSelectedParent(null);
        } finally {
            setIsSaving(false);
        }
    };

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        checkDirty(newName, editedParentName);
    };

    const handleParentChange = (newParentName: string) => {
        setEditedParentName(newParentName);
        // Clear selected parent when user types manually
        if (selectedParent && newParentName !== selectedParent.name) {
            setSelectedParent(null);
        }
        checkDirty(editedName, newParentName);
    };

    const handleParentSelect = (parent: LookupEntity | null) => {
        if (parent) {
            setSelectedParent(parent);
            setEditedParentName(parent.name);
            checkDirty(editedName, parent.name);
        } else {
            setSelectedParent(null);
            setEditedParentName('');
            checkDirty(editedName, '');
        }
    };

    return (
        <div className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell} ${styles.name}`}>
                <EditableText
                    value={entity.name}
                    onChange={handleNameChange}
                    placeholder="Category name"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${styles.parent}`}>
                <EntityLookup
                    dataSource="master"
                    entityType={'category'}
                    searchString={editedParentName}
                    context={LookupContextFactory.basic()}
                    onChange={handleParentChange}
                    onSelect={handleParentSelect}
                    selectedEntity={selectedParent}
                    placeholder="Parent category"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${styles.actions}`}>
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
