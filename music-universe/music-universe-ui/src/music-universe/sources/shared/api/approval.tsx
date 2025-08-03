import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import { updateApprovalStatus as updateApprovalStatus_Lastfm } from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

type DataSourceApprovalStatus = {
    lastfm: ApprovalStatusType
}

export function updateRawEntityApprovalStatus<DS extends DataSource>(
    dataSource: DS,
    entityType: MasterEntityType,
    entityId: number,
    newStatus: DataSourceApprovalStatus[DS]
) : Promise<void> {

    switch (dataSource) {
        case "lastfm":
            return updateApprovalStatus_Lastfm(entityType, entityId, newStatus)
    }
}