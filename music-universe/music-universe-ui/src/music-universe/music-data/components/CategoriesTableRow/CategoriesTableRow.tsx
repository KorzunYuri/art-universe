// hooks
import {useState, useEffect} from "react";
// components
import {
    EditableText,
    DynamicAutocompleteInput,
    StaticAutocompleteInput,
    ReadonlyAttr
} from "@/music-universe/shared/components";
// types
import type { Category } from '@/music-universe/music-data/types/master-entities';
import type { CategorySaveRequest } from "@/music-universe/music-data/api/music-data-categories";
import type {LookupEntity} from "@/music-universe/shared/types/lookup";
import type { MasterEntityTableRow } from "@/music-universe/shared/types/table-row";
// api
import {saveCategory, lookupCategories} from "@/music-universe/music-data/api/music-data-categories";
// styles
import sharedStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import styles from "./CategoriesTableRow.module.css";

interface CategoriesTableRowProps extends MasterEntityTableRow<Category> {
    preloadedCategories?: LookupEntity[];
    preloadedDimensions?: LookupEntity[];
}

export const CategoriesTableRow = (
    {
        entity,
        preloadedCategories = [],
        preloadedDimensions = []
    }: CategoriesTableRowProps) => {
    const [editedName, setEditedName] = useState(entity.name);
    const [editedParentName, setEditedParentName] = useState(entity.parentName || '');
    const [editedDimensionName, setEditedDimensionName] = useState(entity.dimensionName || '');
    const [selectedParent, setSelectedParent] = useState<LookupEntity | null>(null);
    const [selectedDimension, setSelectedDimension] = useState<LookupEntity | null>(null);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    // Find matching parent and dimension on initial load
    useEffect(() => {
        // Find matching parent if available
        if (entity.parentName && preloadedCategories.length > 0) {
            const matchingParent = preloadedCategories.find(
                option => option.name.toLowerCase() === entity.parentName?.toLowerCase()
            );
            if (matchingParent) {
                console.log(`Found matching parent for ${entity.name}: ${matchingParent.name}`);
                setSelectedParent(matchingParent);
            }
        }

        // Find matching dimension if available
        if (entity.dimensionName && preloadedDimensions.length > 0) {
            const matchingDimension = preloadedDimensions.find(
                option => option.name.toLowerCase() === entity.dimensionName?.toLowerCase()
            );
            if (matchingDimension) {
                console.log(`Found matching dimension for ${entity.name}: ${matchingDimension.name}`);
                setSelectedDimension(matchingDimension);
            }
        }
    }, [entity, preloadedCategories, preloadedDimensions]);

    // Sync with external category changes
    useEffect(() => {
        setEditedName(entity.name);
        setEditedParentName(entity.parentName || '');
        setEditedDimensionName(entity.dimensionName || '');
        setIsDirty(false);
    }, [entity.name, entity.parentName, entity.dimensionName]);

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
        <div className={sharedStyles.row}>
            <div className={`${sharedStyles.cell} ${styles.name}`}>
                <EditableText
                    value={entity.name}
                    onChange={handleNameChange}
                    placeholder="Category name"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedStyles.cell} ${styles.parent}`}>
                <DynamicAutocompleteInput
                    value={editedParentName}
                    onChange={handleParentChange}
                    onSelect={handleParentSelect}
                    lookupFunction={lookupCategories}
                    preloadedOptions={preloadedCategories}
                    selectedEntity={selectedParent}
                    placeholder="Parent category"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedStyles.cell} ${styles.dimension}`}>
                <StaticAutocompleteInput
                    value={editedDimensionName}
                    onChange={handleDimensionChange}
                    onSelect={handleDimensionSelect}
                    options={preloadedDimensions}
                    selectedEntity={selectedDimension}
                    placeholder="Dimension"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedStyles.cell} ${styles.effectiveDimension}`}>
                <ReadonlyAttr value={entity.effectiveDimensionName || '-'}/>
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
