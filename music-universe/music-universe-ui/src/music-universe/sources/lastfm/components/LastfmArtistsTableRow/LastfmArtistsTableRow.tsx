// hooks
import {useState, memo, useEffect} from "react";
// components
import {
    EntityBinding,
    ExternalLink,
    ReadonlyAttr,
    EntityTagPanel,
    type RawEntityTableRow
} from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmArtist } from "@/music-universe/sources/lastfm/types";
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from "./LastfmArtistsTableRow.module.scss";
import {updateApprovalStatus} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import {useLastfmArtist} from "@/music-universe/sources/lastfm/query/useLastfmArtist.tsx";

interface LastfmArtistTableRowProps extends RawEntityTableRow<LastfmArtist> {
    entityId: number
}

export const LastfmArtistsTableRow = memo((
    {
        entityId
    }: LastfmArtistTableRowProps) =>
{
    const [isApproving, setIsApproving] = useState(false);
    const [isTagPanelOpen, setIsTagPanelOpen] = useState(false);

    const {
        entity,
        isLoading,
        isError,
        errorMessage
    } = useLastfmArtist(entityId);

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
                    No entity found
                </div>
            </div>
        )
    }

    function onStatusChange(newStatus: number) {
        setIsApproving(true);
        updateApprovalStatus(entity, newStatus)
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
