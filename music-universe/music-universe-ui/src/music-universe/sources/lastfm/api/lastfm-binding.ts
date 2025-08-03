import type {MasterEntityType, RawEntity} from "@/music-universe/shared/types/entities.ts";
import {LastfmTrack} from "@/music-universe/sources/lastfm/types";
import type {
    EntityCreateAndBindRequestMap,
    RawEntityToCreateAndBindRequestConverter
} from "@/music-universe/music-data/api/music-data-binding.ts";

function isLastfmTrack(entity: RawEntity<any>): entity is LastfmTrack {
    return entity instanceof LastfmTrack;
}

/**
 * Factory function for converting Lastfm entities to CreateAndBind requests
 */
export const lastfmEntityToCreateAndBindConverter: RawEntityToCreateAndBindRequestConverter = {
    toBindRequest<T extends MasterEntityType>(
        entity: RawEntity<T>
    ): EntityCreateAndBindRequestMap[T] {

        const baseRequest = { entityName: entity.name };

        if (entity.getEntityType() === 'track' && isLastfmTrack(entity)) {
            return {
                ...baseRequest,
                artistExternalId: entity.getPrimaryArtistId()
            } as EntityCreateAndBindRequestMap[T];
        }

        return baseRequest as EntityCreateAndBindRequestMap[T];
    }
};