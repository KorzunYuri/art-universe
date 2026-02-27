import { useState, useEffect } from 'react';
import { EditableText } from '@/shared/components';
import { useNotifications } from '@/shared/hooks';
import { saveCategory, type CategorySaveRequest } from '@/music/data/master/api/music-data-categories';
import type { Category } from '@/music/shared/types/entities';
import rowStyles from '../../CategoriesTableRow/CategoriesTableRow.module.css';

interface NameCellProps {
    category: Category;
    onSaved: () => void;
}

export const NameCell = ({ category, onSaved }: NameCellProps) => {
    const { showNotification } = useNotifications();
    const [editedName, setEditedName] = useState(category.name);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        setEditedName(category.name);
        setIsDirty(false);
    }, [category.name]);

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== category.name);
    };

    const handleSave = async (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: CategorySaveRequest = { id: category.id, name: editedName.trim() };
            await saveCategory(saveRequest);
            setIsDirty(false);
            onSaved();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save category');
            setEditedName(category.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }} onClick={(e) => e.stopPropagation()}>
            <EditableText
                value={editedName}
                onChange={handleNameChange}
                placeholder="Category name"
                disabled={isSaving}
            />
            {isDirty && (
                <button
                    onClick={handleSave}
                    disabled={isSaving || !editedName.trim()}
                    className={rowStyles.saveButton}
                >
                    {isSaving ? '...' : 'Save'}
                </button>
            )}
        </span>
    );
};
