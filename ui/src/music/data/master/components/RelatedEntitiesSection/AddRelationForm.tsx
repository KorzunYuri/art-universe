import { useState } from 'react';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import { useApplicableRelationTypes } from '@/music/data/master/hooks/useApplicableRelationTypes';
import type { LookupEntity } from '@/shared/types/lookup';
import type { MasterEntityType } from '@/music/shared/types/entities';
import styles from './RelatedEntitiesSection.module.scss';

interface AddRelationFormProps {
    sourceEntityType: MasterEntityType;
    targetEntityType: MasterEntityType;
    onSubmit: (targetEntityId: number, relationTypeIds: number[]) => Promise<void>;
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
    const [selectedRelationTypeIds, setSelectedRelationTypeIds] = useState<number[]>([]);

    const { relationTypes, isLoading: isLoadingTypes } = useApplicableRelationTypes(
        sourceEntityType,
        targetEntityType
    );

    const handleEntitySelected = (entity: LookupEntity) => {
        setSelectedEntity(entity);
    };

    const handleAddType = (typeId: string) => {
        if (!typeId) return;
        const id = Number(typeId);
        if (!selectedRelationTypeIds.includes(id)) {
            setSelectedRelationTypeIds(prev => [...prev, id]);
        }
    };

    const handleRemoveType = (typeId: number) => {
        setSelectedRelationTypeIds(prev => prev.filter(id => id !== typeId));
    };

    const handleSubmit = async () => {
        if (!selectedEntity) return;
        await onSubmit(selectedEntity.id, selectedRelationTypeIds);
        setSelectedEntity(null);
        setSelectedRelationTypeIds([]);
    };

    const availableTypes = relationTypes.filter(rt => !selectedRelationTypeIds.includes(rt.id));

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
                    <div className={styles.relationTypeChips}>
                        {selectedRelationTypeIds.map(typeId => {
                            const rt = relationTypes.find(r => r.id === typeId);
                            return (
                                <span key={typeId} className={styles.relationTypeChip}>
                                    {rt?.name ?? `Type ${typeId}`}
                                    <button
                                        type="button"
                                        className={styles.chipRemove}
                                        onClick={() => handleRemoveType(typeId)}
                                        disabled={isSubmitting}
                                    >
                                        &times;
                                    </button>
                                </span>
                            );
                        })}
                        {availableTypes.length > 0 && (
                            <select
                                value=""
                                onChange={(e) => handleAddType(e.target.value)}
                                disabled={isSubmitting || isLoadingTypes}
                                className={styles.relationTypeSelect}
                            >
                                <option value="">
                                    {selectedRelationTypeIds.length === 0 ? 'Select relation type...' : 'Add another type...'}
                                </option>
                                {availableTypes.map((rt) => (
                                    <option key={rt.id} value={String(rt.id)}>
                                        {rt.name}
                                    </option>
                                ))}
                            </select>
                        )}
                    </div>
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
