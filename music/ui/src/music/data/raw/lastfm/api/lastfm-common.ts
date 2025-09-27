import axios from 'axios';
import {LastfmConfig} from '@/music/data/raw/lastfm/config/lastfmconfig.ts';

import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";

import {createLastfmArtistFromDto, type LastfmArtistDto} from "@/music/data/raw/lastfm/api/lastfm-artists.ts";
import {createLastfmAlbumFromDto, type LastfmAlbumDto} from "@/music/data/raw/lastfm/api/lastfm-albums.ts";
import {createLastfmTrackFromDto, type LastfmTrackDto} from "@/music/data/raw/lastfm/api/lastfm-tracks.ts";
import {createLastfmTagFromDto, type LastfmTagDto} from "@/music/data/raw/lastfm/api/lastfm-tags.ts";
import type {
    LastfmSupportedEntityType,
    LastfmSupportedEntityTypeMap
} from "@/music/data/raw/lastfm/types/lastfm-entity.ts";


export const lastfmEntityTypeToEndpoint: Record<LastfmSupportedEntityType, string> = {
    'artist': 'artists',
    'album': 'albums',
    'track': 'tracks',
    'category': 'tags'
};

export type LastfmEntityDtoMap = {
    artist:     LastfmArtistDto;
    album:      LastfmAlbumDto;
    track:      LastfmTrackDto;
    category:   LastfmTagDto;
};

export const lastfmEntityMappers: {
    [K in LastfmSupportedEntityType]: (dto: LastfmEntityDtoMap[K]) => LastfmSupportedEntityTypeMap[K];
} = {
    artist:     createLastfmArtistFromDto,
    album:      createLastfmAlbumFromDto,
    track:      createLastfmTrackFromDto,
    category:   createLastfmTagFromDto,
};

/**
 * Generic function to update approval status for any LastFM entity
 *
 * @param entityType
 * @param entityId
 * @param newStatus New approval status
 * @returns The same entity with updated approval status
 */
export async function updateApprovalStatus(
    entityType: LastfmSupportedEntityType,
    entityId: number,
    newStatus: ApprovalStatusType
): Promise<void> {

    const endpoint = lastfmEntityTypeToEndpoint[entityType];
    try {
        await axios.patch(
            `${LastfmConfig.baseApiUrl}/${endpoint}/${entityId}/approval`,
            {
                approvalStatus: newStatus,
            }
        );
    } catch (error) {
        console.error(`Failed to update approval status for ${entityType} ${entityId}:`, error);
        throw error;
    }
}
