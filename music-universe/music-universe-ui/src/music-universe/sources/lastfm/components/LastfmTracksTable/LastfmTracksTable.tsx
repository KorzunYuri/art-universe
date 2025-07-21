// types
import type { LastfmTrack } from '@/music-universe/sources/lastfm/types/lastfm-track'
import type {Track} from "@/music-universe/music-data/types/master-entities.ts";
import type { BoundEntityResponse } from '@/music-universe/music-data/utils/master-entities-common.ts'
// api
import { fetchTracks, type TrackSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-tracks'
import { fetchBoundTracks } from '@/music-universe/music-data/api/music-data-tracks'
// components
import { LastfmEntityTable } from '@/music-universe/sources/lastfm/components/LastfmEntityTable'
import {
    LastfmTracksTableHeader,
    LastfmTracksTableRow,
} from '@/music-universe/sources/lastfm/components'
// utils
import { TrackImpl } from '@/music-universe/music-data/types/master-entities'
// styles
import styles from './LastfmTracksTable.module.css'

interface LastfmTracksTableProps {
    artistId?: number;
}

export const LastfmTracksTable = ({ artistId }: LastfmTracksTableProps) => {
    // Create Track from BoundEntityResponse
    const createTrack = (boundEntity: BoundEntityResponse): Track => {
        // For now, we'll use a default primaryArtistId of 0
        // In a real implementation, this should come from the API response
        return new TrackImpl(
            boundEntity.masterId,
            boundEntity.masterName,
            0 // TODO: Get actual primaryArtistId from API
        );
    };

    // Load tracks with search parameters
    const loadTracks = async (params: TrackSearchParams) => {
        try {
            // Add artistId to params if provided
            if (artistId) {
                params.artistId = artistId;
            }
            
            const result = await fetchTracks(params);
            
            return result;
        } catch (error) {
            console.error('Error loading tracks:', error);
            throw error;
        }
    }
    
    return (
        <div className={styles.container}>
            <LastfmEntityTable<LastfmTrack, Track>
                fetchEntities={loadTracks}
                fetchMasterEntities={fetchBoundTracks}
                createMasterEntity={createTrack}
                renderHeader={(sort, setSort) => (
                    <LastfmTracksTableHeader sort={sort} setSort={setSort} />
                )}
                renderRow={(track) => (
                    <LastfmTracksTableRow
                        key={track.id}
                        entity={track}
                    />
                )}
                searchPlaceholder="Search track name..."
            />
        </div>
    )
}
