// hooks
import { useState, useEffect } from "react";
import { useNotifications } from "@/music-universe/shared/hooks";
// components
import { EditableText, ConfirmDialog, type BaseEntityTableRow } from "@/music-universe/shared/components";
import { CategoryParentPanel } from "../CategoryParentPanel";
import { CategoryParentAdder } from "../CategoryParentAdder";
// types
import type { CategorySaveRequest } from "@/music-universe/music-data/api/music-data-categories";
// api
import { saveCategory, deleteCategory } from "@/music-universe/music-data/api/music-data-categories";
// hooks
import { useCategoryWithParents } from "@/music-universe/music-data/hooks/useCategoryWithParents";
// styles
import styles from "./CategoriesTableRow.module.css";
import tableStyles from "../CategoriesTable/CategoriesTable.module.css";
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";

interface CategoriesTableRowProps extends BaseEntityTableRow {
    onDeleted?: () => void;
}

export const CategoriesTableRow = ({ entityId, onDeleted }: CategoriesTableRowProps) => {
    const { showNotification } = useNotifications();
    const {
        category,
        invalidateCategory,
        isLoading: isLoadingCategory,
        isError,
        error
    } = useCategoryWithParents(entityId);

    const [editedName, setEditedName] = useState('');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    // Update edited name when category loads
    useEffect(() => {
        if (category) {
            setEditedName(category.name);
        }
    }, [category]);

    // If category is loading, show loading state
    if (isLoadingCategory) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${tableStyles.name}`}>
                    Loading...
                </div>
            </div>
        );
    }

    if (!category) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${tableStyles.name}`}>
                    {isError && error ? error.message : 'No category found'}
                </div>
            </div>
        );
    }

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== category.name);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: CategorySaveRequest = {
                id: category.id,
                name: editedName.trim()
            };

            const savedCategory = await saveCategory(saveRequest);
            if (savedCategory) {
                console.log('✅ Category saved successfully:', savedCategory.id);
                setIsDirty(false);
                invalidateCategory();
            } else {
                console.error('❌ Failed to save category');
                setEditedName(category.name);
                setIsDirty(false);
            }
        } catch (error: any) {
            console.error('❌ Error saving category:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save category');
            setEditedName(category.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleParentChange = () => {
        invalidateCategory();
    };

    const handleDelete = async () => {
        if (!category || isDeleting) return;

        setIsDeleting(true);
        try {
            const deleted = await deleteCategory(category.id);
            if (deleted) {
                console.log('✅ Category deleted successfully:', category.id);
                onDeleted?.();
            } else {
                console.error('❌ Failed to delete category');
            }
        } catch (error: any) {
            console.error('❌ Error deleting category:', error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete category');
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
                    placeholder="Category name"
                    disabled={isSaving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tableStyles.parents}`}>
                <CategoryParentPanel
                    categoryId={category.id}
                    parents={category.parents}
                    onParentRemoved={handleParentChange}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tableStyles.addParent}`}>
                <CategoryParentAdder
                    categoryId={category.id}
                    dataSource="master"
                    entityType="category"
                    buttonLabel="Add Parent"
                    onParentAdded={handleParentChange}
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
                header="Delete Category"
                message={`Are you sure you want to delete category "${category.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </div>
    );
};
