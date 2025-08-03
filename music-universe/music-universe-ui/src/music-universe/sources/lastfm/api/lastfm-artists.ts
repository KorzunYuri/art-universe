import {type BaseLastfmPageSearchParams} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import type {BaseEntity} from "@/music-universe/shared/types/entities.ts";
import {LastfmArtist} from "@/music-universe/sources/lastfm/types";
import type {Artist} from "@/music-universe/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

export interface LastfmArtistsPageSearchParams extends BaseLastfmPageSearchParams{
    minPlayCount?: number;
    minListenersCount?: number;
}

export interface LastfmArtistDto extends BaseEntity {
    url: string;
    mbid: string | null;
    approvalStatus: ApprovalStatusType;
    playCount: number | null;
    listenersCount: number | null;
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