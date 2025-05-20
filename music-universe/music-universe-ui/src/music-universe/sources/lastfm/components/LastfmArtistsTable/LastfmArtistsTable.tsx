import { useEffect, useState } from 'react'

import {
    LabelWithPopup,
    ExternalLink,
    ReadonlyAttr,
} from '../../../../shared/components'
import { ApprovalToggle } from "../ApprovalToggle";

import sharedStyles from "../../../../shared/components/common/LastfmEntityTable.module.scss"
import artistStyles from "./LastfmArtistsTable.module.css"

import { LastfmConfig } from '../../config/lastfmconfig.ts'
import { type LastfmArtist, fetchArtists} from "../../api/lastfm-artists.ts";

export function LastfmArtistsTable() {

    const [artists, setArtists] = useState<LastfmArtist[]>([])

    useEffect(() => {
        fetchArtists()
            .then(setArtists)
    }, [])

    function onStatusChange(artist: LastfmArtist, newStatus: number) {
        setArtists(prev =>
            prev.map(a =>
                a.id === artist.id
                    ? { ...a, approval_status: newStatus }
                    : a
            )
        )
    }

    return (
        <div className={sharedStyles.container}>
            <div className={sharedStyles.header}>
                <div className={`${sharedStyles.cell} ${artistStyles.name}`}>Name</div>
                <div className={`${sharedStyles.cell} ${artistStyles.url}`}>Last.fm</div>
                <div className={`${sharedStyles.cell} ${artistStyles.mbid}`}>MusicBrainz</div>
                <div className={`${sharedStyles.cell} ${artistStyles.status}`}>Approval</div>
                <div className={`${sharedStyles.cell} ${artistStyles.count}`}>Plays</div>
                <div className={`${sharedStyles.cell} ${artistStyles.count}`}>Listeners</div>
            </div>

            {artists.map((artist) => (
                <div key={artist.id} className={sharedStyles.row}>

                    {/* TODO mix LabelWithPopup with ExternalLink to display entity name with its lastfm url */}
                    <div className={`${sharedStyles.cell} ${artistStyles.name}`}>
                        <LabelWithPopup text={artist.name} />
                    </div>

                    <div className={`${sharedStyles.cell}  ${artistStyles.url}`}>
                        {artist.url && <ExternalLink href={artist.url} label="Last.fm" />}
                    </div>

                    <div className={`${sharedStyles.cell}  ${artistStyles.url}`}>
                        {artist.mbid ? (
                            <ExternalLink
                                href={`${LastfmConfig.mbBaseUrls.artist}${artist.mbid}`}
                                label="MusicBrainz"
                            />
                        ) : (
                            <span>-</span>
                        )}
                    </div>

                    <div className={`${sharedStyles.cell}  ${artistStyles.status}`}>
                        <ApprovalToggle
                            status={artist.approval_status}
                            onChange={(newStatus) => onStatusChange(artist, newStatus)}
                        />
                    </div>

                    <div className={`${sharedStyles.cell}  ${artistStyles.count}`}>
                        <ReadonlyAttr value={artist.play_count} />
                    </div>

                    <div className={`${sharedStyles.cell}  ${artistStyles.count}`}>
                        <ReadonlyAttr value={artist.listeners_count} />
                    </div>
                </div>
            ))}
        </div>
    )
}