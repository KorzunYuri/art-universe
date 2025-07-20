import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { Page } from '@/music-universe/shared/types/page';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import { type Dimension, DimensionImpl } from '@/music-universe/music-data/types/master-entities';

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
    try {
        const response = await axios.get<ApiResponse<Page<DimensionDto>>>(
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

        const data = response.data.data;
        return {
            ...data,
            content: data.content.map(createDimension)
        };
    } catch (error) {
        console.error('❌ Error loading dimensions:', error);
        throw error;
    }
}

/**
 * Searches for dimensions in Music Data by name
 * 
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching dimensions
 */
export async function lookupDimensions(query: string, limit: number = 10): Promise<ApiResponse<LookupEntity[]>> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/dimensions/lookup`;
        const params = { name: query, limit: limit };

        const response = await axios.get<ApiResponse<LookupEntity[]>>(url, { params });
        
        return response.data;
    } catch (error) {
        console.error('❌ Error looking up dimensions:', error);
        if (axios.isAxiosError(error)) {
            console.error('❌ Axios error details:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
        }
        return {
            success: false,
            message: 'Failed to look up dimensions',
            data: []
        };
    }
}

/**
 * Saves a dimension (create or update)
 * 
 * @param dimension Dimension data to save
 * @returns The saved dimension if successful, null otherwise
 */
export async function saveDimension(dimension: DimensionSaveRequest): Promise<Dimension | null> {
    try {
        console.log(`💾 Saving dimension:`, dimension.name);
        
        const response = await axios.post<ApiResponse<DimensionDto>>(
            `${MusicDataConfig.baseApiUrl}/dimensions`,
            dimension
        );
        
        if (response.data.success && response.data.data) {
            return createDimension(response.data.data);
        }
        
        return null;
    } catch (error) {
        console.error('❌ Error saving dimension:', error);
        return null;
    }
}

/**
 * Deletes a dimension
 * 
 * @param dimensionId The dimension ID to delete
 * @returns True if successful, false otherwise
 */
export async function deleteDimension(dimensionId: number): Promise<boolean> {
    try {
        console.log(`🗑️ Deleting dimension ${dimensionId}`);
        
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/dimensions/${dimensionId}`
        );
        
        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error deleting dimension:', error);
        return false;
    }
}
