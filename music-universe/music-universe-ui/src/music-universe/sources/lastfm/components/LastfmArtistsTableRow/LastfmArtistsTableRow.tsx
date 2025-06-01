import sharedStyles from "@/music-universe/sources/lastfm/common/LastfmEntityTable.module.scss";
import artistStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import { ExternalLink, ReadonlyAttr } from "@/music-universe/shared/components";
import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts";
import { ApprovalToggle } from "@/music-universe/sources/lastfm/components";
import type { LastfmArtist } from "@/music-universe/sources/lastfm/types";
import { updateArtistApprovalStatus } from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";

interface LastfmArtistTableRowProps {
    artist: LastfmArtist,
    onChange: (artist: LastfmArtist) => void
}

export const LastfmArtistsTableRow = ({artist, onChange}: LastfmArtistTableRowProps) => {

    function onStatusChange(artistToUpdate: LastfmArtist, newStatus: number) {
        updateArtistApprovalStatus(artistToUpdate.id, newStatus)
            .then(onChange);
    }

    return (
        <div key={artist.id} className={sharedStyles.row}>

            <div className={`${sharedStyles.cell}  ${artistStyles.name}`}>
                {artist.url && <ExternalLink href={artist.url} label={artist.name}/>}
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.mbid}`}>
                {artist.mbid && <ExternalLink
                        href={`${LastfmConfig.mbBaseUrls.artist}${artist.mbid}`}
                        label="MusicBrainz"/>}
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.status}`}>
                <ApprovalToggle
                    status={artist.approvalStatus}
                    onChange={(newStatus) => onStatusChange(artist, newStatus)}
                />
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.count}`}>
                <ReadonlyAttr value={artist.playCount}/>
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.count}`}>
                <ReadonlyAttr value={artist.listenersCount}/>
            </div>
        </div>
    );
};
