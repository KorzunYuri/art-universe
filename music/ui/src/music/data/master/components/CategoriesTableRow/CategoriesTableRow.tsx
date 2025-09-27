// hooks
import { useState, useEffect } from "react";
import { useNotifications } from "@/music/shared/hooks";
// components
import { EditableText, ConfirmDialog, type BaseEntityTableRow } from "@/music/shared/components";
import { MasterEntityPanel } from "../MasterEntityPanel";
import { MasterEntityPicker } from "../MasterEntityPicker";
// types
import type { CategorySaveRequest } from "@/music/data/master/api/music-data-categories.ts";
// api
import { saveCategory, deleteCategory, deleteCategoryRelation, createCategoryRelation } from "@/music/data/master/api/music-data-categories.ts";
// hooks
import { useCategoryWithParents } from "@/music/data/master/hooks/useCategoryWithParents.ts";
// styles
import styles from "./CategoriesTableRow.module.css";
import tableStyles from "../CategoriesTable/CategoriesTable.module.css";
import sharedTableStyles from "@/music/shared/styles/EntityTableStyles.module.scss";

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
    const [processingParents, setProcessingParents] = useState<Set<number>>(new Set());

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

    const handleParentRemoved = async (parentId: number) => {
        setProcessingParents(prev => new Set(prev).add(parentId));
        
        try {
            await deleteCategoryRelation({
                sourceId: parentId,
                targetId: category.id
            });
            
            console.log(`✅ Successfully removed parent ${parentId} from category ${category.id}`);
            invalidateCategory();
        } catch (error: any) {
            console.error(`❌ Error removing parent ${parentId}:`, error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove parent');
        } finally {
            setProcessingParents(prev => {
                const newSet = new Set(prev);
                newSet.delete(parentId);
                return newSet;
            });
        }
    };

    const handleParentAdded = async (entity: any) => {
        try {
            await createCategoryRelation({
                sourceId: entity.id,
                targetId: category.id
            });
            
            console.log(`✅ Successfully added parent ${entity.id} to category ${category.id}`);
            invalidateCategory();
        } catch (error: any) {
            console.error(`❌ Error adding parent ${entity.id}:`, error);
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add parent');
        }
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
                <MasterEntityPanel
                    entities={category.parents}
                    onEntityRemoved={handleParentRemoved}
                    processingEntities={processingParents}
                    emptyMessage="No parents"
                    removeTitle="Remove parent"
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tableStyles.addParent}`}>
                <MasterEntityPicker
                    entityType="category"
                    buttonLabel="Add Parent"
                    onEntitySelected={handleParentAdded}
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
