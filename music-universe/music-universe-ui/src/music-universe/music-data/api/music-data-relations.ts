import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';

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
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceExternalEntityId External source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param targetExternalEntityId External target entity ID
 * @returns The bound relation if successful, null otherwise
 */
export async function bindExternalRelation(
    dataSource: string,
    sourceEntityType: string,
    sourceExternalEntityId: number,
    targetEntityType: string,
    targetExternalEntityId: number
): Promise<RelationBindingDTO | null> {
    try {
        console.log(`🔗 Binding external relation: ${sourceEntityType}:${sourceExternalEntityId} -> ${targetEntityType}:${targetExternalEntityId}`);
        
        const response = await axios.post<ApiResponse<RelationBindingDTO>>(
            `${MusicDataConfig.baseApiUrl}/relations/bind/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}/${targetExternalEntityId}`
        );
        
        if (response.data.success && response.data.data) {
            return response.data.data;
        }
        
        return null;
    } catch (error) {
        console.error('❌ Error binding external relation:', error);
        return null;
    }
}

/**
 * Unbinds an external relation in music-data
 * 
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceExternalEntityId External source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param targetExternalEntityId External target entity ID
 * @returns True if successful, false otherwise
 */
export async function unbindExternalRelation(
    dataSource: string,
    sourceEntityType: string,
    sourceExternalEntityId: number,
    targetEntityType: string,
    targetExternalEntityId: number
): Promise<boolean> {
    try {
        console.log(`🔓 Unbinding external relation: ${sourceEntityType}:${sourceExternalEntityId} -> ${targetEntityType}:${targetExternalEntityId}`);
        
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/relations/unbind/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}/${targetExternalEntityId}`
        );
        
        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error unbinding external relation:', error);
        return false;
    }
}

/**
 * Finds bound external relations in music-data
 * 
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param sourceEntityType Source entity type (e.g., 'ARTIST')
 * @param sourceExternalEntityId External source entity ID
 * @param targetEntityType Target entity type (e.g., 'CATEGORY')
 * @param targetExternalEntityIds List of external target entity IDs
 * @returns DTO with binding status information
 */
export async function findBoundExternalRelations(
    dataSource: string,
    sourceEntityType: string,
    sourceExternalEntityId: number,
    targetEntityType: string,
    targetExternalEntityIds: number[]
): Promise<RelationBindingStatusDTO> {
    try {
        console.log(`🔍 Finding bound external relations for ${targetExternalEntityIds.length} target entities`);
        
        const response = await axios.get<ApiResponse<RelationBindingStatusDTO>>(
            `${MusicDataConfig.baseApiUrl}/relations/bound/${dataSource}/${sourceEntityType}/${sourceExternalEntityId}/${targetEntityType}`,
            {
                params: {
                    ids: targetExternalEntityIds.join(',')
                }
            }
        );
        
        if (response.data.success) {
            console.log(`✅ Found binding information for ${response.data.data.targetBindings.length} target entities`);
            return response.data.data;
        } else {
            console.warn(`⚠️ API returned success=false: ${response.data.message}`);
            return {
                sourceExternalId: sourceExternalEntityId,
                sourceEntityType: sourceEntityType,
                sourceEntityName: '',
                sourceInternalId: 0,
                sourceEntityBound: false,
                targetEntityType: targetEntityType,
                targetBindings: []
            };
        }
    } catch (error) {
        console.error('❌ Error finding bound external relations:', error);
        return {
            sourceExternalId: sourceExternalEntityId,
            sourceEntityType: sourceEntityType,
            sourceEntityName: '',
            sourceInternalId: 0,
            sourceEntityBound: false,
            targetEntityType: targetEntityType,
            targetBindings: []
        };
    }
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
    try {
        console.log(`🔍 Getting ${targetEntityType}s related to ${sourceEntityType} ${sourceEntityId}`);
        
        const response = await axios.get<ApiResponse<EntityDTO[]>>(
            `${MusicDataConfig.baseApiUrl}/relations/${sourceEntityType}/${sourceEntityId}/${targetEntityType}`
        );
        
        if (response.data.success) {
            console.log(`✅ Found ${response.data.data.length} related ${targetEntityType}s`);
            return response.data.data;
        } else {
            console.warn(`⚠️ API returned success=false: ${response.data.message}`);
            return [];
        }
    } catch (error) {
        console.error(`❌ Error getting related ${targetEntityType}s:`, error);
        return [];
    }
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
