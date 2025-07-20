// hooks
import { useState, memo } from "react";
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
import { bindArtistToExisting, createAndBindArtist, unbindArtist, lookupArtists } from "@/music-universe/music-data/api/music-data-artists.ts";
import { updateApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
// constants
import { ApprovalStatus } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// types
import type { LookupEntity } from "@/music-universe/shared/types/lookup";
// styles
import sharedTableStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from "./LastfmArtistsTableRow.module.scss";

interface LastfmArtistTableRowProps extends RawEntityTableRow<LastfmArtist> {
    preloadedLookupData?: LookupEntity[]
}

export const LastfmArtistsTableRow = memo((
    {
        entity,
        preloadedLookupData = []
    }: LastfmArtistTableRowProps) =>
{
    const [isApproving, setIsApproving] = useState(false);
    const [isTagPanelOpen, setIsTagPanelOpen] = useState(false);
    const [, forceUpdate] = useState({});

    function onStatusChange(artistToUpdate: LastfmArtist, newStatus: number) {
        setIsApproving(true);
        updateApprovalStatus(artistToUpdate, newStatus)
            .then(() => {
                // Force re-render after approval status change
                forceUpdate({});
            })
            .finally(() => {
                setIsApproving(false);
            });
    }

    async function handleBeforeBind(artistToApprove: LastfmArtist): Promise<boolean> {
        if (artistToApprove.approvalStatus === ApprovalStatus.APPROVED) {
            return true;
        }

        // Approve the artist if not already approved
        try {
            console.log("Artist not approved, approving first...");
            await updateApprovalStatus(artistToApprove, ApprovalStatus.APPROVED);
            
            // Force re-render after approval status change
            forceUpdate({});
            
            console.log("Artist approved successfully");
            return true;
        } catch (error) {
            console.error("Failed to approve artist:", error);
            return false;
        }
    }

    async function handleBindToExisting(artistId: number, targetArtistId: number) {
        console.log("Binding to existing artist...");
        return await bindArtistToExisting(artistId, targetArtistId);
    }

    async function handleCreateAndBind(artistId: number, name: string) {
        console.log("Creating and binding new artist...");
        return await createAndBindArtist(artistId, name);
    }

    // Handle the result of binding from the EntityBinding component
    const handleAfterBind = (updatedEntity: LastfmArtist) => {
        // Entity is already updated in place via the class instance
        // No need to notify parent component
    };

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

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.status}`} onClick={(e) => e.stopPropagation()}>
                    <ApprovalToggle
                        status={entity.approvalStatus}
                        onChange={(newStatus) => onStatusChange(entity, newStatus)}
                        disabled={isApproving}
                    />
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.binding}`} onClick={(e) => e.stopPropagation()}>
                    <EntityBinding
                        key={`exp-entity-binding-${entity.id}`}
                        entity={entity}
                        onBindToExisting={handleBindToExisting}
                        onCreateAndBind={handleCreateAndBind}
                        onUnbind={unbindArtist}
                        onBeforeBind={handleBeforeBind}
                        onAfterBind={handleAfterBind}
                        lookupFunction={lookupArtists}
                        preloadedOptions={preloadedLookupData}
                        disabled={isApproving}
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
