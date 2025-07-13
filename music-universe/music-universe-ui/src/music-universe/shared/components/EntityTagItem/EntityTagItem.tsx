import { ApprovalStatus } from '@/music-universe/sources/lastfm/constants/approvalStatus';
import styles from './EntityTagItem.module.scss';

export const TagRelationStatus = {
  NOT_APPROVED: 1,  // Серый - артист или тег не одобрены в LastFM
  NOT_BOUND: 2,     // Оранжевый - артист или тег не привязаны к music-data
  PENDING: 3,       // Желтый - артист и тег одобрены и привязаны, связь не одобрена
  APPROVED: 4       // Зеленый - связь одобрена
} as const;

export type TagRelationStatus = typeof TagRelationStatus[keyof typeof TagRelationStatus];

export interface EntityTagItemProps {
  id: number;
  name: string;
  tagApprovalStatus: number;
  entityApprovalStatus: number;
  isBound: boolean;
  usageCount: number;
  relationStatus: number;
  isSourceBound?: boolean;
  isTargetBound?: boolean;
  tagPageUrl?: string;
  onApprove?: (tagId: number) => void;
  onUnapprove?: (tagId: number) => void;
  isProcessing?: boolean;
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
  isProcessing = false
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
