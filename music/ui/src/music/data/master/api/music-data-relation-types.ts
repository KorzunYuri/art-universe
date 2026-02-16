import { MusicDataConfig } from '../config/musicdataconfig';
import type { MasterEntityType } from '@/music/shared/types/entities';

const masterDataApi = MusicDataConfig.api;

export interface RelationTypeDTO {
    id: number;
    name: string;
    reverseName: string | null;
    isSymmetrical: boolean;
    isSystem: boolean;
}

export interface RelationTypeApplicabilityDTO {
    id: number;
    relationTypeId: number;
    sourceEntityType: MasterEntityType;
    targetEntityType: MasterEntityType;
    isDefault: boolean;
}

export interface RelationTypeWithApplicabilitiesDTO extends RelationTypeDTO {
    applicabilities: RelationTypeApplicabilityDTO[];
}

/**
 * Fetches relation types applicable to a specific source→target entity type pair
 */
export async function fetchApplicableRelationTypes(
    sourceEntityType: MasterEntityType,
    targetEntityType: MasterEntityType
): Promise<RelationTypeDTO[]> {
    const response = await masterDataApi.get<RelationTypeDTO[]>(
        `/relation-types/applicable/${sourceEntityType}/${targetEntityType}`
    );
    return response.data;
}

/**
 * Fetches all relation types, optionally filtered by search string
 */
export async function fetchRelationTypes(search?: string): Promise<RelationTypeDTO[]> {
    const response = await masterDataApi.get<RelationTypeDTO[]>(
        '/relation-types',
        { params: search ? { search } : undefined }
    );
    return response.data;
}

/**
 * Fetches a single relation type with its applicabilities
 */
export async function fetchRelationTypeWithApplicabilities(id: number): Promise<RelationTypeWithApplicabilitiesDTO> {
    const response = await masterDataApi.get<RelationTypeWithApplicabilitiesDTO>(`/relation-types/${id}`);
    return response.data;
}

export async function createRelationType(
    name: string,
    reverseName: string | null,
    isSymmetrical: boolean
): Promise<RelationTypeDTO> {
    const response = await masterDataApi.post<RelationTypeDTO>('/relation-types', { name, reverseName, isSymmetrical });
    return response.data;
}

export async function updateRelationType(
    id: number,
    name: string,
    reverseName: string | null,
    isSymmetrical: boolean
): Promise<RelationTypeDTO> {
    const response = await masterDataApi.put<RelationTypeDTO>(`/relation-types/${id}`, { name, reverseName, isSymmetrical });
    return response.data;
}

export async function deleteRelationType(id: number): Promise<void> {
    await masterDataApi.delete(`/relation-types/${id}`);
}

export async function addApplicability(
    id: number,
    sourceEntityType: MasterEntityType,
    targetEntityType: MasterEntityType,
    isDefault: boolean
): Promise<RelationTypeApplicabilityDTO> {
    const response = await masterDataApi.post<RelationTypeApplicabilityDTO>(
        `/relation-types/${id}/applicability`,
        { sourceEntityType, targetEntityType, isDefault }
    );
    return response.data;
}

export async function removeApplicability(
    id: number,
    sourceEntityType: MasterEntityType,
    targetEntityType: MasterEntityType
): Promise<void> {
    await masterDataApi.delete(`/relation-types/${id}/applicability/${sourceEntityType}/${targetEntityType}`);
}
