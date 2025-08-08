// hooks
import { useState, memo } from "react";
// components
import {
    ExternalLink,
    ReadonlyAttr,
    type BaseEntityTableRow
} from "@/music-universe/shared/components";
import {ApprovalToggle, EntityBinding} from "@/music-universe/sources/lastfm/components";
// types
import type { DataSource } from "@/music-universe/sources/shared/types/data-sources.ts";
import type { MasterEntityType } from "@/music-universe/shared/types/entities.ts";
// services
import { useLastfmEntity } from "@/music-universe/sources/lastfm/hooks/useLastfmEntity";
import { updateRawEntityApprovalStatus } from "@/music-universe/sources/shared/api/approval";
// constants
import { ApprovalStatus, type ApprovalStatusType } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// styles
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import tagTableStyles from "../LastfmTagsTable/LastfmTagsTable.module.css";

interface LastfmTagTableRowProps extends BaseEntityTableRow {
}

export const LastfmTagsTableRow = memo((
    {
        entityId
    }: LastfmTagTableRowProps) =>
{
    // TODO generify component and make dataSource & entityType props or fields
    const dataSource: DataSource = 'lastfm';
    const entityType: MasterEntityType = 'category';

    const [isApproving, setIsApproving] = useState(false);

    const {
        entity,
        updateEntity,
        invalidateEntity,
        isLoading,
        isError,
        error
    } = useLastfmEntity(entityType, entityId);

    // If entity is loading, show loading state
    if (isLoading) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${tagTableStyles.name}`}>
                    Loading...
                </div>
            </div>
        )
    }

    if (!entity) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${tagTableStyles.name}`}>
                    {isError && error ? error.message : 'No entity found'}
                </div>
            </div>
        )
    }

    const ensureIsValidForBinding = async (hasMasterExisted: boolean) => {
        if (!entity) return false;
        if (entity.approvalStatus === ApprovalStatus.APPROVED) return true;
        if (entity.approvalStatus === ApprovalStatus.PENDING) {
            setApprovalStatus(ApprovalStatus.APPROVED);
            return true;
        }
        // TODO show warning in popup instead of logging
        console.log(`Entity has invalid status for binding: ${entity.approvalStatus}`);
        return false;
    }

    const setApprovalStatus = (newStatus: ApprovalStatusType) => {
        console.log(`new status ${newStatus} for entity ${entity?.id}`)
        if (!entity) return;
        setIsApproving(true);
        updateRawEntityApprovalStatus(dataSource, entity.getEntityType(), entity.id, newStatus)
            .then(() => {
                entity.setApprovalStatus(newStatus);
                updateEntity(entity);
            })
            .finally(() => {
                setIsApproving(false);
            });
    }

    return (
        <div key={entity.id}
             className={sharedTableStyles.row}
        >
            <div className={`${sharedTableStyles.cell} ${tagTableStyles.name}`}>
                {entity.url ? (
                    <ExternalLink href={entity.url} label={entity.name}/>
                ) : (
                    <span>{entity.name}</span>
                )}
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.status}`}
                 onClick={(e) => e.stopPropagation()}>
                <ApprovalToggle
                    status={entity.approvalStatus}
                    onChange={setApprovalStatus}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.masterBinding}`}
                 onClick={(e) => e.stopPropagation()}>
                <EntityBinding
                    dataSource={dataSource}
                    entityType={entityType}
                    entityId={entityId}
                    onBeforeBind={ensureIsValidForBinding}
                    onAfterBind={invalidateEntity}
                    onAfterUnbind={invalidateEntity}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.count}`}>
                <ReadonlyAttr value={entity.usageCount}/>
            </div>

            <div className={`${sharedTableStyles.cell} ${tagTableStyles.count}`}>
                <ReadonlyAttr value={entity.usageUsersCount}/>
            </div>
        </div>
    );
});
