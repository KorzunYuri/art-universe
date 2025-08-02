// hooks
import {useState} from "react";
// components
import {
    EditableText,
    MasterEntityLookup,
    StaticAutocompleteInput,
    ReadonlyAttr, type BaseEntityTableRow
} from "@/music-universe/shared/components";
// types
import type { CategorySaveRequest } from "@/music-universe/music-data/api/music-data-categories";
import {createBaseLookupRequest, type LookupEntity} from "@/music-universe/music-data/types/master-entities-lookup.ts";
// api
import {saveCategory} from "@/music-universe/music-data/api/music-data-categories";
// styles
import styles from "./CategoriesTableRow.module.css";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {useMasterEntity} from "@/music-universe/music-data/hooks/useMasterEntity.ts";
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import artistTableStyles
    from "@/music-universe/sources/lastfm/components/LastfmArtistsTable/LastfmArtistsTable.module.css";
import {useMasterEntitiesLookup} from "@/music-universe/music-data/hooks/useMasterEntitiesLookup.ts";

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

    const {
        currentOptions: dimensions,
        isLoading: isLoadingDimensions
    } = useMasterEntitiesLookup(entityType, { search: '' }); // we need all dimensions at once

    const [editedName, setEditedName] = useState(entity?.name || '');
    const [editedParentName, setEditedParentName] = useState(entity?.parentName || '');
    const [editedDimensionName, setEditedDimensionName] = useState(entity?.dimensionName || '');
    const [selectedParent, setSelectedParent] = useState<LookupEntity | null>(null);
    const [selectedDimension, setSelectedDimension] = useState<LookupEntity | null>(null);
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

    const checkDirty = (name: string, parentName: string, dimensionName: string) => {
        const isNameDirty = name !== entity.name;
        const isParentDirty = parentName !== (entity.parentName || '');
        const isDimensionDirty = dimensionName !== (entity.dimensionName || '');
        const newIsDirty = isNameDirty || isParentDirty || isDimensionDirty;

        setIsDirty(newIsDirty);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: CategorySaveRequest = {
                id: entity.id,
                name: editedName.trim(),
                parentId: selectedParent?.id || (editedParentName && editedParentName !== (entity.parentName || '') ? null : entity.parentId),
                dimensionId: selectedDimension?.id || (editedDimensionName && editedDimensionName !== (entity.dimensionName || '') ? null : entity.dimensionId)
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
                setEditedDimensionName(entity.dimensionName || '');
                setSelectedParent(null);
                setSelectedDimension(null);
            }
        } catch (error) {
            console.error('❌ Error saving category:', error);
            // Reset to original values on error
            setEditedName(entity.name);
            setEditedParentName(entity.parentName || '');
            setEditedDimensionName(entity.dimensionName || '');
            setSelectedParent(null);
            setSelectedDimension(null);
        } finally {
            setIsSaving(false);
        }
    };

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        checkDirty(newName, editedParentName, editedDimensionName);
    };

    const handleParentChange = (newParentName: string) => {
        setEditedParentName(newParentName);
        // Clear selected parent when user types manually
        if (selectedParent && newParentName !== selectedParent.name) {
            setSelectedParent(null);
        }
        checkDirty(editedName, newParentName, editedDimensionName);
    };

    const handleParentSelect = (parent: LookupEntity | null) => {
        if (parent) {
            setSelectedParent(parent);
            setEditedParentName(parent.name);
            checkDirty(editedName, parent.name, editedDimensionName);
        } else {
            setSelectedParent(null);
            setEditedParentName('');
            checkDirty(editedName, '', editedDimensionName);
        }
    };

    const handleDimensionChange = (newDimensionName: string) => {
        setEditedDimensionName(newDimensionName);
        // Clear selected dimension when user types manually
        if (selectedDimension && newDimensionName !== selectedDimension.name) {
            setSelectedDimension(null);
        }
        checkDirty(editedName, editedParentName, newDimensionName);
    };

    const handleDimensionSelect = (dimension: LookupEntity | null) => {
        if (dimension) {
            setSelectedDimension(dimension);
            setEditedDimensionName(dimension.name);
            checkDirty(editedName, editedParentName, dimension.name);
        } else {
            setSelectedDimension(null);
            setEditedDimensionName('');
            checkDirty(editedName, editedParentName, '');
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
                <MasterEntityLookup
                    entityType={'category'}
                    searchString={editedParentName}
                    requestFactory={createBaseLookupRequest}
                    onChange={handleParentChange}
                    onSelect={handleParentSelect}
                    selectedEntity={selectedParent}
                    placeholder="Parent category"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${styles.dimension}`}>
                <StaticAutocompleteInput
                    searchString={editedDimensionName}
                    onChange={handleDimensionChange}
                    onSelect={handleDimensionSelect}
                    options={dimensions}
                    selectedEntity={selectedDimension}
                    placeholder="Dimension"
                    disabled={isSaving || isLoadingDimensions}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${styles.effectiveDimension}`}>
                <ReadonlyAttr value={entity.effectiveDimensionName || '-'}/>
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
