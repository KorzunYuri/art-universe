import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";

/**
 * DTO for relation binding status response
 */
export interface RelationBindingStatusDTO {
    sourceExternalId: number;
    sourceEntityType: string;
    sourceEntityName: string;
    sourceInternalId: number;
    sourceEntityBound: boolean;
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
    targetEntityBound: boolean;
    internalRelationBound: boolean;
    internalRelationId: number | null;
    externalRelationBound: boolean;
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
    sourceEntityType: string,
    sourceExternalEntityId: number,
    targetEntityType: string,
    targetExternalEntityId: number
): Promise<RelationBindingDTO | null> {
    console.log(`🔗 Binding external relation: ${sourceEntityType}:${sourceExternalEntityId} -> ${targetEntityType}:${targetExternalEntityId}`);
    
    const response = await axios.post<RelationBindingDTO>(
        `${MusicDataConfig.baseApiUrl}/relations/bind/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}/${targetExternalEntityId}`
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
    sourceEntityType: string,
    sourceExternalEntityId: number,
    targetEntityType: string,
    targetExternalEntityId: number
): Promise<boolean> {
    console.log(`🔓 Unbinding external relation: ${sourceEntityType}:${sourceExternalEntityId} -> ${targetEntityType}:${targetExternalEntityId}`);
    
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/relations/unbind/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}/${targetExternalEntityId}`
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
    sourceEntityType: string,
    sourceExternalEntityId: number,
    targetEntityType: string,
    targetExternalEntityIds: number[]
): Promise<RelationBindingStatusDTO> {
    console.log(`🔍 Finding bound external relations for ${targetExternalEntityIds.length} target entities`);
    
    const response = await axios.get<RelationBindingStatusDTO>(
        `${MusicDataConfig.baseApiUrl}/relations/bound/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}`,
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
 * @returns List of related entities
 */
export async function getRelatedEntities(
    sourceEntityType: string,
    sourceEntityId: number,
    targetEntityType: string
): Promise<EntityDTO[]> {
    console.log(`🔍 Getting ${targetEntityType}s related to ${sourceEntityType} ${sourceEntityId}`);
    
    const response = await axios.get<EntityDTO[]>(
        `${MusicDataConfig.baseApiUrl}/relations/${sourceEntityType}/${sourceEntityId}/${targetEntityType}`
    );
    
    return response.data;
}

/**
 * Helper function to create a RelationPair
 * 
 * @param sourceId Source entity ID
 * @param targetId Target entity ID
 * @returns RelationPair object
 */
export function createRelationPair(sourceId: number, targetId: number): RelationPair {
    return { sourceId, targetId };
}
