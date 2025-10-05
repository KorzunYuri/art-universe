import type {BaseEntity} from "@/music/shared/types/entities.ts";
import {LastfmArtist} from "@/music/data/raw/lastfm/types";
import type {Artist} from "@/music/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import type {BaseLastfmPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-common-fetching.ts";
import axios from 'axios';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig.ts';


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
        `${LastfmConfig.writeApiUrl}/artists/search`,
        { searchString }
    );
}