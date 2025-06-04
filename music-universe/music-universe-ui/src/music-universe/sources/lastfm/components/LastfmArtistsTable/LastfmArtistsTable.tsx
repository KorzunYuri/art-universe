import {
    LastfmArtistsTableHeader,
    LastfmArtistsTableRow,
} from '@/music-universe/sources/lastfm/components'
import { PaginatedResource } from '@/music-universe/shared/hooks/PaginatedResource.ts'
import { fetchArtists } from '@/music-universe/sources/lastfm/api/lastfm-artists'
import type { LastfmArtist } from '@/music-universe/sources/lastfm/types'

export const LastfmArtistsTable = () => {
    const {
        data,
        setData,
        loading,
        searchInput,
        setSearchInput,
        applySearch,
        sort,
        setSort,
        nextPage,
        prevPage,
        hasNextPage,
        hasPrevPage,
        reload,
    } = PaginatedResource<LastfmArtist>(fetchArtists)

    const onSearchKeyDown = (key: string) => {
        if (key === 'Enter') {
            applySearch()
        }
    }

    const onArtistChanged = (updated: LastfmArtist) => {
        if (!data) return
        const newContent = data.content.map(a => a.id === updated.id ? updated : a)
        setData({ ...data, content: newContent })
    }

    return (
        <div className="space-y-4">
            <div className="flex gap-2 items-center">
                <input
                    type="text"
                    value={searchInput}
                    placeholder="Search artist name..."
                    onChange={(e) => setSearchInput(e.target.value)}
                    onKeyDown={(e) => onSearchKeyDown(e.key)}
                />
                <button onClick={applySearch}>Search</button>
            </div>

            {loading && <p className="text-gray-500">Loading...</p>}

            {!loading && data && (
                <>
                    <div className="border rounded-md">
                        <LastfmArtistsTableHeader
                            sort={sort}
                            setSort={setSort}
                        />
                        {data.content.map((artist) => (
                            <LastfmArtistsTableRow
                                key={artist.id}
                                artist={artist}
                                onChange={onArtistChanged}
                            />
                        ))}
                    </div>

                    <div className="flex gap-2 items-center mt-4">
                        <button disabled={!hasPrevPage} onClick={prevPage}>
                            Previous
                        </button>
                        <span>
                            Page {data.pageable.pageNumber + 1} of {data.totalPages}
                        </span>
                        <button disabled={!hasNextPage} onClick={nextPage}>
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}
