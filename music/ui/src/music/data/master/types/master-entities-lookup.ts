import type { MasterEntityType, RawEntity } from "@/music/shared/types/entities.ts";
import type {
    BasicMasterLookupRequest,
    MasterArtistRelatedLookupRequest
} from "@/music/data/master/types/music-data-lookup-types.ts";
import type {LookupEntity} from "@/music/shared/types/lookup.ts";

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
