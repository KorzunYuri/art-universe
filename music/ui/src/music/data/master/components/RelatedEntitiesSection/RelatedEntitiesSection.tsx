import { useState, useCallback } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useNotifications } from '@/music/shared/hooks';
import { FoldableSection } from '@/music/shared/components/FoldableSection';
import {
    getRelatedEntities,
    createInternalRelation,
    deleteInternalRelation,
    type RelatedEntityDTO,
} from '@/music/data/master/api/music-data-relations';
import { AddRelationForm } from './AddRelationForm';
import { relationKeys } from '@/music/shared/utils/query-keys';
import type { MasterEntityType } from '@/music/shared/types/entities';
import styles from './RelatedEntitiesSection.module.scss';

interface RelatedEntitiesSectionProps {
    title: string;
    sourceEntityType: MasterEntityType;
    sourceEntityId: number;
    targetEntityType: MasterEntityType;
    defaultOpen?: boolean;
}

const ENTITY_TYPE_LABELS: Record<MasterEntityType, string> = {
    artist: 'artists',
    album: 'albums',
    track: 'tracks',
    category: 'categories',
};

export const RelatedEntitiesSection = ({
    title,
    sourceEntityType,
    sourceEntityId,
    targetEntityType,
    defaultOpen = false,
}: RelatedEntitiesSectionProps) => {
    const queryClient = useQueryClient();
    const { showNotification } = useNotifications();
    const [showAddForm, setShowAddForm] = useState(false);
    const [isAdding, setIsAdding] = useState(false);
    const [removingIds, setRemovingIds] = useState<Set<number>>(new Set());

    const queryKey = relationKeys.entities(sourceEntityType, sourceEntityId, targetEntityType);

    const { data: relatedEntities, isLoading, isError } = useQuery({
        queryKey,
        queryFn: () => getRelatedEntities(sourceEntityType, sourceEntityId, targetEntityType),
    });

    const invalidateRelations = useCallback(() => {
        queryClient.invalidateQueries({ queryKey });
    }, [queryClient, queryKey]);

    const handleAddRelation = useCallback(async (targetEntityId: number, relationTypeId: number | undefined) => {
        setIsAdding(true);
        try {
            await createInternalRelation(
                sourceEntityType,
                sourceEntityId,
                targetEntityType,
                targetEntityId,
                relationTypeId
            );
            invalidateRelations();
            setShowAddForm(false);
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add relation');
        } finally {
            setIsAdding(false);
        }
    }, [sourceEntityType, sourceEntityId, targetEntityType, invalidateRelations, showNotification]);

    const handleRemoveRelation = useCallback(async (relationId: number) => {
        setRemovingIds(prev => new Set(prev).add(relationId));
        try {
            await deleteInternalRelation(relationId);
            invalidateRelations();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove relation');
        } finally {
            setRemovingIds(prev => {
                const next = new Set(prev);
                next.delete(relationId);
                return next;
            });
        }
    }, [invalidateRelations, showNotification]);

    const count = relatedEntities?.length ?? 0;
    const sectionTitle = `${title} (${isLoading ? '...' : count})`;

    return (
        <FoldableSection title={sectionTitle} defaultOpen={defaultOpen}>
            {isLoading && <div className={styles.loading}>Loading {ENTITY_TYPE_LABELS[targetEntityType]}...</div>}

            {isError && <div className={styles.error}>Failed to load related {ENTITY_TYPE_LABELS[targetEntityType]}</div>}

            {!isLoading && !isError && relatedEntities && (
                relatedEntities.length === 0 ? (
                    <div className={styles.empty}>No related {ENTITY_TYPE_LABELS[targetEntityType]}</div>
                ) : (
                    <div className={styles.list}>
                        {relatedEntities.map((entity: RelatedEntityDTO) => (
                            <div key={`${entity.id}-${entity.relationId}`} className={styles.item}>
                                <div className={styles.itemInfo}>
                                    <span className={styles.entityName}>{entity.name}</span>
                                    {entity.relationTypeName && (
                                        <span className={styles.relationType}>{entity.relationTypeName}</span>
                                    )}
                                </div>
                                <button
                                    onClick={() => handleRemoveRelation(entity.relationId)}
                                    disabled={removingIds.has(entity.relationId)}
                                    className={styles.removeButton}
                                    title="Remove relation"
                                >
                                    {removingIds.has(entity.relationId) ? '...' : '\u00d7'}
                                </button>
                            </div>
                        ))}
                    </div>
                )
            )}

            {/* Add relation form / button */}
            {!isLoading && !isError && (
                showAddForm ? (
                    <AddRelationForm
                        sourceEntityType={sourceEntityType}
                        targetEntityType={targetEntityType}
                        onSubmit={handleAddRelation}
                        onCancel={() => setShowAddForm(false)}
                        isSubmitting={isAdding}
                    />
                ) : (
                    <button
                        onClick={() => setShowAddForm(true)}
                        className={styles.addRelationButton}
                    >
                        + Add {targetEntityType}
                    </button>
                )
            )}
        </FoldableSection>
    );
};
