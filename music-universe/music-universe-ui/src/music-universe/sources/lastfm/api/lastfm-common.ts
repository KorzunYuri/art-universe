import axios from 'axios';
import {LastfmConfig} from '@/music-universe/sources/lastfm/config/lastfmconfig';

import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {BasePageSearchParams, Page} from "@/music-universe/shared/types/page.ts";

import {createLastfmArtistFromDto, type LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import {createLastfmTrackFromDto, type LastfmTrackDto} from "@/music-universe/sources/lastfm/api/lastfm-tracks.ts";
import {createLastfmTagFromDto, type LastfmTagDto} from "@/music-universe/sources/lastfm/api/lastfm-tags.ts";
import type {
    LastfmSupportedEntityType,
    LastfmSupportedEntityTypeMap
} from "@/music-universe/sources/lastfm/types/lastfm-entity.ts";


const entityTypeToEndpoint: Record<LastfmSupportedEntityType, string> = {
    'artist': 'artists',
    'track': 'tracks',
    'category': 'tags'
};

type EntityDtoMap = {
    artist:     LastfmArtistDto;
    track:      LastfmTrackDto;
    category:   LastfmTagDto;
};

const entityMappers: {
    [K in LastfmSupportedEntityType]: (dto: EntityDtoMap[K]) => LastfmSupportedEntityTypeMap[K];
} = {
    artist:     createLastfmArtistFromDto,
    track:      createLastfmTrackFromDto,
    category:   createLastfmTagFromDto,
};

export interface BaseLastfmPageSearchParams extends BasePageSearchParams {
    approvalStatuses?: number[];
}

export async function fetchLastfmEntities<T extends LastfmSupportedEntityType>(
    entityType: T,
    params: BaseLastfmPageSearchParams
): Promise<Page<LastfmSupportedEntityTypeMap[T]>> {

    const endpoint = entityTypeToEndpoint[entityType];
    const response = await axios.get<Page<EntityDtoMap[T]>>(
        `${LastfmConfig.baseApiUrl}/${endpoint}`,
        {
            params: {
                ...params,
                approvalStatuses: params.approvalStatuses?.join(',')
            },
        }
    );

    return {
        ...response.data,
        content: response.data.content.map(entityMappers[entityType])
    };
}

export async function fetchLastfmEntity<T extends LastfmSupportedEntityType>(
    entityType: T,
    id: number
): Promise<LastfmSupportedEntityTypeMap[T]> {

    const endpoint = entityTypeToEndpoint[entityType];
    const response = await axios.get<EntityDtoMap[T]>(
        `${LastfmConfig.baseApiUrl}/${endpoint}/${id}`
    );

    return entityMappers[entityType](response.data);
}

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

    const endpoint = entityTypeToEndpoint[entityType];
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
