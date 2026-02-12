import { useState } from 'react';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { useApplicableRelationTypes } from '@/music/data/master/hooks/useApplicableRelationTypes';
import type { LookupEntity } from '@/music/shared/types/lookup';
import type { MasterEntityType } from '@/music/shared/types/entities';
import styles from './RelatedEntitiesSection.module.scss';

interface AddRelationFormProps {
    sourceEntityType: MasterEntityType;
    targetEntityType: MasterEntityType;
    onSubmit: (targetEntityId: number, relationTypeId: number | undefined) => Promise<void>;
    onCancel: () => void;
    isSubmitting: boolean;
}

export const AddRelationForm = ({
    sourceEntityType,
    targetEntityType,
    onSubmit,
    onCancel,
    isSubmitting,
}: AddRelationFormProps) => {
    const [selectedEntity, setSelectedEntity] = useState<LookupEntity | null>(null);
    const [selectedRelationTypeId, setSelectedRelationTypeId] = useState<string>('');

    const { relationTypes, isLoading: isLoadingTypes } = useApplicableRelationTypes(
        sourceEntityType,
        targetEntityType
    );

    const handleEntitySelected = (entity: LookupEntity) => {
        setSelectedEntity(entity);
    };

    const handleSubmit = async () => {
        if (!selectedEntity) return;
        const relationTypeId = selectedRelationTypeId ? Number(selectedRelationTypeId) : undefined;
        await onSubmit(selectedEntity.id, relationTypeId);
        setSelectedEntity(null);
        setSelectedRelationTypeId('');
    };

    return (
        <div className={styles.addForm}>
            <div className={styles.addFormRow}>
                <MasterEntityPicker
                    entityType={targetEntityType}
                    buttonLabel={`Select ${targetEntityType}`}
                    onEntitySelected={handleEntitySelected}
                    disabled={isSubmitting}
                />
                {selectedEntity && (
                    <span className={styles.selectedEntity}>{selectedEntity.name}</span>
                )}
            </div>

            {relationTypes.length > 0 && (
                <div className={styles.addFormRow}>
                    <select
                        value={selectedRelationTypeId}
                        onChange={(e) => setSelectedRelationTypeId(e.target.value)}
                        disabled={isSubmitting || isLoadingTypes}
                        className={styles.relationTypeSelect}
                    >
                        <option value="">No relation type</option>
                        {relationTypes.map((rt) => (
                            <option key={rt.id} value={String(rt.id)}>
                                {rt.name}
                            </option>
                        ))}
                    </select>
                </div>
            )}

            <div className={styles.addFormActions}>
                <button
                    onClick={handleSubmit}
                    disabled={!selectedEntity || isSubmitting}
                    className={styles.addButton}
                >
                    {isSubmitting ? '...' : 'Add'}
                </button>
                <button
                    onClick={onCancel}
                    disabled={isSubmitting}
                    className={styles.cancelButton}
                >
                    Cancel
                </button>
            </div>
        </div>
    );
};
