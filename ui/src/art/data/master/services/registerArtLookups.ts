import { LookupRegistry } from "@/shared/services/LookupRegistry.ts";
import { ArtDataConfig } from "@/art/data/master/config/artdataconfig.ts";
import type { BaseLookupParams, BaseLookupRequest, LookupEntity } from "@/shared/types/lookup.ts";

/**
 * Registers lookup configurations for art entities (person).
 * Called during ArtMasterApp module load.
 */
export function registerArtLookups() {
    console.log('Registering art entity lookups...');

    LookupRegistry.register('master', 'person', {
        transformParams: (params: BaseLookupParams): BaseLookupRequest => ({
            search: params.search,
            limit: params.limit,
        }),

        lookupEntities: async (request: BaseLookupRequest): Promise<LookupEntity[]> => {
            const response = await ArtDataConfig.api.get<{ content: LookupEntity[] }>('/persons', {
                params: {
                    search: request.search,
                    size: request.limit ?? 20,
                },
            });
            return response.data.content;
        },
    });

    console.log('Art entity lookups registered');
}
