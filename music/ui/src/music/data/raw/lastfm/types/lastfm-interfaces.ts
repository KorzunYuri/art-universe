import type { RawEntity, MasterEntityType } from "@/music/shared/types/entities.ts";
import type { Approvable } from "@/music/shared/types/approvable.ts";
import type { ApprovalStatusType } from "@/music/data/raw/lastfm/constants/approvalStatus.ts";

/**
 * Base interface for all LastFM entities
 */
export interface LastfmEntity<T extends MasterEntityType> extends RawEntity<T>, Approvable {
    approvalStatus: ApprovalStatusType;
    setApprovalStatus(approvalStatus: ApprovalStatusType): void;
}

/**
 * Map of LastFM entity types to their corresponding interfaces
 */
export type LastfmSupportedEntityType = Extract<
    MasterEntityType,
    'artist' | 'album' | 'track' | 'category'
>;
