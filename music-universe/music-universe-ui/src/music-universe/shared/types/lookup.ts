/**
 * Universal lookup result structure
 */
export interface LookupEntity {
    id: number;
    name: string;
}

/**
 * Base lookup request interface (API level)
 */
export interface BaseLookupRequest {
    search: string;
    limit?: number;
}

/**
 * Base lookup parameters interface (business level)
 */
export interface BaseLookupParams {
    search: string;
    limit?: number;
}

/**
 * Lookup function type - takes request and returns entities
 */
export type LookupFunction<TRequest extends BaseLookupRequest> = (request: TRequest) => Promise<LookupEntity[]>;

/**
 * Parameter transformer function type
 */
export type ParamsTransformer<TParams extends BaseLookupParams, TRequest extends BaseLookupRequest> = 
    (params: TParams) => TRequest;

/**
 * Lookup configuration combining transformer and lookup function
 */
export interface LookupConfiguration<TParams extends BaseLookupParams, TRequest extends BaseLookupRequest> {
    transformParams: ParamsTransformer<TParams, TRequest>;
    lookupEntities: LookupFunction<TRequest>;
}
