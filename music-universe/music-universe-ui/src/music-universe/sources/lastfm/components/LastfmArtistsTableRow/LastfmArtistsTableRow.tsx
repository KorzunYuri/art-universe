// hooks
import {useState, memo} from "react";
// components
import {
    ExternalLink,
    ReadonlyAttr,
    type BaseEntityTableRow
} from "@/music-universe/shared/components";
import {QuizBinding} from "@/music-universe/music-quiz/components";
import {ApprovalToggle, EntityBinding, EntityTagPanel} from "@/music-universe/sources/lastfm/components";
// types
import type { DataSource } from "@/music-universe/sources/shared/types/data-sources.ts";
import type { MasterEntityType } from "@/music-universe/shared/types/entities.ts";
// services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import { useLastfmEntity } from "@/music-universe/sources/lastfm/hooks/useLastfmEntity";
import { useLastfmEntityApproval } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityApproval.ts";
// styles
import sharedTableStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from "./LastfmArtistsTableRow.module.scss";

interface LastfmArtistTableRowProps extends BaseEntityTableRow {
}

export const LastfmArtistsTableRow = memo((
    {
        entityId
    }: LastfmArtistTableRowProps
) => {

    // TODO generify component and make dataSource & entityType props or fields
    const dataSource: DataSource = 'lastfm';
    const entityType: MasterEntityType = 'artist';

    const [isTagPanelOpen, setIsTagPanelOpen] = useState(false);

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

    const toggleTagPanel = () => {
        setIsTagPanelOpen(!isTagPanelOpen);
    };

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

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.masterBinding}`}
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
                        entityType="artist"
                        masterId={entity.getMasterEntity()?.id ?? null}
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
})
