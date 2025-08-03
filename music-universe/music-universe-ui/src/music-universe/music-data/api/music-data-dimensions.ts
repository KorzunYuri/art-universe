import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { Page } from '@/music-universe/shared/types/page';
import { type Dimension, DimensionImpl } from '@/music-universe/shared/types/entities.ts';

export interface DimensionDto {
    id: number;
    name: string;
}

export interface DimensionSearchParams {
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
}

export interface DimensionSaveRequest {
    id?: number;
    name: string;
}

/**
 * Creates Dimension from DTO
 */
function createDimension(dto: DimensionDto): Dimension {
    return new DimensionImpl(dto.id, dto.name);
}

/**
 * Fetches dimensions from the Music Data API
 * 
 * @param params Search parameters
 * @returns Page of Dimension objects
 */
export async function fetchDimensions(params: DimensionSearchParams): Promise<Page<Dimension>> {
    const response = await axios.get<Page<DimensionDto>>(
        `${MusicDataConfig.baseApiUrl}/dimensions/search`,
        {
            params: {
                query: params.search ?? '',
                page: params.page ?? 0,
                size: params.size ?? 20,
                sort: params.sort ?? 'name,asc',
            },
        }
    );

    return {
        ...response.data,
        content: response.data.content.map(createDimension)
    };
}

/**
 * Saves a dimension (create or update)
 * 
 * @param dimension Dimension data to save
 * @returns The saved dimension if successful, null otherwise
 */
export async function saveDimension(dimension: DimensionSaveRequest): Promise<Dimension | null> {
    console.log(`💾 Saving dimension:`, dimension.name);
    
    const response = await axios.post<DimensionDto>(
        `${MusicDataConfig.baseApiUrl}/dimensions`,
        dimension
    );
    
    return createDimension(response.data);
}

/**
 * Deletes a dimension
 *
 * @param dimensionId The dimension ID to delete
 * @returns True if successful, false otherwise
 */
export async function deleteDimension(dimensionId: number): Promise<boolean> {
    console.log(`🗑️ Deleting dimension ${dimensionId}`);

    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/dimensions/${dimensionId}`
    );

    return response.data;
}
