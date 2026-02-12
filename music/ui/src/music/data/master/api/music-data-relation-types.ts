import { MusicDataConfig } from '../config/musicdataconfig';
import type { MasterEntityType } from '@/music/shared/types/entities';

const masterDataApi = MusicDataConfig.api;

export interface RelationTypeDTO {
    id: number;
    name: string;
    reverseName: string | null;
    symmetrical: boolean;
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
