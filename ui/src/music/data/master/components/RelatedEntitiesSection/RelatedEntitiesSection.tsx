import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useNotifications } from '@/shared/hooks';
import { FoldableSection } from '@/shared/components/FoldableSection';
import {
    getRelatedEntities,
    createInternalRelation,
    deleteInternalRelation,
    type RelatedEntityDTO,
} from '@/music/data/master/api/music-data-relations';
import { AddRelationForm } from './AddRelationForm';
import { relationKeys } from '@/music/shared/utils/query-keys';
import type { MasterEntityType } from '@/music/shared/types/entities';
import { getMasterEntityUrl } from '@/music/data/master/utils/masterEntityUrl';
import styles from './RelatedEntitiesSection.module.scss';

interface RelatedEntitiesSectionProps {
    title: string;
    sourceEntityType: MasterEntityType;
    sourceEntityId: number;
    targetEntityType: MasterEntityType;
    defaultOpen?: boolean;
    onEntityAction?: (entity: RelatedEntityDTO) => void;
    entityActionLabel?: string;
    actionInProgressIds?: Set<number>;
    onEntitySwap?: (entityA: RelatedEntityDTO, entityB: RelatedEntityDTO) => void;
    swappingRelationIds?: Set<number>;
}

const ENTITY_TYPE_LABELS: Record<MasterEntityType, string> = {
    artist: 'artists',
    album: 'albums',
    track: 'tracks',
    category: 'categories',
    person: 'persons',
};

export const RelatedEntitiesSection = ({
    title,
    sourceEntityType,
    sourceEntityId,
    targetEntityType,
    defaultOpen = false,
    onEntityAction,
    entityActionLabel,
    actionInProgressIds,
    onEntitySwap,
    swappingRelationIds,
}: RelatedEntitiesSectionProps) => {
    const queryClient = useQueryClient();
    const { showNotification } = useNotifications();
    const [showAddForm, setShowAddForm] = useState(false);
    const [isAdding, setIsAdding] = useState(false);
    const [removingIds, setRemovingIds] = useState<Set<number>>(new Set());

    const queryKey = relationKeys.entities(sourceEntityType, sourceEntityId, targetEntityType);
    const reverseQueryKey = (targetEntityId: number) =>
        relationKeys.entities(targetEntityType, targetEntityId, sourceEntityType);

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
            queryClient.invalidateQueries({ queryKey: reverseQueryKey(targetEntityId) });
            setShowAddForm(false);
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to add relation');
        } finally {
            setIsAdding(false);
        }
    }, [sourceEntityType, sourceEntityId, targetEntityType, invalidateRelations, showNotification, queryClient]);

    const handleRemoveRelation = useCallback(async (relationId: number) => {
        const targetEntityId = relatedEntities?.find(e => e.relationId === relationId)?.id;
        setRemovingIds(prev => new Set(prev).add(relationId));
        try {
            await deleteInternalRelation(relationId);
            invalidateRelations();
            if (targetEntityId != null) {
                queryClient.invalidateQueries({ queryKey: reverseQueryKey(targetEntityId) });
            }
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to remove relation');
        } finally {
            setRemovingIds(prev => {
                const next = new Set(prev);
                next.delete(relationId);
                return next;
            });
        }
    }, [invalidateRelations, showNotification, relatedEntities, queryClient]);

    const count = relatedEntities?.length ?? 0;
    const sectionTitle = `${title} (${isLoading ? '...' : count})`;

    const displayEntities = onEntitySwap && relatedEntities
        ? [...relatedEntities].sort((a, b) => (a.trackOrder ?? 0) - (b.trackOrder ?? 0))
        : relatedEntities;

    return (
        <FoldableSection title={sectionTitle} defaultOpen={defaultOpen}>
            {isLoading && <div className={styles.loading}>Loading {ENTITY_TYPE_LABELS[targetEntityType]}...</div>}

            {isError && <div className={styles.error}>Failed to load related {ENTITY_TYPE_LABELS[targetEntityType]}</div>}

            {!isLoading && !isError && displayEntities && (
                displayEntities.length === 0 ? (
                    <div className={styles.empty}>No related {ENTITY_TYPE_LABELS[targetEntityType]}</div>
                ) : (
                    <div className={styles.list}>
                        {displayEntities.map((entity: RelatedEntityDTO, index: number, arr: RelatedEntityDTO[]) => {
                            const url = getMasterEntityUrl(targetEntityType, entity.id);
                            const isAlbumTrack = sourceEntityType === 'album' && targetEntityType === 'track';
                            const hasReorderButtons = onEntitySwap != null && entity.trackOrder != null;
                            return (
                                <div key={`${entity.id}-${entity.relationId}`} className={styles.item}>
                                    <div className={styles.itemInfo}>
                                        {isAlbumTrack && entity.trackOrder != null && (
                                            <span className={styles.trackOrder}>{entity.trackOrder}.</span>
                                        )}
                                        {entity.relationTypeName && (
                                            <span className={styles.relationType}>{entity.relationTypeName}</span>
                                        )}
                                        {url ? (
                                            <Link to={url} className={styles.entityName}>{entity.name}</Link>
                                        ) : (
                                            <span className={styles.entityName}>{entity.name}</span>
                                        )}
                                    </div>
                                    {hasReorderButtons && (
                                        <>
                                            <button
                                                onClick={() => onEntitySwap!(entity, arr[index - 1])}
                                                disabled={index === 0 || swappingRelationIds?.has(entity.relationId) || swappingRelationIds?.has(arr[index - 1]?.relationId)}
                                                className={styles.moveButton}
                                                title="Move up"
                                            >↑</button>
                                            <button
                                                onClick={() => onEntitySwap!(entity, arr[index + 1])}
                                                disabled={index === arr.length - 1 || swappingRelationIds?.has(entity.relationId) || swappingRelationIds?.has(arr[index + 1]?.relationId)}
                                                className={styles.moveButton}
                                                title="Move down"
                                            >↓</button>
                                        </>
                                    )}
                                    {onEntityAction && entityActionLabel && (
                                        <button
                                            onClick={() => onEntityAction(entity)}
                                            disabled={actionInProgressIds?.has(entity.id)}
                                            className={styles.actionButton}
                                            title={entityActionLabel}
                                        >
                                            {actionInProgressIds?.has(entity.id) ? '...' : entityActionLabel}
                                        </button>
                                    )}
                                    <button
                                        onClick={() => handleRemoveRelation(entity.relationId)}
                                        disabled={removingIds.has(entity.relationId)}
                                        className={styles.removeButton}
                                        title="Remove relation"
                                    >
                                        {removingIds.has(entity.relationId) ? '...' : '\u00d7'}
                                    </button>
                                </div>
                            );
                        })}
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
