import type { BaseLookupParams, BaseLookupRequest } from "@/music/shared/types/lookup.ts";
import type { BasicLookupContext } from "@/music/shared/types/lookup-context.ts";

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
