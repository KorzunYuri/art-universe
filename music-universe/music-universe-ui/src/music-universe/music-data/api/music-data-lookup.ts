import type {MasterEntityType, RawEntity, ArtistRelatedRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {
    BatchLookupResponseDTO,
    LookupEntity, LookupRequestMap, LookupRequestSourceParams
} from "@/music-universe/music-data/types/master-entities-lookup.ts";
import {MusicDataConfig} from "@/music-universe/music-data/config/musicdataconfig.ts";
import axios from "axios";
import {entityToEndpoint} from "@/music-universe/music-data/api/music-data-commons.ts";

/**
 * Performs batch lookup of master entities for a specific entity
 *
 * @param entityType
 * @param sourceParams parameters used to build the request
 * @returns List of matching categories
 */
export async function lookupMasterEntities<K extends MasterEntityType>(
    entityType: K,
    sourceParams: LookupRequestSourceParams<K>
): Promise<LookupEntity[]> {
    const endpoint = entityToEndpoint[entityType];
    const url = `${MusicDataConfig.baseApiUrl}/${endpoint}/lookup`;
    const params = toLookupRequest(sourceParams);

    const response = await axios.get<LookupEntity[]>(url, { params });
    return response.data;
}

/**
 * Performs batch lookup of master entities by multiple names, for page load optimization.
 * Master entities are always searched for specific raw entities
 *
 * @param entityType
 * @param sourceParams raw entity to search master entities for
 * @param limit default row limit for each lookup list
 * @returns Object with lookup results grouped by search strings
 */
export async function batchLookupMasterEntities<K extends MasterEntityType>(
    entityType: K,
    sourceParams: LookupRequestSourceParams<K>[],
    limit: number = 10
): Promise<BatchLookupResponseDTO> {
    const endpoint = entityToEndpoint[entityType];
    const url = `${MusicDataConfig.baseApiUrl}/${endpoint}/lookup/batch`;
    const request = {
        searchRequests: sourceParams.map(toLookupRequest),
        limit
    };

    const response = await axios.post<BatchLookupResponseDTO>(url, request);
    return response.data;
}

/**
 * Creates a lookup request based on a raw entity
 *
 * @returns Lookup request object appropriate for the entity type
 * @param sourceParams parameters used to build the request
 */
function toLookupRequest<T extends MasterEntityType>(
    sourceParams: LookupRequestSourceParams<T>
): LookupRequestMap[T] {
    // Base request for all entity types
    const baseRequest = { search: sourceParams.search };
    const entity = sourceParams.rawEntity;

    // For track and album entities, add artist-related parameters if available
    if (
        (entity.getEntityType() === 'track' || entity.getEntityType() === 'album')
        && isArtistRelatedEntity(entity)
    ) {
        const artistRelatedEntity = entity as ArtistRelatedRawEntity<T>;

        return {
            ...baseRequest,
            ...(artistRelatedEntity.getExternalArtistId() !== undefined ?
                { externalArtistId: artistRelatedEntity.getExternalArtistId() } : {}),
            ...(artistRelatedEntity.getMasterArtistId() !== undefined ?
                { masterArtistId: artistRelatedEntity.getMasterArtistId() } : {})
        } as LookupRequestMap[T];
    }

    return baseRequest as LookupRequestMap[T];
}

/**
 * Type guard to check if an entity implements ArtistRelatedRawEntity
 */
function isArtistRelatedEntity<T extends MasterEntityType>(entity: RawEntity<T>): entity is ArtistRelatedRawEntity<T> {
    return 'getExternalArtistId' in entity &&
           typeof entity.getExternalArtistId === 'function' &&
           'getMasterArtistId' in entity &&
           typeof entity.getMasterArtistId === 'function';
}
