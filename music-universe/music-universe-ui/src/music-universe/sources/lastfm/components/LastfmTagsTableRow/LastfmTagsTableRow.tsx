// hooks
import { memo } from "react";
import { useLastfmEntity } from "@/music-universe/sources/lastfm/hooks/useLastfmEntity.tsx";
import { useLastfmEntityApproval } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityApproval.ts";
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

    const {
        entity,
        updateEntity,
        invalidateEntity,
        isLoading,
        isError,
        error
    } = useLastfmEntity(entityType, entityId);

    const {
        isApproving,
        setApprovalStatus,
        ensureIsValidForBinding
    } = useLastfmEntityApproval(entity, dataSource, updateEntity);

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
