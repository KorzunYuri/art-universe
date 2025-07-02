// hooks
import { useState, useEffect } from "react";
// components
import { EditableText, AutocompleteInput, ReadonlyAttr } from "@/music-universe/shared/components";
// types
import type { Category, CategorySaveRequest } from "@/music-universe/music-data/api/music-data-categories";
import type { LookupEntity } from "@/music-universe/shared/components/AutocompleteInput";
// api
import { saveCategory, lookupCategories } from "@/music-universe/music-data/api/music-data-categories";
import { lookupDimensions } from "@/music-universe/music-data/api/music-data-dimensions";
// styles
import sharedStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
import styles from "./CategoriesTableRow.module.css";

interface CategoriesTableRowProps {
    category: Category;
    onChange: (category: Category) => void;
}

export const CategoriesTableRow = ({ category, onChange }: CategoriesTableRowProps) => {
    const [editedName, setEditedName] = useState(category.name);
    const [editedParentName, setEditedParentName] = useState(category.parentName || '');
    const [editedDimensionName, setEditedDimensionName] = useState(category.dimensionName || '');
    const [selectedParent, setSelectedParent] = useState<LookupEntity | null>(null);
    const [selectedDimension, setSelectedDimension] = useState<LookupEntity | null>(null);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    // Sync with external category changes
    useEffect(() => {
        setEditedName(category.name);
        setEditedParentName(category.parentName || '');
        setEditedDimensionName(category.dimensionName || '');
        setSelectedParent(null);
        setSelectedDimension(null);
        setIsDirty(false);
    }, [category.name, category.parentName, category.dimensionName]);

    const checkDirty = (name: string, parentName: string, dimensionName: string) => {
        const isNameDirty = name !== category.name;
        const isParentDirty = parentName !== (category.parentName || '');
        const isDimensionDirty = dimensionName !== (category.dimensionName || '');
        const newIsDirty = isNameDirty || isParentDirty || isDimensionDirty;
        
        setIsDirty(newIsDirty);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: CategorySaveRequest = {
                id: category.id,
                name: editedName.trim(),
                parentId: selectedParent?.id || (editedParentName && editedParentName !== (category.parentName || '') ? null : category.parentId),
                dimensionId: selectedDimension?.id || (editedDimensionName && editedDimensionName !== (category.dimensionName || '') ? null : category.dimensionId)
            };

            const savedCategory = await saveCategory(saveRequest);
            if (savedCategory) {
                console.log('✅ Category saved successfully:', savedCategory.id);
                onChange(savedCategory);
            } else {
                console.error('❌ Failed to save category');
                // Reset to original values on failure
                setEditedName(category.name);
                setEditedParentName(category.parentName || '');
                setEditedDimensionName(category.dimensionName || '');
                setSelectedParent(null);
                setSelectedDimension(null);
            }
        } catch (error) {
            console.error('❌ Error saving category:', error);
            // Reset to original values on error
            setEditedName(category.name);
            setEditedParentName(category.parentName || '');
            setEditedDimensionName(category.dimensionName || '');
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

    const handleParentSelect = (parent: LookupEntity) => {
        setSelectedParent(parent);
        setEditedParentName(parent.name);
        checkDirty(editedName, parent.name, editedDimensionName);
    };

    const handleDimensionChange = (newDimensionName: string) => {
        setEditedDimensionName(newDimensionName);
        // Clear selected dimension when user types manually
        if (selectedDimension && newDimensionName !== selectedDimension.name) {
            setSelectedDimension(null);
        }
        checkDirty(editedName, editedParentName, newDimensionName);
    };

    const handleDimensionSelect = (dimension: LookupEntity) => {
        setSelectedDimension(dimension);
        setEditedDimensionName(dimension.name);
        checkDirty(editedName, editedParentName, dimension.name);
    };

    return (
        <div className={sharedStyles.row}>
            <div className={`${sharedStyles.cell} ${styles.name}`}>
                <EditableText
                    value={category.name}
                    onChange={handleNameChange}
                    placeholder="Category name"
                    disabled={isSaving}
                />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.parent}`}>
                <AutocompleteInput
                    value={editedParentName}
                    onChange={handleParentChange}
                    onSelect={handleParentSelect}
                    lookupFunction={lookupCategories}
                    placeholder="Parent category"
                    disabled={isSaving}
                />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.dimension}`}>
                <AutocompleteInput
                    value={editedDimensionName}
                    onChange={handleDimensionChange}
                    onSelect={handleDimensionSelect}
                    lookupFunction={lookupDimensions}
                    placeholder="Dimension"
                    disabled={isSaving}
                />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.effectiveDimension}`}>
                <ReadonlyAttr value={category.effectiveDimensionName || '-'} />
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
