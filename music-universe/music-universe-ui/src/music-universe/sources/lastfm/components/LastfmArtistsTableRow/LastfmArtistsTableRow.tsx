// hooks
import { useState } from "react";
// components
import { EntityBinding, ExternalLink, ReadonlyAttr, EntityTagPanel } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmArtist } from "@/music-universe/sources/lastfm/types";
import { bindArtistToExisting, createAndBindArtist, unbindArtist, lookupArtists } from "@/music-universe/music-data/api/music-data-artists.ts";
import { updateArtistApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
// constants
import { ApprovalStatus } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// types
import type { LookupEntity } from "@/music-universe/shared/types/lookup";
// styles
import sharedTableStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from "./LastfmArtistsTableRow.module.scss";

interface LastfmArtistTableRowProps {
    artist: LastfmArtist,
    onChange: (artist: LastfmArtist) => void,
    preloadedLookupData?: LookupEntity[]
}

export const LastfmArtistsTableRow = ({artist, onChange, preloadedLookupData = []}: LastfmArtistTableRowProps) => {
    const [isApproving, setIsApproving] = useState(false);
    const [isTagPanelOpen, setIsTagPanelOpen] = useState(false);

    function onStatusChange(artistToUpdate: LastfmArtist, newStatus: number) {
        setIsApproving(true);
        updateArtistApprovalStatus(artistToUpdate.id, newStatus)
            .then(updatedArtist => {
                // Preserve the boundEntity information when updating approval status
                onChange({
                    ...updatedArtist,
                    boundEntity: artist.boundEntity
                });
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
            const approvedArtist = await updateArtistApprovalStatus(artistToApprove.id, ApprovalStatus.APPROVED);

            // Update the UI with approved artist
            onChange({
                ...approvedArtist,
                boundEntity: artist.boundEntity
            });

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
        // If the entity was just bound (boundEntity changed from undefined to defined)
        if (updatedEntity.boundEntity && !artist.boundEntity) {
            console.log("Entity was bound, ensuring approval status is updated");
            // Ensure the approval status is set to APPROVED
            onChange({
                ...updatedEntity,
                approvalStatus: ApprovalStatus.APPROVED
            });
        } else {
            // For other changes, just pass through
            onChange(updatedEntity);
        }
    };

    const toggleTagPanel = () => {
        setIsTagPanelOpen(!isTagPanelOpen);
    };

    return (
        <>
            <div 
                key={artist.id} 
                className={`${sharedTableStyles.row} ${isTagPanelOpen ? styles.activeRow : ''}`}
                onClick={toggleTagPanel}
            >
                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.name}`}>
                    {artist.url && <ExternalLink href={artist.url} label={artist.name}/>}
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.mbid}`}>
                    {artist.mbid && <ExternalLink
                            href={`${LastfmConfig.mbBaseUrls.artist}${artist.mbid}`}
                            label="MusicBrainz"/>}
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.status}`} onClick={(e) => e.stopPropagation()}>
                    <ApprovalToggle
                        status={artist.approvalStatus}
                        onChange={(newStatus) => onStatusChange(artist, newStatus)}
                        disabled={isApproving}
                    />
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.binding}`} onClick={(e) => e.stopPropagation()}>
                    <EntityBinding
                        entity={artist}
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
                    <ReadonlyAttr value={artist.playCount}/>
                </div>

                <div className={`${sharedTableStyles.cell}  ${artistTableStyles.count}`}>
                    <ReadonlyAttr value={artist.listenersCount}/>
                </div>
            </div>
            
            {isTagPanelOpen && (
                <div className={styles.tagPanelContainer}>
                    <EntityTagPanel
                        entityType="ARTIST"
                        entityId={artist.id}
                        entityApprovalStatus={artist.approvalStatus}
                        tagPageBaseUrl="/lastfm/tags/"
                        onClose={() => setIsTagPanelOpen(false)}
                    />
                </div>
            )}
        </>
    );
};
