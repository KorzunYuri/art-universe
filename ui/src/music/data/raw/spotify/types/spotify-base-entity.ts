import {
    BaseRawEntity,
    type MasterEntityMap,
    type MasterEntityType
} from "@/music/shared/types/entities.ts";
import type { DataSource } from "@/music/data/raw/shared/types/data-sources.ts";

/**
 * Base class for all Spotify entities.
 * No approval status — Spotify entities are considered approved by default.
 */
export abstract class BaseSpotifyEntity<T extends MasterEntityType>
    extends BaseRawEntity<T>
{
    constructor(
        id: number,
        name: string,
        masterEntity?: MasterEntityMap[T]
    ) {
        super(id, name, masterEntity);
    }

    getDataSource(): DataSource {
        return 'spotify';
    }
}
