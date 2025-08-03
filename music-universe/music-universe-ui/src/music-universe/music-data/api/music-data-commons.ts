// Map entity type to API endpoint
import {
    type MasterEntityType,
    type Album,
    type Artist,
    type Track,
    type Category,
    type Dimension,
} from "@/music-universe/shared/types/entities.ts";
import type {
    BatchLookupRequestDTO,
    BatchLookupResponseDTO,
    LookupEntity
} from "@/music-universe/music-data/types/master-entities-lookup.ts";
import {MusicDataConfig} from "@/music-universe/music-data/config/musicdataconfig.ts";
import axios from "axios";

export const entityToEndpoint: Record<MasterEntityType, string> = {
    'artist': 'artists',
    'album': 'albums',
    'track': 'tracks',
    'category': 'categories',
    'dimension': 'dimensions'
};

export type EntityTypeMap = {
    artist:     Artist;
    album:      Album;
    track:      Track;
    category:   Category;
    dimension:  Dimension;
};

/**
 * Unified lookup for entities of specific type
 *
 * @param entityType
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching categories
 */
export async function lookupMasterEntities(
    entityType: MasterEntityType,
    query: string,
    limit: number = 10
): Promise<LookupEntity[]> {
    const endpoint = entityToEndpoint[entityType];
    const url = `${MusicDataConfig.baseApiUrl}/${endpoint}/lookup`;
    const params = { name: query, limit: limit };

    const response = await axios.get<LookupEntity[]>(url, { params });
    return response.data;
}

/**
 * Performs batch lookup of entities by multiple names, for page load optimization
 *
 * @param entityType
 * @param searchTerms Array of search strings to look up
 * @param limit Maximum number of results for each name (default: 10)
 * @returns Object with lookup results grouped by search strings
 */
export async function batchLookupMasterEntities(
    entityType: MasterEntityType,
    searchTerms: string[],
    limit: number = 10
): Promise<BatchLookupResponseDTO> {
    const endpoint = entityToEndpoint[entityType];
    const url = `${MusicDataConfig.baseApiUrl}/${endpoint}/lookup/batch`;
    const request: BatchLookupRequestDTO = {
        searchTerms,
        limit
    };

    const response = await axios.post<BatchLookupResponseDTO>(url, request);
    return response.data;
}

