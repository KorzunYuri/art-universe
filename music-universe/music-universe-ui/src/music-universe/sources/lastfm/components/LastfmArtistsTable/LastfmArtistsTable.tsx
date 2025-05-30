import { useEffect, useState } from 'react'

import sharedStyles from "../../../../shared/components/common/LastfmEntityTable.module.scss"

import { type LastfmArtist, fetchArtists} from "../../api/lastfm-artists.ts";
import {LastfmArtistTableRow} from "../LastfmArtistTableRow";
import {LastfmArtistTableHeader} from "../LastfmArtistTableHeader";

export function LastfmArtistsTable() {

    const [artists, setArtists] = useState<LastfmArtist[]>([])

    useEffect(() => {
        fetchArtists()
            .then(setArtists)
    }, [])

    function onArtistChange(updated: LastfmArtist) {
        setArtists(prev =>
            prev.map(a => a.id === updated.id ? updated : a)
        )
    }

    return (
        <div className={sharedStyles.container}>
            <LastfmArtistTableHeader />

            {artists.map((artist) => (
                <LastfmArtistTableRow artist={artist} onChange={onArtistChange} />
            ))}
        </div>
    )
}