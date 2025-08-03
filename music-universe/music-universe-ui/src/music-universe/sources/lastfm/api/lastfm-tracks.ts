import { LastfmTrack } from "@/music-universe/sources/lastfm/types/lastfm-track";
import type { Track } from "@/music-universe/shared/types/entities.ts";
import {createLastfmArtistFromDto, type LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {BaseLastfmPageSearchParams} from "@/music-universe/sources/lastfm/api/lastfm-common-fetching.ts";

export interface LastfmTrackDto {
    id: number;
    name: string;
    url: string;
    mbid: string | null;
    approvalStatus: ApprovalStatusType;
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
        dto.artist ? createLastfmArtistFromDto(dto.artist) : undefined,
        masterEntity
    );
}

export interface LastfmTracksPageSearchParams extends BaseLastfmPageSearchParams {
    minPlayCount?: number;
    minListenersCount?: number;
    artistId?: number;
}
