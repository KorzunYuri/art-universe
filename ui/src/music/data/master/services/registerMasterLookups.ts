import { LookupRegistry } from "@/shared/services/LookupRegistry.ts";
import { lookupMasterEntities } from "@/music/data/master/api/music-data-common-lookup.ts";
import type {
    BasicMasterEntityLookupParams,
    BasicMasterLookupRequest,
    MasterArtistRelatedLookupParams,
    MasterArtistRelatedLookupRequest
} from "@/music/data/master/types/music-data-lookup-types.ts";
import type { MasterEntityType } from "@/music/shared/types/entities.ts";

/**
 * Registers lookup configurations for master entities
 * This function is called during app initialization to register all master entity lookups
 */
export function registerMasterLookups() {
    console.log('🔧 Registering master entity lookups...');

    // Register basic entity lookups (artist, category)
    const registerBasicLookup = (entityType: Extract<MasterEntityType, 'artist' | 'category'>) => {
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

    // Register all artist-related lookups
    registerArtistRelatedLookup('album');
    registerArtistRelatedLookup('track');

    console.log('✅ Master entity lookups registered');
}
