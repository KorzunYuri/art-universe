import { BaseRawEntity } from "@/music-universe/shared/types/entities.ts";
import type { LastfmEntity } from "./lastfm-entity";
import type {Category, MasterEntityType} from "@/music-universe/music-data/types/master-entities";

export interface LastfmTagDto {
    id: number;
    name: string;
    url: string | null;
    approvalStatus: number;
    usageCount: number | null;
    usageUsersCount: number | null;
}

/**
 * LastFM Tag entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTag extends BaseRawEntity<Category> implements LastfmEntity<Category> {
    url: string | null;
    approvalStatus: number;
    usageCount: number | null;
    usageUsersCount: number | null;

    constructor(data: LastfmTagDto, masterEntity?: Category) {
        super(data.id, data.name, masterEntity);
        this.url = data.url;
        this.approvalStatus = data.approvalStatus;
        this.usageCount = data.usageCount;
        this.usageUsersCount = data.usageUsersCount;
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
export function createLastfmTag(data: LastfmTagDto, masterEntity?: Category): LastfmTag {
    return new LastfmTag(data, masterEntity);
}
