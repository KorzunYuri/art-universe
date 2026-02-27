import {
    BaseRawEntity,
    type MasterEntityMap,
    type MasterEntityType
} from "@/music/shared/types/entities.ts";
import type { ApprovalStatusType } from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import type { DataSource } from "@/music/data/raw/shared/types/data-sources.ts";
import type { LastfmEntity } from "./lastfm-interfaces.ts";

/**
 * Base class for all LastFM entities
 */
export abstract class BaseLastfmEntity<T extends MasterEntityType> 
    extends BaseRawEntity<T> 
    implements LastfmEntity<T> {
    
    approvalStatus: ApprovalStatusType;

    constructor(
        id: number,
        name: string,
        approvalStatus: ApprovalStatusType,
        masterEntity?: MasterEntityMap[T]
    ) {
        super(id, name, masterEntity);
        this.approvalStatus = approvalStatus;
    }

    /**
     * Returns the data source for this entity
     * Implemented once for all LastFM entities
     */
    getDataSource(): DataSource {
        return 'lastfm';
    }

    /**
     * Sets the approval status for this entity
     * @param approvalStatus New approval status
     */
    setApprovalStatus(approvalStatus: ApprovalStatusType): void {
        this.approvalStatus = approvalStatus;
    }
}
