// hooks
import { useState } from "react";
// components
import { EntityBinding, ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmArtist } from "@/music-universe/sources/lastfm/types";
import { bindArtist, unbindArtist } from "@/music-universe/sources/music-data/api/music-data-artists.ts";
import { updateArtistApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
// constants
import { ApprovalStatus } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// styles
import sharedTableStyles from "@/music-universe/sources/lastfm/common/LastfmEntityTable.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";

interface LastfmArtistTableRowProps {
    artist: LastfmArtist,
    onChange: (artist: LastfmArtist) => void
}

export const LastfmArtistsTableRow = ({artist, onChange}: LastfmArtistTableRowProps) => {
    const [isApproving, setIsApproving] = useState(false);

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

    async function handleBindArtist(artistId: number, name: string) {
        // First, approve the artist if not already approved
        let currentArtist = {...artist};
        let wasApproved = false;
        
        if (artist.approvalStatus !== ApprovalStatus.APPROVED) {
            try {
                console.log("Artist not approved, approving first...");
                // Explicitly approve the artist on LastFM
                const approvedArtist = await updateArtistApprovalStatus(artist.id, ApprovalStatus.APPROVED);
                
                // Update our local state with the approved artist
                currentArtist = {
                    ...approvedArtist,
                    boundEntity: artist.boundEntity // Preserve existing binding if any
                };
                
                // Update the parent component with the approved status
                onChange(currentArtist);
                wasApproved = true;
                console.log("Artist approved successfully");
            } catch (error) {
                console.error("Failed to approve artist:", error);
                // Continue with binding even if approval fails
            }
        }
        
        // Then bind the artist
        console.log("Binding artist...");
        const result = await bindArtist(artistId, name);
        
        // If binding was successful and we didn't just approve the artist,
        // make sure the UI reflects the approved status
        if (result && !wasApproved && artist.approvalStatus !== ApprovalStatus.APPROVED) {
            console.log("Binding successful, updating UI to show approved status");
            // We need to update the UI to show the artist as approved
            // This is needed because the binding process requires approval,
            // but we might not have updated the UI if the artist was approved
            // as part of the binding process on the backend
            setTimeout(() => {
                onChange({
                    ...artist,
                    approvalStatus: ApprovalStatus.APPROVED,
                    boundEntity: result
                });
            }, 0);
        }
        
        return result;
    }

    // Handle the result of binding from the EntityBinding component
    const handleEntityChange = (updatedEntity: LastfmArtist) => {
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

    return (
        <div key={artist.id} className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell}  ${artistTableStyles.name}`}>
                {artist.url && <ExternalLink href={artist.url} label={artist.name}/>}
            </div>

            <div className={`${sharedTableStyles.cell}  ${artistTableStyles.mbid}`}>
                {artist.mbid && <ExternalLink
                        href={`${LastfmConfig.mbBaseUrls.artist}${artist.mbid}`}
                        label="MusicBrainz"/>}
            </div>

            <div className={`${sharedTableStyles.cell}  ${artistTableStyles.status}`}>
                <ApprovalToggle
                    status={artist.approvalStatus}
                    onChange={(newStatus) => onStatusChange(artist, newStatus)}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell}  ${artistTableStyles.binding}`}>
                <EntityBinding
                    entity={artist}
                    onBind={handleBindArtist}
                    onUnbind={unbindArtist}
                    onChange={handleEntityChange}
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
    );
};
