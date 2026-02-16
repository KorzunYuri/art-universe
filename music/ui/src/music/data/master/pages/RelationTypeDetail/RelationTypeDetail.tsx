import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useNotifications } from '@/music/shared/hooks';
import { EditableText, ConfirmDialog } from '@/music/shared/components';
import { useRelationTypeDetail } from '@/music/data/master/hooks/useRelationTypeDetail';
import {
    updateRelationType,
    deleteRelationType,
    addApplicability,
    removeApplicability,
    type RelationTypeApplicabilityDTO,
} from '@/music/data/master/api/music-data-relation-types';
import type { MasterEntityType } from '@/music/shared/types/entities';
import styles from '../MasterDetailPage.module.scss';
import localStyles from './RelationTypeDetail.module.scss';

const RELATABLE_TYPES: MasterEntityType[] = ['artist', 'album', 'track'];

export const RelationTypeDetail = () => {
    const { relationTypeId } = useParams<{ relationTypeId: string }>();
    const id = Number(relationTypeId);
    const { showNotification } = useNotifications();

    const {
        relationType,
        invalidateRelationType,
        isLoading,
        isError,
        error,
    } = useRelationTypeDetail(id);

    const [editedName, setEditedName] = useState('');
    const [editedReverseName, setEditedReverseName] = useState('');
    const [editedIsSymmetrical, setEditedIsSymmetrical] = useState(false);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const [processingApplicabilities, setProcessingApplicabilities] = useState<Set<number>>(new Set());
    const [newSource, setNewSource] = useState<MasterEntityType>('artist');
    const [newTarget, setNewTarget] = useState<MasterEntityType>('album');
    const [newIsDefault, setNewIsDefault] = useState(false);
    const [isAddingApplicability, setIsAddingApplicability] = useState(false);

    useEffect(() => {
        if (relationType) {
            setEditedName(relationType.name);
            setEditedReverseName(relationType.reverseName ?? '');
            setEditedIsSymmetrical(relationType.isSymmetrical);
            setIsDirty(false);
        }
    }, [relationType]);

    const checkDirty = (name: string, reverseName: string, isSymmetrical: boolean) => {
        if (!relationType) return false;
        return (
            name !== relationType.name ||
            reverseName !== (relationType.reverseName ?? '') ||
            isSymmetrical !== relationType.isSymmetrical
        );
    };

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(checkDirty(newName, editedReverseName, editedIsSymmetrical));
    };

    const handleReverseNameChange = (newReverseName: string) => {
        setEditedReverseName(newReverseName);
        setIsDirty(checkDirty(editedName, newReverseName, editedIsSymmetrical));
    };

    const handleSymmetricalChange = (checked: boolean) => {
        setEditedIsSymmetrical(checked);
        setIsDirty(checkDirty(editedName, editedReverseName, checked));
    };

    if (isLoading) {
        return <div className={styles.loading}>Loading...</div>;
    }

    if (isError || !relationType) {
        return (
            <div className={styles.error}>
                {error ? (error as any).message : 'Relation type not found'}
            </div>
        );
    }

    const isSystem = relationType.isSystem;

    const handleSave = async () => {
        if (!isDirty || !editedName.trim() || isSaving) return;
        setIsSaving(true);
        try {
            await updateRelationType(
                id,
                editedName.trim(),
                editedReverseName.trim() || null,
                editedIsSymmetrical
            );
            setIsDirty(false);
            invalidateRelationType();
        } catch (err: any) {
            showNotification('error', err?.response?.data?.message || err?.message || 'Failed to save');
            setEditedName(relationType.name);
            setEditedReverseName(relationType.reverseName ?? '');
            setEditedIsSymmetrical(relationType.isSymmetrical);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (isDeleting) return;
        setIsDeleting(true);
        try {
            await deleteRelationType(id);
            invalidateRelationType();
        } catch (err: any) {
            showNotification('error', err?.response?.data?.message || err?.message || 'Failed to delete');
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirm(false);
        }
    };

    const handleRemoveApplicability = async (applicability: RelationTypeApplicabilityDTO) => {
        setProcessingApplicabilities(prev => new Set(prev).add(applicability.id));
        try {
            await removeApplicability(id, applicability.sourceEntityType, applicability.targetEntityType);
            invalidateRelationType();
        } catch (err: any) {
            showNotification('error', err?.response?.data?.message || err?.message || 'Failed to remove applicability');
        } finally {
            setProcessingApplicabilities(prev => {
                const next = new Set(prev);
                next.delete(applicability.id);
                return next;
            });
        }
    };

    const handleAddApplicability = async () => {
        if (isAddingApplicability) return;
        setIsAddingApplicability(true);
        try {
            await addApplicability(id, newSource, newTarget, newIsDefault);
            setNewIsDefault(false);
            invalidateRelationType();
        } catch (err: any) {
            showNotification('error', err?.response?.data?.message || err?.message || 'Failed to add applicability');
        } finally {
            setIsAddingApplicability(false);
        }
    };

    return (
        <div className={styles.detail}>
            {/* Header: name + actions */}
            <div className={styles.header}>
                <EditableText
                    value={editedName}
                    onChange={handleNameChange}
                    placeholder="Relation type name"
                    disabled={isSaving || isSystem}
                    className={styles.nameInput}
                />
                {!isSystem && (
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
                )}
                {isSystem && (
                    <span className={localStyles.systemBadge}>System (read-only)</span>
                )}
            </div>

            {/* Reverse Name */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Reverse Name</h3>
                <EditableText
                    value={editedReverseName}
                    onChange={handleReverseNameChange}
                    placeholder="Reverse name (optional)"
                    disabled={isSaving || isSystem}
                />
            </section>

            {/* Symmetrical */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Symmetrical</h3>
                <label className={localStyles.checkboxLabel}>
                    <input
                        type="checkbox"
                        checked={editedIsSymmetrical}
                        onChange={(e) => handleSymmetricalChange(e.target.checked)}
                        disabled={isSaving || isSystem}
                    />
                    {editedIsSymmetrical ? 'Yes — relation goes both ways' : 'No — relation is directional'}
                </label>
            </section>

            {/* Applicabilities */}
            <section className={styles.section}>
                <h3 className={styles.sectionTitle}>Applicabilities</h3>
                {relationType.applicabilities.length === 0 ? (
                    <div className={styles.stub}>No applicabilities defined</div>
                ) : (
                    <div className={localStyles.applicabilityList}>
                        {relationType.applicabilities.map((app) => (
                            <div key={app.id} className={localStyles.applicabilityRow}>
                                <span className={localStyles.applicabilityLabel}>
                                    {app.sourceEntityType} → {app.targetEntityType}
                                    {app.isDefault && (
                                        <span className={localStyles.defaultBadge}>default</span>
                                    )}
                                </span>
                                {!isSystem && (
                                    <button
                                        onClick={() => handleRemoveApplicability(app)}
                                        disabled={processingApplicabilities.has(app.id)}
                                        className={localStyles.removeButton}
                                        title="Remove applicability"
                                    >
                                        ✕
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}

                {!isSystem && (
                    <div className={localStyles.addApplicabilityForm}>
                        <select
                            value={newSource}
                            onChange={(e) => setNewSource(e.target.value as MasterEntityType)}
                            disabled={isAddingApplicability}
                            className={localStyles.typeSelect}
                        >
                            {RELATABLE_TYPES.map((t) => (
                                <option key={t} value={t}>{t}</option>
                            ))}
                        </select>
                        <span className={localStyles.arrow}>→</span>
                        <select
                            value={newTarget}
                            onChange={(e) => setNewTarget(e.target.value as MasterEntityType)}
                            disabled={isAddingApplicability}
                            className={localStyles.typeSelect}
                        >
                            {RELATABLE_TYPES.map((t) => (
                                <option key={t} value={t}>{t}</option>
                            ))}
                        </select>
                        <label className={localStyles.checkboxLabel}>
                            <input
                                type="checkbox"
                                checked={newIsDefault}
                                onChange={(e) => setNewIsDefault(e.target.checked)}
                                disabled={isAddingApplicability}
                            />
                            Default
                        </label>
                        <button
                            onClick={handleAddApplicability}
                            disabled={isAddingApplicability}
                            className={localStyles.addButton}
                        >
                            {isAddingApplicability ? '...' : 'Add'}
                        </button>
                    </div>
                )}
            </section>

            <ConfirmDialog
                isOpen={showDeleteConfirm}
                header="Delete Relation Type"
                message={`Are you sure you want to delete relation type "${relationType.name}"?`}
                onConfirm={handleDelete}
                onCancel={() => setShowDeleteConfirm(false)}
            />
        </div>
    );
};
