import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNotifications } from '@/shared/hooks';
import { EditableText, ConfirmDialog } from '@/shared/components';
import { usePerson } from '@/art/data/master/hooks/usePerson';
import {
    savePerson,
    deletePerson,
    type PersonSaveRequest,
} from '@/art/data/master/api/art-data-persons';
import styles from '@/art/data/master/styles/ArtDetailPage.module.scss';

export const PersonDetail = () => {
    const { personId } = useParams<{ personId: string }>();
    const id = Number(personId);
    const { showNotification } = useNotifications();

    const {
        person,
        invalidatePerson,
        isLoading,
        isError,
        error,
    } = usePerson(id);

    const [editedName, setEditedName] = useState('');
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    useEffect(() => {
        if (person) {
            setEditedName(person.name);
            setIsDirty(false);
        }
    }, [person]);

    if (isLoading) {
        return <div className={styles.loading}>Loading person...</div>;
    }

    if (isError || !person) {
        return (
            <div className={styles.error}>
                {error ? error.message : 'Person not found'}
            </div>
        );
    }

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== person.name);
    };

    const handleSave = async () => {
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: PersonSaveRequest = { id: person.id, name: editedName.trim() };
            await savePerson(saveRequest);
            setIsDirty(false);
            invalidatePerson();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save person');
            setEditedName(person.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deletePerson(person.id);
            // Navigation back will happen via the parent closing the panel
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to delete person');
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
                    placeholder="Person name"
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

            {/* Relations stub */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Relations</h3>
                <div className={styles.stub}>Relations coming soon</div>
            </section>

            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Person"
                message={`Are you sure you want to delete person "${person.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </div>
    );
};
