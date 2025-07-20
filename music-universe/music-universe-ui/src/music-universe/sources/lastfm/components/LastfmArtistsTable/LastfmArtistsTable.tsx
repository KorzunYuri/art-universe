// hooks
import { useState, useEffect } from 'react'
// components
import {
    LastfmArtistsTableHeader,
    LastfmArtistsTableRow,
} from '@/music-universe/sources/lastfm/components'
// types
import type { LastfmArtist } from '@/music-universe/sources/lastfm/types'
import type { LookupEntity } from '@/music-universe/shared/types/lookup'
import type { BoundEntityResponse } from '@/music-universe/shared/types/master'
import type { Artist } from "@/music-universe/music-data/types/master-entities";
// api
import { fetchArtists, type ArtistSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-artists'
import { fetchBoundArtists, batchLookupArtists } from '@/music-universe/music-data/api/music-data-artists'
// components
import { LastfmEntityTable } from '@/music-universe/sources/lastfm/components/LastfmEntityTable'
// utils
import { ArtistImpl } from '@/music-universe/music-data/types/master-entities'
// styles
import styles from './LastfmArtistsTable.module.css'

export const LastfmArtistsTable = () => {
    // Preloaded lookup data for artists
    const [preloadedLookupData, setPreloadedLookupData] = useState<{[name: string]: LookupEntity[]}>({})

    // Create Artist from BoundEntityResponse
    const createArtist = (boundEntity: BoundEntityResponse): Artist => {
        return new ArtistImpl(boundEntity.masterId, boundEntity.masterName);
    };

    // Load artists with search parameters
    const loadArtists = async (params: ArtistSearchParams) => {
        try {
            const result = await fetchArtists(params);
            
            // Perform batch lookup for artist names
            if (result && result.content.length > 0) {
                try {
                    // Collect artist names for batch lookup
                    const artistNames = result.content.map(artist => artist.name)
                    
                    // Perform batch lookup for all artists
                    const lookupResponse = await batchLookupArtists(artistNames)
                    
                    if (lookupResponse.success) {
                        setPreloadedLookupData(lookupResponse.data.results)
                    } else {
                        setPreloadedLookupData({})
                    }
                } catch (error) {
                    console.error('Error performing batch lookup:', error)
                    setPreloadedLookupData({})
                }
            }
            
            return result;
        } catch (error) {
            console.error('Error loading artists:', error)
            throw error;
        }
    }
    
    // Reset preloaded data when component unmounts
    useEffect(() => {
        return () => {
            setPreloadedLookupData({});
        };
    }, []);

    return (
        <div className={styles.container}>
            <LastfmEntityTable<LastfmArtist, Artist>
                fetchEntities={loadArtists}
                fetchMasterEntities={fetchBoundArtists}
                createMasterEntity={createArtist}
                renderHeader={(sort, setSort) => (
                    <LastfmArtistsTableHeader sort={sort} setSort={setSort} />
                )}
                renderRow={(artist) => (
                    <LastfmArtistsTableRow
                        key={artist.id}
                        entity={artist}
                        preloadedLookupData={preloadedLookupData[artist.name] || []}
                    />
                )}
                searchPlaceholder="Search artist name..."
            />
        </div>
    )
}
