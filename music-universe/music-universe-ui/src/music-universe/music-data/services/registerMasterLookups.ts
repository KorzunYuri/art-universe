import { LookupRegistry } from "@/music-universe/shared/services/LookupRegistry";
import { lookupMasterEntities } from "@/music-universe/music-data/api/music-data-common-lookup";
import type {
    BasicMasterEntityLookupParams,
    BasicMasterLookupRequest,
    MasterArtistRelatedLookupParams,
    MasterArtistRelatedLookupRequest
} from "@/music-universe/music-data/types/music-data-lookup-types";
import type { MasterEntityType } from "@/music-universe/shared/types/entities";

/**
 * Registers lookup configurations for master entities
 * This function is called during app initialization to register all master entity lookups
 */
export function registerMasterLookups() {
    console.log('🔧 Registering master entity lookups...');

    // Register basic entity lookups (artist, category, dimension)
    const registerBasicLookup = (entityType: Extract<MasterEntityType, 'artist' | 'category' | 'dimension'>) => {
        LookupRegistry.register('master', entityType, {
            transformParams: (params: BasicMasterEntityLookupParams): BasicMasterLookupRequest => ({
                search: params.search,
                limit: params.limit
            }),
            
            lookupEntities: async (request: BasicMasterLookupRequest) => {
                return await lookupMasterEntities(entityType, request);
            }
        });
    };

    // Register artist-related entity lookups (album, track)
    const registerArtistRelatedLookup = (entityType: Extract<MasterEntityType, 'album' | 'track'>) => {
        LookupRegistry.register('master', entityType, {
            transformParams: (params: MasterArtistRelatedLookupParams): MasterArtistRelatedLookupRequest => {
                const request: MasterArtistRelatedLookupRequest = {
                    search: params.search,
                    limit: params.limit
                };

                // Use explicit context parameters instead of dynamic extraction
                if (params.context.type === 'artist-related') {
                    if (params.context.masterArtistId !== undefined) {
                        request.masterArtistId = params.context.masterArtistId;
                    }
                    if (params.context.externalArtistId !== undefined) {
                        request.externalArtistId = params.context.externalArtistId;
                    }
                    if (params.context.dataSource !== undefined) {
                        request.dataSource = params.context.dataSource;
                    }
                }

                return request;
            },
            
            lookupEntities: async (request: MasterArtistRelatedLookupRequest) => {
                return await lookupMasterEntities(entityType, request);
            }
        });
    };

    // Register all basic lookups
    registerBasicLookup('artist');
    registerBasicLookup('category');
    registerBasicLookup('dimension');

    // Register all artist-related lookups
    registerArtistRelatedLookup('album');
    registerArtistRelatedLookup('track');

    console.log('✅ Master entity lookups registered');
}
