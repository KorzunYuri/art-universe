import { LastfmAlbum } from "@/music/data/raw/lastfm/types/lastfm-album.ts";
import type { Album } from "@/music/shared/types/entities.ts";
import {createLastfmArtistFromDto, type LastfmArtistDto} from "@/music/data/raw/lastfm/api/lastfm-artists.ts";
import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import type {BaseLastfmPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-common-fetching.ts";

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
