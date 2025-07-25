// hooks
import {useState, memo} from "react";
// components
import {
    ApprovalToggle,
    ExternalLink,
    ReadonlyAttr,
    EntityTagPanel,
    type BaseEntityTableRow
} from "@/music-universe/shared/components";
// types
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {ApprovalStatus, type ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from "./LastfmArtistsTableRow.module.scss";
import {useLastfmEntity} from "@/music-universe/sources/lastfm/query/useLastfmEntity.tsx";
import {updateRawEntityApprovalStatus} from "@/music-universe/sources/shared/api/approval.tsx";
import {EntityBinding} from "@/music-universe/sources/lastfm/components";

interface LastfmArtistTableRowProps extends BaseEntityTableRow {
    entityId: number
}

export const LastfmArtistsTableRow = memo((
    {
        entityId
    }: LastfmArtistTableRowProps) => {
    // TODO generify component and make dataSource & entityType props or fields
    const dataSource: DataSource = 'lastfm';
    const entityType: MasterEntityType = 'artist';

    const [isApproving, setIsApproving] = useState(false);
    const [isTagPanelOpen, setIsTagPanelOpen] = useState(false);

    const {
        entity,
        updateEntity,
        invalidateEntity,
        isLoading,
        isError,
        error
    } = useLastfmEntity(entityType, entityId);

    const toggleTagPanel = () => {
        setIsTagPanelOpen(!isTagPanelOpen);
    };

    // If entity is loading, show loading state
    if (isLoading) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${artistTableStyles.name}`}>
                    Loading...
                </div>
            </div>
        )
    }

    if (!entity) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${artistTableStyles.name}`}>
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
        <>
            <div
                key={entity.id}
                className={`${sharedTableStyles.row} ${isTagPanelOpen ? styles.activeRow : ''}`}
                onClick={toggleTagPanel}
            >
                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.name}`}>
                    {entity.url && <ExternalLink href={entity.url} label={entity.name}/>}
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.mbid}`}>
                    {entity.mbid && <ExternalLink
                        href={`${LastfmConfig.mbBaseUrls.artist}${entity.mbid}`}
                        label="MusicBrainz"/>}
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.status}`}
                     onClick={(e) => e.stopPropagation()}>
                    <ApprovalToggle
                        status={entity.approvalStatus}
                        onChange={setApprovalStatus}
                        disabled={isApproving}
                    />
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.binding}`}
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

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.count}`}>
                    <ReadonlyAttr value={entity.playCount}/>
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.count}`}>
                    <ReadonlyAttr value={entity.listenersCount}/>
                </div>
            </div>
            
            {isTagPanelOpen && (
                <div className={styles.tagPanelContainer}>
                    <EntityTagPanel
                        entityType='artist'
                        entityId={entity.id}
                        entityApprovalStatus={entity.approvalStatus}
                        tagPageBaseUrl="/lastfm/tags/"
                        onClose={() => setIsTagPanelOpen(false)}
                    />
                </div>
            )}
        </>
    );
});
