/**
 * Contains interfaces and methods related to binding raw entities to master entities
 */

import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {
    type MasterEntityType,
    type Album, AlbumImpl,
    type Artist, ArtistImpl,
    type Track, TrackImpl,
    type Category, CategoryImpl,
    type Dimension, DimensionImpl,
    type RawEntity
} from "@/music-universe/shared/types/entities.ts";
import {entityToEndpoint, type EntityTypeMap} from "@/music-universe/music-data/api/music-data-commons.ts";
import axios from "axios";
import {MusicDataConfig} from "@/music-universe/music-data/config/musicdataconfig.ts";

/**
 * Structures for binding (bindToNew, bindToExisting) and 'fetch bound entities' methods
 */
interface CreateAndBindRequest {
    entityName: string
}

interface TrackCreateAndBindRequest extends CreateAndBindRequest {
    artistExternalId: number | undefined;
}

export interface BoundEntityResponse {
    externalId: number;
    dataSource: DataSource;
    masterId: number;
    masterName: string;
}

interface TrackBoundEntityResponse extends BoundEntityResponse {
    primaryArtistId: number;
}

type EntityBindingResponseMap = {
    artist:     BoundEntityResponse;
    album:      BoundEntityResponse;
    track:      TrackBoundEntityResponse;
    category:   BoundEntityResponse;
    dimension:  BoundEntityResponse;
}

export type EntityCreateAndBindRequestMap = {
    artist:     CreateAndBindRequest;
    album:      CreateAndBindRequest;
    track:      TrackCreateAndBindRequest;
    category:   CreateAndBindRequest;
    dimension:  CreateAndBindRequest;
}

function createArtistFromBindingResponse(res: BoundEntityResponse): Artist {
    return new ArtistImpl(res.masterId, res.masterName);
}
function createAlbumFromBindingResponse(res: BoundEntityResponse): Album {
    return new AlbumImpl(res.masterId, res.masterName);
}
function createTrackFromBindingResponse(res: TrackBoundEntityResponse): Track {
    return new TrackImpl(res.masterId, res.masterName, res.primaryArtistId);
}
function createCategoryFromBindingResponse(res: BoundEntityResponse): Category {
    return new CategoryImpl(res.masterId, res.masterName);
}
function createDimensionFromBindingResponse(res: BoundEntityResponse): Dimension {
    return new DimensionImpl(res.masterId, res.masterName);
}

const bindingResponseMappers: {
    [K in MasterEntityType]: (dto: EntityBindingResponseMap[K]) => EntityTypeMap[K];
} = {
    artist:     createArtistFromBindingResponse,
    album:      createAlbumFromBindingResponse,
    track:      createTrackFromBindingResponse,
    category:   createCategoryFromBindingResponse,
    dimension:  createDimensionFromBindingResponse,
};


/**
 * Bound entity response adapted for UI needs
 */
export interface BoundEntityInfo<K extends MasterEntityType> {
    dataSource: DataSource;
    entityType: K;
    externalId: number;
    masterEntity: EntityTypeMap[K];
}

function makeEntityInfoMapper<K extends MasterEntityType>(entityType: K) {
    return (dto: EntityBindingResponseMap[K]): BoundEntityInfo<K> => ({
        dataSource: dto.dataSource,
        entityType,
        externalId: dto.externalId,
        masterEntity: bindingResponseMappers[entityType](dto),
    });
}

/**
 * ***************
 * * API METHODS *
 * ***************
 */

/**
 * Fetches bound master entities
 *
 * @param dataSource
 * @param entityType
 * @param externalIds List of external IDs to check
 * @returns List of bound master entities
 */
export async function fetchBoundMasterEntities<K extends MasterEntityType>(
    dataSource: DataSource,
    entityType: K,
    externalIds: number[]
): Promise<BoundEntityInfo<K>[]> {
    const endpoint = entityToEndpoint[entityType];
    const url = `${MusicDataConfig.baseApiUrl}/${endpoint}/bound/${dataSource}`;
    const response = await axios.get<EntityBindingResponseMap[K][]>(
        url,
        {
            params: {
                externalIds: externalIds.join(','),
            },
        }
    );

    return response.data.map(makeEntityInfoMapper(entityType));
}

/**
 * Bind an entity from data source to an existing master entity
 *
 * @param dataSource
 * @param entityType
 * @param externalId The data source entity ID
 * @param masterId The master entity ID in music-data
 * @returns The bound master entity
 */
export async function bindRawEntityToExistingMaster<K extends MasterEntityType>(
    dataSource: DataSource,
    entityType: K,
    externalId: number,
    masterId: number
): Promise<BoundEntityInfo<K>> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.post<EntityBindingResponseMap[K]>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}/bind/existing/${dataSource}/${externalId}`,
        {
            masterId: masterId
        }
    );

    return makeEntityInfoMapper(entityType)(response.data);
}

/**
 * Creates a new entity and binds it to raw entity from an external source
 *
 * @param dataSource
 * @param entityType
 * @param rawEntityId
 * @param request
 * @returns The bound master entity
 */
export async function bindRawEntityToNewMaster<K extends MasterEntityType>(
    dataSource: DataSource,
    entityType: K,
    rawEntityId: number,
    request: EntityCreateAndBindRequestMap[K]
): Promise<BoundEntityInfo<K>> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.post<EntityBindingResponseMap[K]>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}/bind/new/${dataSource}/${rawEntityId}`,
        request
    );

    return makeEntityInfoMapper(entityType)(response.data);
}

/**
 * Converter of raw entities to CreateAndBindRequest, to be implemented by every data source
 * Unifying the raw entities' structure for these need is inconvenient so we will let the caller make the job.
 */
export interface RawEntityToCreateAndBindRequestConverter {
    toBindRequest<T extends MasterEntityType>(
        entity: RawEntity<T>,
        entityName: string
    ): EntityCreateAndBindRequestMap[T];
}

/**
 * Unbinds a raw entity from master
 *
 * @param dataSource
 * @param entityType
 * @param externalId The data source entity ID
 * @returns True if successful, false otherwise
 */
export async function unbindRawEntity(
    dataSource: DataSource,
    entityType: MasterEntityType,
    externalId: number
): Promise<boolean> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}/unbind/${dataSource}/${externalId}`
    );

    return response.data;
}
