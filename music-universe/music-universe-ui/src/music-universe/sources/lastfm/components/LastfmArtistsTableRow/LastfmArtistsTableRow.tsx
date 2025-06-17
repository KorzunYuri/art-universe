// hooks
import { useState } from "react";
// components
import { ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
// backend services
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import type { LastfmArtist } from "@/music-universe/sources/lastfm/types";
import { bindArtist, unbindArtist } from "@/music-universe/sources/music-data/api/music-data-artists.ts";
import { updateArtistApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
// styles
import commonStyles from "@/music-universe/shared/styles/common.module.scss";
import sharedTableStyles from "@/music-universe/sources/lastfm/common/LastfmEntityTable.module.scss";
import artistTableStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import artistRowStyles from "./LastfmArtistsTableRow.module.scss"

interface LastfmArtistTableRowProps {
    artist: LastfmArtist,
    onChange: (artist: LastfmArtist) => void
}

export const LastfmArtistsTableRow = ({artist, onChange}: LastfmArtistTableRowProps) => {
    const [artistName, setArtistName] = useState(artist.name);
    const [isBinding, setIsBinding] = useState(false);
    const [isUnbinding, setIsUnbinding] = useState(false);
    const [isEditing, setIsEditing] = useState(false);

    function onStatusChange(artistToUpdate: LastfmArtist, newStatus: number) {
        updateArtistApprovalStatus(artistToUpdate.id, newStatus)
            .then(onChange);
    }

    async function handleBind() {
        if (!artistName.trim()) return;
        
        setIsBinding(true);
        try {
            const result = await bindArtist(artist.id, artistName);
            if (result) {
                onChange({
                    ...artist,
                    boundArtist: {
                        referenceId: result.referenceId,
                        referenceName: result.referenceName
                    }
                });
                setIsEditing(false);
            }
        } catch (error) {
            console.error("Failed to bind artist:", error);
        } finally {
            setIsBinding(false);
        }
    }

    async function handleUnbind() {
        setIsUnbinding(true);
        try {
            const success = await unbindArtist(artist.id);
            if (success) {
                // Keep the name in the input field for easy re-binding
                setArtistName(artist.boundArtist?.referenceName || artist.name);
                onChange({
                    ...artist,
                    boundArtist: undefined
                });
                setIsEditing(true);
            }
        } catch (error) {
            console.error("Failed to unbind artist:", error);
        } finally {
            setIsUnbinding(false);
        }
    }

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
                />
            </div>

            <div className={`${sharedTableStyles.cell}  ${artistTableStyles.binding}`}>
                {artist.boundArtist && !isEditing ? (
                    <div className={artistRowStyles.wrapper}>
                        <span className={`${commonStyles.muLabel} ${artistRowStyles.bindingName} ${sharedTableStyles.approvalYes}`}>
                            {artist.boundArtist.referenceName}
                        </span>
                        <button
                            onClick={handleUnbind}
                            disabled={isUnbinding}
                            className={`${artistRowStyles.bindingButton}`}
                        >
                            {isUnbinding ? "..." : "Unbind"}
                        </button>
                    </div>
                ) : (
                    <div className={artistRowStyles.wrapper}>
                        <input
                            type="text"
                            value={artistName}
                            onChange={(e) => setArtistName(e.target.value)}
                            className={`${commonStyles.muLabel} ${artistRowStyles.bindingName}`}
                            placeholder="Artist name"
                        />
                        <button
                            onClick={handleBind}
                            disabled={isBinding || !artistName.trim()}
                            className={`${artistRowStyles.bindingButton}`}
                        >
                            {isBinding ? "..." : "Bind"}
                        </button>
                    </div>
                )}
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
