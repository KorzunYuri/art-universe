import { LastfmAlbum } from "@/music-universe/sources/lastfm/types/lastfm-album";
import type { Album } from "@/music-universe/shared/types/entities.ts";
import {createLastfmArtistFromDto, type LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {BaseLastfmPageSearchParams} from "@/music-universe/sources/lastfm/api/lastfm-common-fetching.ts";

export interface LastfmAlbumDto {
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
 * Factory function to create LastfmAlbum from API response DTO
 * This function handles the conversion from API DTO to domain entity
 */
export function createLastfmAlbumFromDto(dto: LastfmAlbumDto, masterEntity?: Album): LastfmAlbum {
    return new LastfmAlbum(
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

export interface LastfmAlbumsPageSearchParams extends BaseLastfmPageSearchParams {
    minPlayCount?: number;
    minListenersCount?: number;
    artistId?: number;
    tagId?: number;
}
