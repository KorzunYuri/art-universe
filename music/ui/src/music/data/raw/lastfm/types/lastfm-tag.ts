import {BaseLastfmEntity} from "./lastfm-base-entity.ts";
import type {Category} from "@/music/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";

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
