import type {BaseEntity} from "@/music-universe/shared/types/entities.ts";
import {LastfmArtist} from "@/music-universe/sources/lastfm/types";
import type {Artist} from "@/music-universe/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {BaseLastfmPageSearchParams} from "@/music-universe/sources/lastfm/api/lastfm-common-fetching.ts";
import axios from 'axios';
import { LastfmConfig } from '@/music-universe/sources/lastfm/config/lastfmconfig';


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

export interface LastfmArtistsPageSearchParams extends BaseLastfmPageSearchParams {
    minPlayCount?: number;
    minListenersCount?: number;
    tagId?: number;
}

/**
 * Triggers forced search for an artist
 */
export async function searchArtist(searchString: string): Promise<void> {
    await axios.post(
        `${LastfmConfig.baseApiUrl}/artists/search`,
        { searchString }
    );
}