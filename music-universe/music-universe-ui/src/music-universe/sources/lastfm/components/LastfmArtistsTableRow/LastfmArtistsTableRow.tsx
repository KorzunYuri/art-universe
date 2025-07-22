// hooks
import {useState, memo} from "react";
// components
import {
    EntityBinding,
    ExternalLink,
    ReadonlyAttr,
    EntityTagPanel,
    type RawEntityTableRow
} from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// types
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import type {MasterEntityType} from "@/music-universe/music-data/types/master-entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmArtist } from "@/music-universe/sources/lastfm/types";
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from "./LastfmArtistsTableRow.module.scss";
import {useLastfmArtist} from "@/music-universe/sources/lastfm/query/useLastfmArtist.tsx";
import {updateRawEntityApprovalStatus} from "@/music-universe/sources/shared/api/approval.tsx";

interface LastfmArtistTableRowProps extends RawEntityTableRow<LastfmArtist> {
    entityId: number
}

export const LastfmArtistsTableRow = memo((
    {
        entityId
    }: LastfmArtistTableRowProps) =>
{
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
    } = useLastfmArtist(entityType, entityId);

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

    function onStatusChange(newStatus: ApprovalStatusType) {
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

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.status}`} onClick={(e) => e.stopPropagation()}>
                    <ApprovalToggle
                        status={entity.approvalStatus}
                        onChange={onStatusChange}
                        disabled={isApproving}
                    />
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.binding}`} onClick={(e) => e.stopPropagation()}>
                    {/*<EntityBinding*/}
                    {/*    key={`exp-entity-binding-${entity.id}`}*/}
                    {/*    entity={entity}*/}
                    {/*    onBindToExisting={handleBindToExisting}*/}
                    {/*    onCreateAndBind={handleCreateAndBind}*/}
                    {/*    onUnbind={unbindArtist}*/}
                    {/*    onBeforeBind={handleBeforeBind}*/}
                    {/*    onAfterBind={handleAfterBind}*/}
                    {/*    lookupFunction={lookupArtists}*/}
                    {/*    preloadedOptions={preloadedLookupData}*/}
                    {/*    disabled={isApproving}*/}
                    {/*/>*/}
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
                        entityType="ARTIST"
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
