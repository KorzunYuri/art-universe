import type { 
    BaseLookupParams, 
    BaseLookupRequest
} from "@/music-universe/shared/types/lookup";
import type { DataSource } from "@/music-universe/sources/shared/types/data-sources";
import type { BasicLookupContext } from "@/music-universe/shared/types/lookup-context";
import type { ArtistRelatedLookupContext } from "@/music-universe/sources/shared/types/lookup-context";

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
    dimension: BasicMasterEntityLookupParams;
};

/**
 * Map of entity types to their request types for master entities
 */
export type MasterLookupRequestMap = {
    artist: BasicMasterLookupRequest;
    album: MasterArtistRelatedLookupRequest;
    track: MasterArtistRelatedLookupRequest;
    category: BasicMasterLookupRequest;
    dimension: BasicMasterLookupRequest;
};
