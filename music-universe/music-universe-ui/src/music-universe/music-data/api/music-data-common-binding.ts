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
    type RawEntity,
    type ArtistRelatedRawEntity, type MasterEntityMap
} from "@/music-universe/shared/types/entities.ts";
import {entityToEndpoint} from "@/music-universe/music-data/api/music-data-commons.ts";
import axios from "axios";
import {MusicDataConfig} from "@/music-universe/music-data/config/musicdataconfig.ts";

/**
 * Structures for binding (bindToNew, bindToExisting) and 'fetch bound entities' methods
 */
export interface EntityCreateAndBindRequest {
    entityName: string
}

export interface EntityBindToExistingRequest {
    masterId: number
}

export interface ArtistRelatedEntityCreateAndBindRequest extends EntityCreateAndBindRequest {
    primaryArtistId: number;
}

export interface ArtistRelatedEntityBindToExistingRequest extends EntityBindToExistingRequest {
    primaryArtistId: number;
}


export interface BoundEntityResponse {
    externalId: number;
    dataSource: DataSource;
    masterId: number;
    masterName: string;
}

interface ArtistRelatedBoundEntityResponse extends BoundEntityResponse {
    primaryArtistId: number;
}

type EntityBindingResponseMap = {
    artist:     BoundEntityResponse;
    album:      ArtistRelatedBoundEntityResponse;
    track:      ArtistRelatedBoundEntityResponse;
    category:   BoundEntityResponse;
    dimension:  BoundEntityResponse;
}

export type EntityCreateAndBindRequestMap = {
    artist:     EntityCreateAndBindRequest;
    album:      ArtistRelatedEntityCreateAndBindRequest;
    track:      ArtistRelatedEntityCreateAndBindRequest;
    category:   EntityCreateAndBindRequest;
    dimension:  EntityCreateAndBindRequest;
}

export type EntityBindToExistingRequestMap = {
    artist:     EntityBindToExistingRequest;
    album:      ArtistRelatedEntityBindToExistingRequest;
    track:      ArtistRelatedEntityBindToExistingRequest;
    category:   EntityBindToExistingRequest;
    dimension:  EntityBindToExistingRequest;
}

function createArtistFromBindingResponse(res: BoundEntityResponse): Artist {
    return new ArtistImpl(res.masterId, res.masterName);
}
function createAlbumFromBindingResponse(res: ArtistRelatedBoundEntityResponse): Album {
    return new AlbumImpl(res.masterId, res.masterName, res.primaryArtistId);
}
function createTrackFromBindingResponse(res: ArtistRelatedBoundEntityResponse): Track {
    return new TrackImpl(res.masterId, res.masterName, res.primaryArtistId);
}
function createCategoryFromBindingResponse(res: BoundEntityResponse): Category {
    return new CategoryImpl(res.masterId, res.masterName);
}
function createDimensionFromBindingResponse(res: BoundEntityResponse): Dimension {
    return new DimensionImpl(res.masterId, res.masterName);
}

const bindingResponseMappers: {
    [K in MasterEntityType]: (dto: EntityBindingResponseMap[K]) => MasterEntityMap[K];
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
    masterEntity: MasterEntityMap[K];
}

function makeBoundEntityInfoMapper<K extends MasterEntityType>(entityType: K) {
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

    return response.data.map(makeBoundEntityInfoMapper(entityType));
}

/**
 * Bind an entity from data source to an existing master entity
 *
 * @returns The bound master entity
 * @param masterId master entity id to bind entity to
 * @param entity raw entity to bind to existing master entity
 */
export async function bindRawEntityToExistingMaster<K extends MasterEntityType>(
    masterId: number,
    entity: RawEntity<K>
): Promise<BoundEntityInfo<K>> {
    const endpoint = entityToEndpoint[entity.getEntityType()];
    const response = await axios.post<EntityBindingResponseMap[K]>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}/bind/existing/${entity.getDataSource()}/${entity.id}`,
        toBindToExistingRequest(masterId, entity)
    );

    return makeBoundEntityInfoMapper(entity.getEntityType())(response.data);
}

/**
 * Creates a new entity and binds it to raw entity from an external source
 *
 * @returns The bound master entity
 * @param entityName effective name of master entity that will be created
 * @param entity raw entity to create master entity from
 */
export async function bindRawEntityToNewMaster<K extends MasterEntityType>(
    entityName: string,
    entity: RawEntity<K>
): Promise<BoundEntityInfo<K>> {
    const endpoint = entityToEndpoint[entity.getEntityType()];
    const response = await axios.post<EntityBindingResponseMap[K]>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}/bind/new/${entity.getDataSource()}/${entity.id}`,
        toCreateAndBindRequest(entityName, entity)
    );

    return makeBoundEntityInfoMapper(entity.getEntityType())(response.data);
}

/**
 * Helper function to create a request for artist-related entities
 */
function createArtistRelatedRequest<T extends 'create' | 'bind', K extends MasterEntityType>(
    baseRequest: T extends 'create' ? { entityName: string } : { masterId: number },
    entity: ArtistRelatedRawEntity<K>
): T extends 'create' ? ArtistRelatedEntityCreateAndBindRequest : ArtistRelatedEntityBindToExistingRequest {
    const artistId = entity.getExternalArtistId();

    if (!artistId) {
        throw new Error("Cannot create artist-related binding request: artist ID is missing");
    }

    return {
        ...baseRequest,
        primaryArtistId: artistId
    } as T extends 'create' ? ArtistRelatedEntityCreateAndBindRequest : ArtistRelatedEntityBindToExistingRequest;
}

/**
 * Creates a request for creating and binding a new master entity
 *
 * @param entityName Name for the new master entity
 * @param entity Raw entity to bind
 * @returns Request object for creating and binding
 */
function toCreateAndBindRequest<T extends MasterEntityType>(
    entityName: string,
    entity: RawEntity<T>
): EntityCreateAndBindRequestMap[T] {
    const baseRequest = { entityName };

    // For track entities, we need to include the primary artist ID
    if (entity.getEntityType() === 'track' && isArtistRelatedEntity(entity)) {
        return createArtistRelatedRequest<'create', T>(baseRequest, entity) as EntityCreateAndBindRequestMap[T];
    }

    return baseRequest as EntityCreateAndBindRequestMap[T];
}

/**
 * Creates a request for binding to an existing master entity
 *
 * @param masterId ID of the existing master entity
 * @param entity Raw entity to bind
 * @returns Request object for binding to existing
 */
function toBindToExistingRequest<T extends MasterEntityType>(
    masterId: number,
    entity: RawEntity<T>
): EntityBindToExistingRequestMap[T] {
    const baseRequest = { masterId };

    // For track entities, we need to include the primary artist ID
    if (entity.getEntityType() === 'track' && isArtistRelatedEntity(entity)) {
        return createArtistRelatedRequest<'bind', T>(baseRequest, entity) as EntityBindToExistingRequestMap[T];
    }

    return baseRequest as EntityBindToExistingRequestMap[T];
}

/**
 * Type guard to check if an entity implements ArtistRelatedRawEntity
 */
function isArtistRelatedEntity<T extends MasterEntityType>(entity: RawEntity<T>): entity is ArtistRelatedRawEntity<T> {
    return 'getExternalArtistId' in entity && typeof entity.getExternalArtistId === 'function';
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
