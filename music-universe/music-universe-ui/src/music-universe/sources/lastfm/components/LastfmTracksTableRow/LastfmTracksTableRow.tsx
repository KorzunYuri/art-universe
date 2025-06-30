// hooks
import { useState } from "react";
// components
import { ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmTrack } from "@/music-universe/sources/lastfm/types/lastfm-track";
import { updateTrackApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-tracks.ts";
// styles
import sharedTableStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
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
                {/* EntityBinding temporarily removed */}
                <span>-</span>
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
