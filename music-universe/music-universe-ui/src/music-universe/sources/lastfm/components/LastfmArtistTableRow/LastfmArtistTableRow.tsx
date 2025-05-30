import type {LastfmArtist} from "../../api/lastfm-artists.ts";
import sharedStyles from "../../../../shared/components/common/LastfmEntityTable.module.scss";
import artistStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";
import {ExternalLink, LabelWithPopup, ReadonlyAttr} from "../../../../shared/components";
import {LastfmConfig} from "../../config/lastfmconfig.ts";
import {ApprovalToggle} from "../ApprovalToggle";

interface LastfmArtistTableRowProps {
    artist: LastfmArtist,
    onChange: (artist: LastfmArtist) => void
}

export const LastfmArtistTableRow = ({artist, onChange}: LastfmArtistTableRowProps) => {

    function onStatusChange(updatedArtist: LastfmArtist, newStatus: number) {
        updatedArtist.approval_status = newStatus;
        onChange(updatedArtist);
    }

    return (
        <div key={artist.id} className={sharedStyles.row}>

            {/* TODO mix LabelWithPopup with ExternalLink to display entity name with its lastfm url */}
            <div className={`${sharedStyles.cell} ${artistStyles.name}`}>
                <LabelWithPopup text={artist.name}/>
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.url}`}>
                {artist.url && <ExternalLink href={artist.url} label="Last.fm"/>}
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.url}`}>
                {artist.mbid && <ExternalLink
                        href={`${LastfmConfig.mbBaseUrls.artist}${artist.mbid}`}
                        label="MusicBrainz"/>}
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.status}`}>
                <ApprovalToggle
                    status={artist.approval_status}
                    onChange={(newStatus) => onStatusChange(artist, newStatus)}
                />
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.count}`}>
                <ReadonlyAttr value={artist.play_count}/>
            </div>

            <div className={`${sharedStyles.cell}  ${artistStyles.count}`}>
                <ReadonlyAttr value={artist.listeners_count}/>
            </div>
        </div>
    );
};
