import { BaseRawEntity } from "@/music-universe/shared/types/entities.ts";
import type { LastfmEntity } from "./lastfm-entity";
import type {Category, MasterEntityType} from "@/music-universe/music-data/types/master-entities";

/**
 * LastFM Tag entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTag extends BaseRawEntity<Category> implements LastfmEntity<Category> {
    url: string | null;
    approvalStatus: number;
    usageCount: number | null;
    usageUsersCount: number | null;

    constructor(
        id: number,
        name: string,
        url: string | null,
        approvalStatus: number,
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

    getEntityType(): MasterEntityType {
        return "category";
    }

    setApprovalStatus(approvalStatus: number): void {
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
    approvalStatus: number,
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
