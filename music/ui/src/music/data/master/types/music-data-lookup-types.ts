import type { 
    BaseLookupParams, 
    BaseLookupRequest
} from "@/shared/types/lookup.ts";
import type { DataSource } from "@/music/data/raw/shared/types/data-sources.ts";
import type { BasicLookupContext } from "@/shared/types/lookup-context.ts";
import type { ArtistRelatedLookupContext } from "@/music/data/raw/shared/types/lookup-context.ts";

/**
 * Music Data specific lookup parameters
 */
export interface BasicMasterEntityLookupParams extends BaseLookupParams {
    context: BasicLookupContext;
}

export interface MasterArtistRelatedLookupParams extends BaseLookupParams {
    context: ArtistRelatedLookupContext;
}

/**
 * Music Data specific lookup requests (API level)
 */
export interface BasicMasterLookupRequest extends BaseLookupRequest {
}

export interface MasterArtistRelatedLookupRequest extends BaseLookupRequest {
    masterArtistId?: number;
    externalArtistId?: number;
    dataSource?: DataSource;
}

/**
 * Map of entity types to their parameter types for master entities
 */
export type MasterLookupParamsMap = {
    artist: BasicMasterEntityLookupParams;
    album: MasterArtistRelatedLookupParams;
    track: MasterArtistRelatedLookupParams;
    category: BasicMasterEntityLookupParams;
};

/**
 * Map of entity types to their request types for master entities
 */
export type MasterLookupRequestMap = {
    artist: BasicMasterLookupRequest;
    album: MasterArtistRelatedLookupRequest;
    track: MasterArtistRelatedLookupRequest;
    category: BasicMasterLookupRequest;
};
