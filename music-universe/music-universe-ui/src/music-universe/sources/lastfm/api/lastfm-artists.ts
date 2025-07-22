import {type BaseLastfmPageSearchParams, fetchEntities} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import type {BaseEntity} from "@/music-universe/shared/types/entities.ts";
import {LastfmArtist} from "@/music-universe/sources/lastfm/types";
import type {Page} from "@/music-universe/shared/types/page.ts";
import type {Artist} from "@/music-universe/music-data/types/master-entities.ts";

export interface LastfmArtistsPageSearchParams extends BaseLastfmPageSearchParams{
    minPlayCount?: number;
    minListenersCount?: number;
}

export interface LastfmArtistDto extends BaseEntity {
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
}

export async function fetchArtists(params: LastfmArtistsPageSearchParams) : Promise<Page<LastfmArtist>> {
    return fetchEntities(
        'artist',
        {
            search: params.search ?? '',
            minPlayCount: params.minPlayCount,
            minListenersCount: params.minListenersCount,
            approvalStatuses: params.approvalStatuses,
            page: params.page ?? 0,
            size: params.size ?? 20,
            sort: params.sort ?? 'name,asc',
        } as LastfmArtistsPageSearchParams
    )
}

/**
 * Factory function to create LastfmArtist from API response DTO
 */
export function createLastfmArtistFromDto(dto: LastfmArtistDto, masterEntity?: Artist): LastfmArtist {
    return new LastfmArtist(
        dto.id,
        dto.name,
        dto.url,
        dto.mbid,
        dto.approvalStatus,
        dto.playCount,
        dto.listenersCount,
        masterEntity
    );
}