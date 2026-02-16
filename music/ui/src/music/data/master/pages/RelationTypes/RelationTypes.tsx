import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { RelationTypesTable } from '@/music/data/master/components/RelationTypesTable';
import { useNotifications } from '@/music/shared/hooks';
import { createRelationType } from '@/music/data/master/api/music-data-relation-types';
import { relationTypeKeys } from '@/music/shared/utils/query-keys';
import styles from './RelationTypes.module.css';

export function RelationTypes() {
    const queryClient = useQueryClient();
    const { showNotification } = useNotifications();

    const [newName, setNewName] = useState('');
    const [newReverseName, setNewReverseName] = useState('');
    const [newIsSymmetrical, setNewIsSymmetrical] = useState(false);
    const [isCreating, setIsCreating] = useState(false);

    const handleCreate = async () => {
        if (!newName.trim() || isCreating) return;
        setIsCreating(true);
        try {
            await createRelationType(
                newName.trim(),
                newReverseName.trim() || null,
                newIsSymmetrical
            );
            setNewName('');
            setNewReverseName('');
            setNewIsSymmetrical(false);
            queryClient.invalidateQueries({ queryKey: relationTypeKeys.all });
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create relation type');
        } finally {
            setIsCreating(false);
        }
    };

    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Relation Types</h2>
            </div>

            <div className={styles.createSection}>
                <input
                    type="text"
                    value={newName}
                    onChange={(e) => setNewName(e.target.value)}
                    placeholder="Name (required)"
                    disabled={isCreating}
                    onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
                    className={styles.nameInput}
                />
                <input
                    type="text"
                    value={newReverseName}
                    onChange={(e) => setNewReverseName(e.target.value)}
                    placeholder="Reverse name (optional)"
                    disabled={isCreating}
                    className={styles.reverseNameInput}
                />
                <label className={styles.checkboxLabel}>
                    <input
                        type="checkbox"
                        checked={newIsSymmetrical}
                        onChange={(e) => setNewIsSymmetrical(e.target.checked)}
                        disabled={isCreating}
                    />
                    Symmetrical
                </label>
                <button
                    onClick={handleCreate}
                    disabled={!newName.trim() || isCreating}
                    className={styles.createButton}
                >
                    {isCreating ? '...' : 'Create'}
                </button>
            </div>

            <RelationTypesTable />
        </div>
    );
}
