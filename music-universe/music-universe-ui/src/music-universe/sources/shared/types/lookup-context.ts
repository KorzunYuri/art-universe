import type { DataSource } from "@/music-universe/sources/shared/types/data-sources";
import type { RawEntity, MasterEntityType, ArtistRelatedRawEntity } from "@/music-universe/shared/types/entities";
import type { BasicLookupContext } from "@/music-universe/shared/types/lookup-context";
import { LookupContextFactory } from "@/music-universe/shared/types/lookup-context";

/**
 * Artist-related lookup context for album/track searches with artist scope
 * This context is specific to raw entities from external sources
 */
export interface ArtistRelatedLookupContext {
    type: 'artist-related';
    dataSource?: DataSource;
    externalArtistId?: number;
    masterArtistId?: number;
}

/**
 * Union type for all raw entity lookup contexts
 */
export type RawEntityLookupContext = BasicLookupContext | ArtistRelatedLookupContext;

/**
 * Type guard to check if an entity implements ArtistRelatedRawEntity
 */
function isArtistRelatedEntity<T extends MasterEntityType>(
    entity: RawEntity<T>
): entity is ArtistRelatedRawEntity<T> {
    return (entity.getEntityType() === 'track' || entity.getEntityType() === 'album') &&
           'getExternalArtistId' in entity && 
           typeof entity.getExternalArtistId === 'function' &&
           'getMasterArtistId' in entity && 
           typeof entity.getMasterArtistId === 'function';
}

/**
 * Factory for creating lookup contexts for raw entities
 * Automatically determines the appropriate context based on entity type and data
 */
export const RawEntityLookupContextFactory = {
    /**
     * Automatically determines context based on raw entity type and data
     * - For album/track entities: creates artist-related context with extracted parameters
     * - For other entities: creates basic context
     */
    fromRawEntity<T extends MasterEntityType>(entity: RawEntity<T>): RawEntityLookupContext {
        // Check if this is an artist-related entity (album or track)
        if (isArtistRelatedEntity(entity)) {
            const context: ArtistRelatedLookupContext = {
                type: 'artist-related',
                dataSource: entity.getDataSource()
            };

            const externalArtistId = entity.getExternalArtistId();
            if (externalArtistId !== undefined) {
                context.externalArtistId = externalArtistId;
            }

            const masterArtistId = entity.getMasterArtistId();
            if (masterArtistId !== undefined) {
                context.masterArtistId = masterArtistId;
            }

            return context;
        }

        return LookupContextFactory.basic();
    }
};
