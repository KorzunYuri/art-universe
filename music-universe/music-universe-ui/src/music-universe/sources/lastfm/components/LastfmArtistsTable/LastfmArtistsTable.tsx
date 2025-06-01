import { useState} from 'react'

import type {LastfmArtist} from '@/music-universe/sources/lastfm/types'
import { PaginatedResource } from '@/music-universe/shared/hooks/paginatedResource'
import { fetchArtists } from '@/music-universe/sources/lastfm/api/lastfm-artists.ts'
import { LastfmArtistsTableHeader } from '@/music-universe/sources/lastfm/components'
import { LastfmArtistsTableRow } from '../LastfmArtistsTableRow'

export const LastfmArtistsTable = () => {
    const {
        meta,
        loading,
        search,
        setNewSearch,
        page,
        setPage,
        reload
    } = PaginatedResource<LastfmArtist>(fetchArtists)

    const [searchInput, setSearchInput] = useState(search)

    const onSearchChanged = (newSearch: string) => {
        setSearchInput(newSearch)
    }

    const onSearchKeyDown = (key: string) => {
        if (key === 'Enter') {
            setNewSearch(search)
        }
    };

    return (
        <div className="space-y-4">
            <div className="flex gap-2 items-center">
                <input
                    type="text"
                    value={searchInput}
                    placeholder="Search artist name..."
                    onChange={(e) => onSearchChanged(e.target.value)}
                    onKeyDown={(e) => onSearchKeyDown(e.key)}
                />
                <button
                    onClick={() => setNewSearch(searchInput)}
                >
                    Search
                </button>
            </div>

            {loading && <p className="text-gray-500">Loading...</p>}

            {!loading && meta && (
                <>
                    <div className="border rounded-md">
                        <LastfmArtistsTableHeader />
                        {meta.content.map((artist) => (
                            <LastfmArtistsTableRow key={artist.id} artist={artist} onChange={() => reload()} />
                        ))}
                    </div>

                    <div className="flex gap-2 items-center mt-4">
                        <button disabled={page <= 0} onClick={() => setPage(page - 1)}>Previous</button>
                        <span>Page {page + 1} of {meta.totalPages}</span>
                        <button disabled={page + 1 >= meta.totalPages} onClick={() => setPage(page + 1)}>Next</button>
                    </div>
                </>
            )}
        </div>
    )
}