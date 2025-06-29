import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { Page } from '@/music-universe/shared/types/page';

export interface Category {
    id: number;
    name: string;
    parentId: number | null;
    parentName: string | null;
    dimensionId: number | null;
    dimensionName: string | null;
    effectiveDimensionId: number | null;
    effectiveDimensionName: string | null;
}

export interface CategorySearchParams {
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
}

/**
 * Fetches categories from the Music Data API
 * 
 * @param params Search parameters
 * @returns Page of Category objects
 */
export async function fetchCategories(params: CategorySearchParams): Promise<Page<Category>> {
    try {
        const response = await axios.get<ApiResponse<Page<Category>>>(
            `${MusicDataConfig.baseApiUrl}/categories/search`,
            {
                params: {
                    search: params.search ?? '',
                    page: params.page ?? 0,
                    size: params.size ?? 20,
                    sort: params.sort ?? 'name,asc',
                },
            }
        );

        return response.data.data;
    } catch (error) {
        console.error('❌ Error loading categories:', error);
        throw error;
    }
}
