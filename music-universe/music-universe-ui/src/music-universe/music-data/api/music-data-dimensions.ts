import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { Page } from '@/music-universe/shared/types/page';
import type { LookupEntity } from '@/music-universe/shared/components/AutocompleteInput';

export interface Dimension {
    id: number;
    name: string;
}

export interface DimensionSearchParams {
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
}

/**
 * Fetches dimensions from the Music Data API
 * 
 * @param params Search parameters
 * @returns Page of Dimension objects
 */
export async function fetchDimensions(params: DimensionSearchParams): Promise<Page<Dimension>> {
    try {
        const response = await axios.get<ApiResponse<Page<Dimension>>>(
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

        return response.data.data;
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
