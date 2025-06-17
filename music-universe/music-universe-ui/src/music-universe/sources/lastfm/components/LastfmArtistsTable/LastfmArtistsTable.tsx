// hooks
import { useEffect, useState, useRef } from 'react'
// components
import {
    LastfmArtistsTableHeader,
    LastfmArtistsTableRow,
} from '@/music-universe/sources/lastfm/components'
// backend services
import type { LastfmArtist } from '@/music-universe/sources/lastfm/types'
import { PaginatedResource } from '@/music-universe/shared/hooks/PaginatedResource.ts'
import { fetchArtists } from '@/music-universe/sources/lastfm/api/lastfm-artists'
import { fetchBoundArtists } from '@/music-universe/sources/music-data/api/music-data-artists'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'

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

    const [loadingBoundArtists, setLoadingBoundArtists] = useState(false)
    const skipNextFetchRef = useRef(false);
    const previousContentRef = useRef<LastfmArtist[]>([]);

    const loadBoundArtists = async () => {
        if (!data || data.content.length === 0) return;
        
        setLoadingBoundArtists(true);
        try {
            // Extract all artist IDs
            const artistIds = data.content.map(artist => artist.id);
            
            // Fetch bound artists from music-data API
            const boundArtists = await fetchBoundArtists(artistIds);
            
            // Update the artists with bound information
            if (boundArtists.length > 0) {
                const updatedContent = data.content.map(artist => {
                    const boundArtist = boundArtists.find(ba => ba.externalId === artist.id);
                    if (boundArtist) {
                        return {
                            ...artist,
                            boundArtist: {
                                referenceId: boundArtist.referenceId,
                                referenceName: boundArtist.referenceName
                            }
                        };
                    }
                    return artist;
                });
                
                setData({ ...data, content: updatedContent });
            }
        } catch (error) {
            console.error('Error loading bound artists:', error);
        } finally {
            setLoadingBoundArtists(false);
        }
    };

    // Load bound artists whenever the artist data changes
    useEffect(() => {
        if (!data || data.content.length === 0) return;
        
        // Skip fetch if we just updated an artist through the UI
        if (skipNextFetchRef.current) {
            skipNextFetchRef.current = false;
            return;
        }
        
        // Skip fetch if only boundArtist property changed
        if (previousContentRef.current.length === data.content.length) {
            const onlyBoundArtistChanged = data.content.every((artist, index) => {
                const prevArtist = previousContentRef.current[index];
                // If IDs don't match, content has changed
                if (prevArtist.id !== artist.id) return false;
                
                // If any property other than boundArtist changed, content has changed
                const artistWithoutBound = { ...artist };
                const prevArtistWithoutBound = { ...prevArtist };
                delete artistWithoutBound.boundArtist;
                delete prevArtistWithoutBound.boundArtist;
                
                return JSON.stringify(artistWithoutBound) === JSON.stringify(prevArtistWithoutBound);
            });
            
            if (onlyBoundArtistChanged) {
                previousContentRef.current = [...data.content];
                return;
            }
        }
        
        previousContentRef.current = [...data.content];
        loadBoundArtists();
    }, [data?.content, setData]);

    const onSearchKeyDown = (key: string) => {
        if (key === 'Enter') {
            applySearch();
        }
    };

    const onArtistChanged = (updated: LastfmArtist) => {
        if (!data) return;
        
        // Set flag to skip next fetch when we update an artist through the UI
        skipNextFetchRef.current = true;
        
        const newContent = data.content.map(a => a.id === updated.id ? updated : a);
        setData({ ...data, content: newContent });
    };

    const handleRefresh = () => {
        reload();
        // reset flag to trigger bindings reload
        skipNextFetchRef.current = false;
        previousContentRef.current = [];
    };

    return (
        <div>
            <div>
                <div>
                    <input
                        type="text"
                        value={searchInput}
                        placeholder="Search artist name..."
                        onChange={(e) => setSearchInput(e.target.value)}
                        onKeyDown={(e) => onSearchKeyDown(e.key)}
                        className={commonStyles.muLabel}
                    />
                    <button onClick={applySearch}>Search</button>
                    <button onClick={handleRefresh} disabled={loading || loadingBoundArtists}>
                        Refresh
                    </button>
                </div>
                
                {(loading || loadingBoundArtists) && (
                    <div>Loading...</div>
                )}
            </div>

            {!loading && data && (
                <>
                    <div>
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

                    <div>
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
