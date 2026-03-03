import { useState, useEffect } from 'react';
import { EntityTagItem, TagRelationStatus } from '@/music/data/raw/lastfm/components/EntityTagItem/EntityTagItem.tsx';
import { fetchEntityTags, type EntityTagDto } from '@/music/data/raw/lastfm/api/lastfm-tags.ts';
import {
  findBoundExternalRelations,
  createInternalRelation,
  deleteInternalRelation
} from '@/music/data/master/api/music-data-relations.ts';
import {ApprovalStatus, type ApprovalStatusType} from '@/music/data/raw/lastfm/constants/approvalStatus.ts';
import styles from './EntityTagPanel.module.scss';
import type {MasterEntityType} from "@/music/shared/types/entities.ts";

export interface EntityTagPanelProps {
  entityType: MasterEntityType;
  entityId: number;
  entityApprovalStatus: ApprovalStatusType;
  tagPageBaseUrl?: string; // Base URL for tag pages, e.g. '/music/data/raw/lastfm/tags/'
  onClose?: () => void;
}

interface TagWithRelation extends EntityTagDto {
  isBound: boolean;
  relationStatus: TagRelationStatus;
  isSourceBound?: boolean;
  isTargetBound?: boolean;
  isProcessing?: boolean;
  sourceInternalId?: number | null;
  targetInternalId?: number | null;
  internalRelationId?: number | null;
}

export const EntityTagPanel = ({
  entityType,
  entityId,
  entityApprovalStatus,
  tagPageBaseUrl,
  onClose
}: EntityTagPanelProps) => {

  // TODO rewrite to TanQuery

  const [tags, setTags] = useState<TagWithRelation[]>([]);
  const [loading, setLoading] = useState(false);

  // Load tags and their relations
  const loadTagsAndRelations = async () => {
    setLoading(true);
    try {
      // 1. Load tags from LastFM API
      const entityTags = await fetchEntityTags(entityType, entityId, {
        minUsageCount: 0 // Show all tags, including those with null usage count
      });

      console.log('EntityTags from LastFM:', entityTags);

      if (entityTags.length === 0) {
        setTags([]);
        return;
      }

      // 2. Extract tag IDs for bound relations check
      const tagIds = entityTags.map(tag => tag.id);

      // 3. Check which relations are bound in music-data
      const boundRelationsStatus = await findBoundExternalRelations(
        'lastfm',
        entityType,
        entityId,
        'category',
        tagIds
      );

      console.log('BoundRelationsStatus from Music-data:', boundRelationsStatus);

      // 4. Combine data and determine relation status
      const tagsWithRelations = entityTags.map(tag => {
        const binding = boundRelationsStatus.targetBindings.find(
          binding => binding.targetExternalId === tag.id
        );

        const isBound = binding?.isInternalRelationBound || false;
        const isSourceBound = boundRelationsStatus.isSourceEntityBound;
        const isTargetBound = binding?.isTargetEntityBound || false;

        // Determine relation status
        let relationStatus: TagRelationStatus;

        const isTagApproved = tag.tagApprovalStatus === ApprovalStatus.APPROVED ||
                             tag.tagApprovalStatus === ApprovalStatus.AUTOAPPROVED;
        const isEntityApproved = entityApprovalStatus === ApprovalStatus.APPROVED ||
                                entityApprovalStatus === ApprovalStatus.AUTOAPPROVED;

        if (!isTagApproved || !isEntityApproved) {
          relationStatus = TagRelationStatus.NOT_APPROVED;
        } else if (!isSourceBound || !isTargetBound) {
          relationStatus = TagRelationStatus.NOT_BOUND;
        } else if (isBound) {
          relationStatus = TagRelationStatus.APPROVED;
        } else {
          relationStatus = TagRelationStatus.PENDING;
        }

        return {
          ...tag,
          isBound,
          relationStatus,
          isSourceBound,
          isTargetBound,
          isProcessing: false,
          sourceInternalId: boundRelationsStatus.sourceInternalId,
          targetInternalId: binding?.targetInternalId ?? null,
          internalRelationId: binding?.internalRelationId ?? null
        };
      });

      console.log('Tags with relations:', tagsWithRelations);

      // 5. Sort by usage count (descending)
      const sortedTags = [...tagsWithRelations].sort((a, b) =>
        (b.usageCount || 0) - (a.usageCount || 0)
      );

      setTags(sortedTags);
    } catch (error) {
      console.error(`❌ Error loading ${entityType.toLowerCase()} tags:`, error);
    } finally {
      setLoading(false);
    }
  };

  // Load tags on component mount
  useEffect(() => {
    loadTagsAndRelations();
  }, [entityType, entityId, entityApprovalStatus]);

  // Handle approving relation
  const handleApprove = async (tagId: number) => {
    const tag = tags.find(t => t.id === tagId);
    if (!tag?.sourceInternalId || !tag?.targetInternalId) {
      console.error(`❌ Cannot approve: missing internal IDs for tag ${tagId}`);
      return;
    }

    // Set processing state
    setTags(prevTags => prevTags.map(t =>
      t.id === tagId ? { ...t, isProcessing: true } : t
    ));

    try {
      // Call API to create internal relation
      const relationIds = await createInternalRelation(
        entityType,
        tag.sourceInternalId,
        'category',
        tag.targetInternalId
      );

      if (relationIds && relationIds.length > 0) {
        // Update all tags bound to the same master category
        const targetId = tag.targetInternalId;
        setTags(prevTags => prevTags.map(t =>
          t.targetInternalId === targetId && t.isTargetBound
            ? {
                ...t,
                isBound: true,
                relationStatus: TagRelationStatus.APPROVED,
                isProcessing: false,
                internalRelationId: relationIds[0]
              }
            : t
        ));
        console.log(`✅ Successfully created internal relation between ${entityType} ${tag.sourceInternalId} and category ${targetId}`);
      } else {
        // Reset processing state on failure
        setTags(prevTags => prevTags.map(t =>
          t.id === tagId ? { ...t, isProcessing: false } : t
        ));
        console.error(`❌ Failed to create internal relation for tag ${tagId}`);
      }
    } catch (error) {
      console.error(`❌ Error creating internal relation:`, error);
      // Reset processing state on error
      setTags(prevTags => prevTags.map(t =>
        t.id === tagId ? { ...t, isProcessing: false } : t
      ));
    }
  };

  // Handle unapproving relation
  const handleUnapprove = async (tagId: number) => {
    const tag = tags.find(t => t.id === tagId);
    if (!tag?.internalRelationId) {
      console.error(`❌ Cannot unapprove: missing internal relation ID for tag ${tagId}`);
      return;
    }

    // Set processing state
    setTags(prevTags => prevTags.map(t =>
      t.id === tagId ? { ...t, isProcessing: true } : t
    ));

    try {
      // Call API to delete internal relation
      const success = await deleteInternalRelation(
        tag.internalRelationId,
        entityType,
        'category'
      );

      if (success) {
        // Update all tags bound to the same master category
        const targetId = tag.targetInternalId;
        setTags(prevTags => prevTags.map(t =>
          t.targetInternalId === targetId && t.isTargetBound
            ? {
                ...t,
                isBound: false,
                relationStatus: TagRelationStatus.PENDING,
                isProcessing: false,
                internalRelationId: null
              }
            : t
        ));
        console.log(`✅ Successfully deleted internal relation for category ${targetId}`);
      } else {
        // Reset processing state on failure
        setTags(prevTags => prevTags.map(t =>
          t.id === tagId ? { ...t, isProcessing: false } : t
        ));
        console.error(`❌ Failed to delete internal relation for tag ${tagId}`);
      }
    } catch (error) {
      console.error(`❌ Error deleting internal relation:`, error);
      // Reset processing state on error
      setTags(prevTags => prevTags.map(t =>
        t.id === tagId ? { ...t, isProcessing: false } : t
      ));
    }
  };

  return (
    <div className={styles.tagPanel}>
      {loading ? (
        <div className={styles.loading}>Loading tags...</div>
      ) : (
        <div className={styles.tagContainer}>
          <button
            onClick={() => loadTagsAndRelations()}
            className={styles.refreshButton}
            title="Refresh tags"
            disabled={loading}
          >
            🔄
          </button>

          <div className={styles.tagList}>
            {tags.length === 0 ? (
              <div className={styles.noTags}>No tags found</div>
            ) : (
              tags.map(tag => (
                <EntityTagItem
                  key={tag.id}
                  id={tag.id}
                  name={tag.name}
                  tagApprovalStatus={tag.tagApprovalStatus}
                  entityApprovalStatus={entityApprovalStatus}
                  isBound={tag.isBound}
                  usageCount={tag.usageCount || 0}
                  relationStatus={tag.relationStatus}
                  isSourceBound={tag.isSourceBound}
                  isTargetBound={tag.isTargetBound}
                  tagPageUrl={tagPageBaseUrl ? `${tagPageBaseUrl}${tag.id}` : undefined}
                  onApprove={handleApprove}
                  onUnapprove={handleUnapprove}
                  isProcessing={tag.isProcessing}
                />
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
