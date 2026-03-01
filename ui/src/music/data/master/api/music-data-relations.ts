import { MusicDataConfig } from '../config/musicdataconfig.ts';
import type {DataSource} from "@/music/data/raw/shared/types/data-sources.ts";
import type {MasterEntityType} from "@/music/shared/types/entities.ts";

const masterDataApi = MusicDataConfig.api;

/**
 * DTO for relation binding status response
 */
export interface RelationBindingStatusDTO {
    sourceExternalId: number;
    sourceEntityType: string;
    sourceEntityName: string;
    sourceInternalId: number;
    isSourceEntityBound: boolean;
    targetEntityType: string;
    targetBindings: TargetBindingDTO[];
}

/**
 * DTO for target binding information
 */
export interface TargetBindingDTO {
    targetExternalId: number;
    targetEntityName: string;
    targetInternalId: number | null;
    isTargetEntityBound: boolean;
    isInternalRelationBound: boolean;
    internalRelationId: number | null;
    isExternalRelationBound: boolean;
}

/**
 * DTO for relation binding response
 */
export interface RelationBindingDTO {
    relationId: number;
    sourceId: number;
    targetId: number;
}

/**
 * DTO for entity information
 */
export interface EntityDTO {
    id: number;
    name: string;
    entityType: string;
}

/**
 * DTO for relation type info within a grouped related entity
 */
export interface RelationTypeInfo {
    relationId: number;
    relationTypeId: number | null;
    relationTypeName: string | null;
}

/**
 * DTO for related entity with grouped relation type information
 */
export interface RelatedEntityDTO {
    id: number;
    name: string;
    entityType: string;
    relationTypes: RelationTypeInfo[];
    trackOrder: number | null;
}

/**
 * Pair of IDs representing a relation
 */
export interface RelationPair {
    sourceId: number;
    targetId: number;
}

/**
 * Binds an external relation to an internal one in music-data
 * 
 * @param dataSource {DataSource}
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceExternalEntityId External source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param targetExternalEntityId External target entity ID
 * @returns The bound relation if successful, null otherwise
 */
export async function bindExternalRelation(
    dataSource: DataSource,
    sourceEntityType: MasterEntityType,
    sourceExternalEntityId: number,
    targetEntityType: MasterEntityType,
    targetExternalEntityId: number
): Promise<RelationBindingDTO | null> {
    console.log(`🔗 Binding external relation: ${sourceEntityType}:${sourceExternalEntityId} -> ${targetEntityType}:${targetExternalEntityId}`);

    const response = await masterDataApi.post<RelationBindingDTO>(
        `/relations/bind/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}/${targetExternalEntityId}`
    );

    return response.data;
}

/**
 * Unbinds an external relation in music-data
 * 
 * @param dataSource {DataSource}
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceExternalEntityId External source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param targetExternalEntityId External target entity ID
 * @returns True if successful, false otherwise
 */
export async function unbindExternalRelation(
    dataSource: DataSource,
    sourceEntityType: MasterEntityType,
    sourceExternalEntityId: number,
    targetEntityType: MasterEntityType,
    targetExternalEntityId: number
): Promise<boolean> {
    console.log(`🔓 Unbinding external relation: ${sourceEntityType}:${sourceExternalEntityId} -> ${targetEntityType}:${targetExternalEntityId}`);

    const response = await masterDataApi.delete<boolean>(
        `/relations/unbind/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}/${targetExternalEntityId}`
    );

    return response.data;
}

/**
 * Finds bound external relations in music-data
 * 
 * @param dataSource {DataSource}
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceExternalEntityId External source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param targetExternalEntityIds List of external target entity IDs
 * @returns DTO with binding status information
 */
export async function findBoundExternalRelations(
    dataSource: DataSource,
    sourceEntityType: MasterEntityType,
    sourceExternalEntityId: number,
    targetEntityType: MasterEntityType,
    targetExternalEntityIds: number[]
): Promise<RelationBindingStatusDTO> {
    console.log(`🔍 Finding bound external relations for ${targetExternalEntityIds.length} target entities`);

    const response = await masterDataApi.get<RelationBindingStatusDTO>(
        `/relations/bound/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}`,
        {
            params: {
                ids: targetExternalEntityIds.join(',')
            }
        }
    );

    return response.data;
}

/**
 * Gets related entities for a given entity
 *
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceEntityId Source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param relationTypeId Optional filter by relation type
 * @returns List of related entities with relation type information
 */
export async function getRelatedEntities(
    sourceEntityType: MasterEntityType,
    sourceEntityId: number,
    targetEntityType: MasterEntityType,
    relationTypeId?: number
): Promise<RelatedEntityDTO[]> {
    const response = await masterDataApi.get<RelatedEntityDTO[]>(
        `/relations/${sourceEntityType}/${sourceEntityId}/${targetEntityType}`,
        { params: relationTypeId != null ? { relationTypeId } : undefined }
    );

    return response.data;
}

/**
 * Creates internal relations between two master entities.
 * Accepts multiple relation type IDs; when none provided, creates a single untyped relation.
 */
export async function createInternalRelation(
    sourceEntityType: MasterEntityType,
    sourceEntityId: number,
    targetEntityType: MasterEntityType,
    targetEntityId: number,
    relationTypeIds?: number[]
): Promise<number[]> {
    const params = relationTypeIds && relationTypeIds.length > 0
        ? { relationTypeIds: relationTypeIds.join(',') }
        : undefined;

    const response = await masterDataApi.post<number[]>(
        `/relations/internal/${sourceEntityType}/${sourceEntityId}/${targetEntityType}/${targetEntityId}`,
        null,
        { params }
    );

    return response.data;
}

/**
 * Deletes an internal relation by ID
 */
export async function deleteInternalRelation(relationId: number): Promise<boolean> {
    const response = await masterDataApi.delete<boolean>(
        `/relations/internal/${relationId}`
    );

    return response.data;
}
