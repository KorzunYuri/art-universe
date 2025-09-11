import type { MasterEntityType, RawEntity } from "@/music-universe/shared/types/entities";
import type {
    BasicMasterLookupRequest,
    MasterArtistRelatedLookupRequest
} from "@/music-universe/music-data/types/music-data-lookup-types";
import type {LookupEntity} from "@/music-universe/shared/types/lookup.ts";

/**
 * Request parameters for lookup operations
 */
export class LookupRequestSourceParams<T extends MasterEntityType = MasterEntityType> {
    constructor(
        public search: string,
        public rawEntity?: RawEntity<T>
    ) {}
}

/**
 * Response for batch lookup operations
 */
export interface BatchLookupResponseDTO {
    results: Record<string, LookupEntity[]>;
}

/**
 * Map of entity types to their lookup request types
 * Uses the existing typed interfaces instead of inline definitions
 */
export type LookupRequestMap = {
    artist: BasicMasterLookupRequest;
    album: MasterArtistRelatedLookupRequest;
    track: MasterArtistRelatedLookupRequest;
    category: BasicMasterLookupRequest;
};
