import {BaseRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {LastfmEntity} from "./lastfm-entity";
import type {Category} from "@/music-universe/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

/**
 * LastFM Tag entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTag extends BaseRawEntity<"category"> implements LastfmEntity<"category"> {
    url: string | null;
    approvalStatus: ApprovalStatusType;
    usageCount: number | null;
    usageUsersCount: number | null;

    constructor(
        id: number,
        name: string,
        url: string | null,
        approvalStatus: ApprovalStatusType,
        usageCount: number | null,
        usageUsersCount: number | null,
        masterEntity?: Category
    ) {
        super(id, name, masterEntity);
        this.url = url;
        this.approvalStatus = approvalStatus;
        this.usageCount = usageCount;
        this.usageUsersCount = usageUsersCount;
    }

    getEntityType(): "category" {
        return "category";
    }

    setApprovalStatus(approvalStatus: ApprovalStatusType): void {
        this.approvalStatus = approvalStatus;
    }
}

/**
 * Factory function to create LastfmTag from API response
 */
export function createLastfmTag(
    id: number,
    name: string,
    url: string | null,
    approvalStatus: ApprovalStatusType,
    usageCount: number | null,
    usageUsersCount: number | null,
    masterEntity?: Category
): LastfmTag {
    return new LastfmTag(
        id,
        name,
        url,
        approvalStatus,
        usageCount,
        usageUsersCount,
        masterEntity
    );
}
