import {ApprovalStatus, type ApprovalStatusType} from '@/music/data/raw/lastfm/constants/approvalStatus.ts';
import styles from './EntityTagItem.module.scss';

export const TagRelationStatus = {
  NOT_APPROVED: 1,  // gray - not approved
  NOT_BOUND: 2,     // orange - entity or tag are not bound to master
  PENDING: 3,       // yellow - both entity and tag are bound to master but the relation itself is not bound
  APPROVED: 4       // green - entity-tag relation is bound to master
} as const;

export type TagRelationStatus = typeof TagRelationStatus[keyof typeof TagRelationStatus];

export interface EntityTagItemProps {
  id: number;
  name: string;
  tagApprovalStatus: ApprovalStatusType;
  entityApprovalStatus: ApprovalStatusType;
  isBound: boolean;
  usageCount: number;
  relationStatus: number;
  isSourceBound?: boolean;
  isTargetBound?: boolean;
  tagPageUrl?: string;
  onApprove?: (tagId: number) => void;
  onUnapprove?: (tagId: number) => void;
  isProcessing?: boolean;
  readOnly?: boolean;
}

export const EntityTagItem = ({
  id,
  name,
  tagApprovalStatus,
  entityApprovalStatus,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  isBound,
  usageCount,
  relationStatus,
  isSourceBound,
  isTargetBound,
  tagPageUrl,
  onApprove,
  onUnapprove,
  isProcessing = false,
  readOnly = false
}: EntityTagItemProps) => {
  // Truncate name to 20 characters
  const displayName = name.length > 20 ? `${name.substring(0, 20)}...` : name;
  
  // Get status message for tooltip
  const getStatusMessage = (): string => {
    switch (relationStatus) {
      case TagRelationStatus.NOT_APPROVED:
        if (tagApprovalStatus !== ApprovalStatus.APPROVED && 
            tagApprovalStatus !== ApprovalStatus.AUTOAPPROVED) {
          return "Tag is not approved in LastFM. Click to search for this tag.";
        }
        if (entityApprovalStatus !== ApprovalStatus.APPROVED && 
            entityApprovalStatus !== ApprovalStatus.AUTOAPPROVED) {
          return "Entity is not approved in LastFM.";
        }
        return "Both tag and entity need to be approved in LastFM.";
      
      case TagRelationStatus.NOT_BOUND:
        if (!isTargetBound) {
          return "Tag is not bound to a category in Music Data. Click to search for this tag.";
        }
        if (!isSourceBound) {
          return "Entity is not bound in Music Data.";
        }
        return "Both tag and entity need to be bound to Music Data.";
      
      case TagRelationStatus.PENDING:
        return "Click to approve relation in Music Data.";
      
      case TagRelationStatus.APPROVED:
        return "Relation is approved in Music Data. Click to unapprove.";
      
      default:
        return "Unknown status";
    }
  };
  
  // Determine tag style class
  const getTagClass = (): string => {
    switch (relationStatus) {
      case TagRelationStatus.NOT_APPROVED:
        return styles.disabled;
      case TagRelationStatus.NOT_BOUND:
        return styles['not-bound'];
      case TagRelationStatus.PENDING:
        return styles.pending;
      case TagRelationStatus.APPROVED:
        return styles.approved;
      default:
        return '';
    }
  };
  
  // Handle click on tag
  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();

    if (isProcessing) return;

    // In readOnly mode, always navigate to the tag page
    if (readOnly) {
      if (tagPageUrl) {
        window.open(tagPageUrl, '_blank');
      }
      return;
    }

    // If tag is not approved or not bound, navigate to tags page with search parameter
    if ((relationStatus === TagRelationStatus.NOT_APPROVED || relationStatus === TagRelationStatus.NOT_BOUND) && tagPageUrl) {
      // Extract base URL (remove the tag ID from the end)
      const baseUrl = tagPageUrl.substring(0, tagPageUrl.lastIndexOf('/'));
      // Navigate to tags page with search parameter
      window.open(`${baseUrl}?search=${encodeURIComponent(name)}`, '_blank');
      return;
    }

    // If relation is approved, unapprove it
    if (relationStatus === TagRelationStatus.APPROVED) {
      onUnapprove?.(id);
      return;
    }

    // If relation is pending, approve it
    if (relationStatus === TagRelationStatus.PENDING) {
      onApprove?.(id);
      return;
    }
  };
  
  return (
    <div 
      className={`${styles.tagItem} ${getTagClass()} ${isProcessing ? styles.processing : ''}`}
      title={`${displayName} - ${getStatusMessage()}`}
      onClick={handleClick}
    >
      <span className={styles.tagName}>{displayName}</span>
      <span className={styles.tagCount}>({usageCount})</span>
      {isProcessing && (
        <span className={styles.processingIndicator}>...</span>
      )}
    </div>
  );
};
