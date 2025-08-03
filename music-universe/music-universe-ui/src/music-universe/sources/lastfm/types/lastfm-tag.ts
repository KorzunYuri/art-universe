import {BaseLastfmEntity} from "./lastfm-entity";
import type {Category} from "@/music-universe/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

/**
 * LastFM Tag entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTag extends BaseLastfmEntity<"category"> {
    url: string | null;
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
        super(id, name, approvalStatus, masterEntity);
        this.url = url;
        this.usageCount = usageCount;
        this.usageUsersCount = usageUsersCount;
    }

    getEntityType(): "category" {
        return "category";
    }
}
