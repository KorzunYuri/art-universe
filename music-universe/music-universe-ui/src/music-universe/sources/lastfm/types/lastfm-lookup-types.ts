import type { BaseLookupParams, BaseLookupRequest } from "@/music-universe/shared/types/lookup";
import type { BasicLookupContext } from "@/music-universe/shared/types/lookup-context";

/**
 * LastFM specific lookup parameters
 */
export interface LastfmBasicLookupParams extends BaseLookupParams {
    context: BasicLookupContext;
}

/**
 * LastFM specific lookup requests
 */
export interface LastfmBasicLookupRequest extends BaseLookupRequest {
}
