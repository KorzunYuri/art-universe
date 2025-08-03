// types
import type { LastfmTrack } from '@/music-universe/sources/lastfm/types/lastfm-track'
import type {MasterEntityType, Track} from "@/music-universe/shared/types/entities.ts";
// components
import { LastfmEntityTable } from '@/music-universe/sources/lastfm/components/LastfmEntityTable'
// utils
import { TrackImpl } from '@/music-universe/shared/types/entities.ts'
// styles
import styles from './LastfmTracksTable.module.css'
import { type BoundEntityResponse} from "@/music-universe/music-data/api/music-data-binding.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";

interface LastfmTracksTableProps {
    artistId?: number;
}

export const LastfmTracksTable = ({ artistId }: LastfmTracksTableProps) => {

    const dataSource: DataSource = 'lastfm';
    const entityType: MasterEntityType = 'track';

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
    
    return (
        <div className={styles.container}>
            <LastfmEntityTable<LastfmTrack, Track>
                fetchEntities={(searchParams) => fetchLastfmEntities(entityType, searchParams)}
                fetchMasterEntities={(rawEntityIds) => fetchBoundMasterEntities(dataSource, entityType, rawEntityIds)}
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
