// hooks
import { useState } from "react";
// components
import { EntityBinding, ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmTrack } from "@/music-universe/sources/lastfm/types/lastfm-track";
import { bindTrack, unbindTrack } from "@/music-universe/sources/music-data/api/music-data-tracks.ts";
import { bindArtist } from "@/music-universe/sources/music-data/api/music-data-artists.ts";
import { updateTrackApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-tracks.ts";
import { updateArtistApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
// constants
import { ApprovalStatus } from "@/music-universe/sources/lastfm/constants/approvalStatus";
// styles
import sharedTableStyles from "@/music-universe/sources/lastfm/common/LastfmEntityTable.module.scss";
import trackTableStyles from "../LastfmTracksTable/LastfmTracksTable.module.css";

interface LastfmTrackTableRowProps {
    track: LastfmTrack,
    onChange: (track: LastfmTrack) => void
}

export const LastfmTracksTableRow = ({track, onChange}: LastfmTrackTableRowProps) => {
    const [isApproving, setIsApproving] = useState(false);

    function onStatusChange(trackToUpdate: LastfmTrack, newStatus: number) {
        setIsApproving(true);
        updateTrackApprovalStatus(trackToUpdate.id, newStatus)
            .then(updatedTrack => {
                // Preserve the boundEntity information when updating approval status
                onChange({
                    ...updatedTrack,
                    boundEntity: track.boundEntity
                });
            })
            .finally(() => {
                setIsApproving(false);
            });
    }

    async function handleBindTrack(trackId: number, trackName: string) {
        if (!track.artist) {
            console.error("Cannot bind track without artist information");
            return null;
        }

        console.log(`🎵 Starting track binding process for track ${trackId} (${trackName}) by artist ${track.artist.name} (${track.artist.id})`);
        
        // First, ensure the artist is approved and bound
        let artistWasApproved = false;
        
        // Step 1: Approve the artist if not already approved
        if (track.artist.approvalStatus !== ApprovalStatus.APPROVED) {
            try {
                console.log("🎤 Artist not approved, approving first...");
                await updateArtistApprovalStatus(track.artist.id, ApprovalStatus.APPROVED);
                artistWasApproved = true;
                console.log("✅ Artist approved successfully");
            } catch (error) {
                console.error("❌ Failed to approve artist:", error);
                // Continue with binding even if approval fails
            }
        }
        
        // Step 2: Bind the artist if not already bound
        // Note: We don't have artist binding information in the track object,
        // so we'll try to bind the artist anyway. The backend should handle duplicates.
        try {
            console.log("🔗 Ensuring artist is bound...");
            await bindArtist(track.artist.id, track.artist.name);
            console.log("✅ Artist binding ensured");
        } catch (error) {
            console.error("⚠️ Artist binding failed, but continuing with track binding:", error);
            // Continue with track binding even if artist binding fails
        }
        
        // Step 3: Approve the track if not already approved
        let currentTrack = {...track};
        let trackWasApproved = false;
        
        if (track.approvalStatus !== ApprovalStatus.APPROVED) {
            try {
                console.log("🎵 Track not approved, approving first...");
                const approvedTrack = await updateTrackApprovalStatus(track.id, ApprovalStatus.APPROVED);
                
                currentTrack = {
                    ...approvedTrack,
                    boundEntity: track.boundEntity,
                    artist: track.artist
                };
                
                onChange(currentTrack);
                trackWasApproved = true;
                console.log("✅ Track approved successfully");
            } catch (error) {
                console.error("❌ Failed to approve track:", error);
                // Continue with binding even if approval fails
            }
        }
        
        // Step 4: Bind the track
        console.log("🔗 Binding track...");
        const result = await bindTrack(trackId, trackName, track.artist.id);
        
        // Step 5: Update UI to reflect all changes
        if (result && !trackWasApproved && track.approvalStatus !== ApprovalStatus.APPROVED) {
            console.log("✅ Binding successful, updating UI to show approved status");
            setTimeout(() => {
                onChange({
                    ...track,
                    approvalStatus: ApprovalStatus.APPROVED,
                    boundEntity: result,
                    artist: artistWasApproved ? {
                        ...track.artist!,
                        approvalStatus: ApprovalStatus.APPROVED
                    } : track.artist
                });
            }, 0);
        }
        
        return result;
    }

    // Handle the result of binding from the EntityBinding component
    const handleEntityChange = (updatedEntity: LastfmTrack) => {
        // If the entity was just bound (boundEntity changed from undefined to defined)
        if (updatedEntity.boundEntity && !track.boundEntity) {
            console.log("🎵 Track was bound, ensuring approval status is updated");
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
        <div key={track.id} className={sharedTableStyles.row}>
            <div className={`${sharedTableStyles.cell} ${trackTableStyles.artist}`}>
                {track.artist && <ReadonlyAttr value={track.artist.name} />}
            </div>
            
            <div className={`${sharedTableStyles.cell} ${trackTableStyles.name}`}>
                {track.url && <ExternalLink href={track.url} label={track.name}/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.mbid}`}>
                {track.mbid && <ExternalLink
                        href={`${LastfmConfig.mbBaseUrls.track}${track.mbid}`}
                        label="MusicBrainz"/>}
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.status}`}>
                <ApprovalToggle
                    status={track.approvalStatus}
                    onChange={(newStatus) => onStatusChange(track, newStatus)}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.binding}`}>
                <EntityBinding
                    entity={track}
                    onBind={handleBindTrack}
                    onUnbind={unbindTrack}
                    onChange={handleEntityChange}
                    disabled={isApproving}
                />
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.count}`}>
                <ReadonlyAttr value={track.playCount}/>
            </div>

            <div className={`${sharedTableStyles.cell} ${trackTableStyles.count}`}>
                <ReadonlyAttr value={track.listenersCount}/>
            </div>
        </div>
    );
};
