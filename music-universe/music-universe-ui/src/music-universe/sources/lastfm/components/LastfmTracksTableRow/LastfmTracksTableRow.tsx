// hooks
import {useCallback, useState} from "react";
import {useLastfmEntity} from "@/music-universe/sources/lastfm/hooks/useLastfmEntity.tsx";
// components
import {
    type BaseEntityTableRow, EntityTagPanel,
    ExternalLink,
    ReadonlyAttr
} from "@/music-universe/shared/components";
import {QuizBinding} from "@/music-universe/music-quiz/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import {updateRawEntityApprovalStatus} from "@/music-universe/sources/shared/api/approval.tsx";
// types
import {ApprovalToggle, EntityBinding} from "@/music-universe/sources/lastfm/components";
import {ApprovalStatus, type ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
// styles
import styles from "@/music-universe/sources/lastfm/components/LastfmArtistsTableRow/LastfmArtistsTableRow.module.scss";
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import trackTableStyles from "../LastfmTracksTable/LastfmTracksTable.module.css";
import artistTableStyles from "@/music-universe/sources/lastfm/components/LastfmArtistsTable/LastfmArtistsTable.module.css";

interface LastfmTrackTableRowProps extends BaseEntityTableRow {
}

export const LastfmTracksTableRow = (
    {
        entityId
    }: LastfmTrackTableRowProps) =>
{
    const dataSource: DataSource = 'lastfm';
    const entityType: MasterEntityType = 'track';

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

    const setApprovalStatus = useCallback((newStatus: ApprovalStatusType) => {
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
    }, [entity, updateEntity]);

    const ensureIsValidForBinding = useCallback(async (hasMasterExisted: boolean) => {
        if (!entity) return false;
        if (entity.approvalStatus === ApprovalStatus.APPROVED) return true;
        if (entity.approvalStatus === ApprovalStatus.PENDING) {
            setApprovalStatus(ApprovalStatus.APPROVED);
            return true;
        }
        // TODO show warning in popup instead of logging
        console.log(`Entity has invalid status for binding: ${entity.approvalStatus}`);
        return false;
    }, [entity, setApprovalStatus]);

    // If entity is loading, show loading state
    if (isLoading) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${trackTableStyles.name}`}>
                    Loading...
                </div>
            </div>
        )
    }

    if (!entity) {
        return (
            <div className={sharedTableStyles.row}>
                <div className={`${sharedTableStyles.cell} ${trackTableStyles.name}`}>
                    {isError && error ? error.message : 'No entity found'}
                </div>
            </div>
        )
    }

    const toggleTagPanel = () => {
        setIsTagPanelOpen(!isTagPanelOpen);
    };

    return (
        <div key={entity.id}
             className={sharedTableStyles.row}
             onClick={toggleTagPanel}
        >
            <div className={`${sharedTableStyles.cell} ${trackTableStyles.artist}`}>
                {entity.artist && <ReadonlyAttr value={entity.artist.name}/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.name}`}>
                {entity.url && <ExternalLink href={entity.url} label={entity.name}/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.mbid}`}>
                {entity.mbid && <ExternalLink
                    href={`${LastfmConfig.mbBaseUrls.track}${entity.mbid}`}
                    label="MusicBrainz"/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.status}`}
                 onClick={(e) => e.stopPropagation()}>
                <ApprovalToggle
                    status={entity.approvalStatus}
                    onChange={setApprovalStatus}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell}  ${trackTableStyles.masterBinding}`}
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

            <div className={`${sharedTableStyles.cell}  ${artistTableStyles.quizBinding}`}
                 onClick={(e) => e.stopPropagation()}>
                <QuizBinding
                    entityType="track"
                    masterId={entity.getMasterEntity()?.id ?? null}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.count}`}>
                <ReadonlyAttr value={entity.playCount}/>
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.count}`}>
                <ReadonlyAttr value={entity.listenersCount}/>
            </div>

            {isTagPanelOpen && (
                <div className={styles.tagPanelContainer}>
                    <EntityTagPanel
                        entityType='track'
                        entityId={entity.id}
                        entityApprovalStatus={entity.approvalStatus}
                        tagPageBaseUrl="/lastfm/tags/"
                        onClose={() => setIsTagPanelOpen(false)}
                    />
                </div>
            )}
        </div>
    );
};
