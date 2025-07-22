import type { Page } from "@/music-universe/shared/types/page";
import { LastfmTrack } from "@/music-universe/sources/lastfm/types/lastfm-track";
import {type BaseLastfmPageSearchParams, fetchEntities} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import type { Track } from "@/music-universe/music-data/types/master-entities";
import type { LastfmArtistDto } from "@/music-universe/sources/lastfm/api/lastfm-artists";

export interface LastfmTrackDto {
    id: number;
    name: string;
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
    artist?: LastfmArtistDto;
}

/**
 * Factory function to create LastfmTrack from API response DTO
 * This function handles the conversion from API DTO to domain entity
 */
export function createLastfmTrackFromDto(dto: LastfmTrackDto, masterEntity?: Track): LastfmTrack {
    return new LastfmTrack(
        dto.id,
        dto.name,
        dto.url,
        dto.mbid,
        dto.approvalStatus,
        dto.playCount,
        dto.listenersCount,
        dto.artist,
        masterEntity
    );
}

export interface LastfmTracksPageSearchParams extends BaseLastfmPageSearchParams {
    minPlayCount?: number;
    minListenersCount?: number;
    artistId?: number;
}

export async function fetchTracks(params: LastfmTracksPageSearchParams): Promise<Page<LastfmTrack>> {
    return fetchEntities(
        'track',
        {
            search: params.search ?? '',
            minPlayCount: params.minPlayCount,
            minListenersCount: params.minListenersCount,
            artistId: params.artistId,
            approvalStatuses: params.approvalStatuses,
            page: params.page ?? 0,
            size: params.size ?? 20,
            sort: params.sort ?? 'name,asc',
        } as LastfmTracksPageSearchParams
    );
}
