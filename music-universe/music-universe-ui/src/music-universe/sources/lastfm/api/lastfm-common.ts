import axios from 'axios';
import {LastfmConfig} from '@/music-universe/sources/lastfm/config/lastfmconfig';

import type {MasterEntityType} from "@/music-universe/music-data/types/master-entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {BasePageSearchParams, Page} from "@/music-universe/shared/types/page.ts";
import {LastfmArtist} from "@/music-universe/sources/lastfm/types";

import {LastfmTrack} from "@/music-universe/sources/lastfm/types/lastfm-track.ts";
import {LastfmTag} from "@/music-universe/sources/lastfm/types/lastfm-tag.ts";
import {createLastfmArtistFromDto, type LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import {createLastfmTrackFromDto, type LastfmTrackDto} from "@/music-universe/sources/lastfm/api/lastfm-tracks.ts";
import {createLastfmTagFromDto, type LastfmTagDto} from "@/music-universe/sources/lastfm/api/lastfm-tags.ts";

export type SupportedMasterEntityType = Extract<
    MasterEntityType,
    'artist' | 'track' | 'category'
>;

// Map entity type to API endpoint
const entityTypeToEndpoint: Record<SupportedMasterEntityType, string> = {
    'artist': 'artists',
    'track': 'tracks',
    'category': 'tags'
};

// Define DTO types for each entity type
type EntityDtoMap = {
    artist: LastfmArtistDto;
    track: LastfmTrackDto;
    category: LastfmTagDto;
};

// Define entity types for each entity type
type EntityTypeMap = {
    artist: LastfmArtist;
    track: LastfmTrack;
    category: LastfmTag;
};

// Map entity type to DTO type and entity type
interface EntityTypeMapping<K extends SupportedMasterEntityType> {
    dtoType: EntityDtoMap[K];
    entityType: EntityTypeMap[K];
    createEntity: (dto: EntityDtoMap[K]) => EntityTypeMap[K];
}

const entityMappings: {
    [K in SupportedMasterEntityType]: EntityTypeMapping<K>
} = {
    artist: {
        dtoType: {} as LastfmArtistDto,
        entityType: {} as LastfmArtist,
        createEntity: createLastfmArtistFromDto,
    },
    track: {
        dtoType: {} as LastfmTrackDto,
        entityType: {} as LastfmTrack,
        createEntity: createLastfmTrackFromDto,
    },
    category: {
        dtoType: {} as LastfmTagDto,
        entityType: {} as LastfmTag,
        createEntity: createLastfmTagFromDto,
    },
};

function getEntityMapping<T extends SupportedMasterEntityType>(
    type: T
): EntityTypeMapping<T> {
    return entityMappings[type];
}

export interface BaseLastfmPageSearchParams extends BasePageSearchParams {
    approvalStatuses?: number[];
}

export async function fetchEntities<T extends SupportedMasterEntityType>(
    entityType: T,
    params: BaseLastfmPageSearchParams
): Promise<Page<EntityTypeMap[T]>> {
    const endpoint = entityTypeToEndpoint[entityType];
    const entityMapping = getEntityMapping(entityType);

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
        content: response.data.content.map(entityMapping.createEntity)
    };
}

export async function fetchEntity<T extends SupportedMasterEntityType>(
    entityType: T,
    id: number
): Promise<EntityTypeMap[T]> {
    const endpoint = entityTypeToEndpoint[entityType];
    if (!endpoint) {
        throw new Error(`Unsupported entity type: ${entityType}`);
    }

    const mapping = getEntityMapping(entityType);

    const response = await axios.get<EntityDtoMap[T]>(
        `${LastfmConfig.baseApiUrl}/${endpoint}/${id}`
    );

    return mapping.createEntity(response.data);
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
    entityType: SupportedMasterEntityType,
    entityId: number,
    newStatus: ApprovalStatusType
): Promise<void> {

    const endpoint = entityTypeToEndpoint[entityType];

    if (!endpoint) {
        throw new Error(`Unknown entity type: ${entityType}`);
    }

    try {
        // Make API call to update approval status
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
