import type {MasterEntityType} from "@/music/shared/types/entities.ts";
import { updateApprovalStatus as updateApprovalStatus_Lastfm } from "@/music/data/raw/lastfm/api/lastfm-common.ts";
import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import type {LastfmSupportedEntityType} from "@/music/data/raw/lastfm/types/lastfm-interfaces.ts";

export type ApprovalDataSource = 'lastfm';

type DataSourceApprovalStatus = {
    lastfm: ApprovalStatusType
}

export function updateRawEntityApprovalStatus<DS extends ApprovalDataSource>(
    dataSource: DS,
    entityType: MasterEntityType,
    entityId: number,
    newStatus: DataSourceApprovalStatus[DS]
) : Promise<void> {

    switch (dataSource) {
        case "lastfm":
            return updateApprovalStatus_Lastfm(entityType as LastfmSupportedEntityType, entityId, newStatus);
    }
}