import type {MasterEntityType, RawEntity} from "@/music-universe/shared/types/entities.ts";

/**
 * Common entity structure returned by lookup operations
 */
export interface LookupEntity {
    id: number;
    name: string;
}

/**
 * Base lookup request interface
 */
export interface BaseLookupRequest {
    search: string;
    limit?: number;
}

export function createBaseLookupRequest(
    search: string,
    limit: number = 20
) {
    return {
        search,
        limit
    }
}

/**
 * Artist-related lookup request interface
 */
export interface ArtistRelatedLookupRequest extends BaseLookupRequest {
    masterArtistId?: number;
    externalArtistId?: number;
}

/**
 * Map of entity types to their lookup request types
 */
export type LookupRequestMap = {
    artist:     BaseLookupRequest;
    album:      ArtistRelatedLookupRequest;
    track:      ArtistRelatedLookupRequest;
    category:   BaseLookupRequest;
    dimension:  BaseLookupRequest;
};

/**
 * Wrapper for parameters that are used to make a LookupRequest or BatchLookupRequest item
 */
export class LookupRequestSourceParams<K extends MasterEntityType> {
    search: string;
    rawEntity?: RawEntity<K>;

    constructor(
        search: string,
        rawEntity: RawEntity<K>
    ) {
        this.search = search;
        this.rawEntity = rawEntity;
    }
}

/**
 * Generic batch lookup request interface
 */
export interface BatchLookupRequest<T extends BaseLookupRequest> {
    searchRequests: T[];
    limit?: number;
}

/**
 * Map of entity types to their batch lookup request types
 */
export type BatchLookupRequestMap = {
    [K in MasterEntityType]: BatchLookupRequest<LookupRequestMap[K]>;
};

/**
 * Generic response DTO for batch lookup operations
 */
export interface BatchLookupResponseDTO {
    results: {
        [searchTerm: string]: LookupEntity[];
    };
}