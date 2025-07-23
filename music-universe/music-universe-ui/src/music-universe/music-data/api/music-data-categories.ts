import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { Page } from '@/music-universe/shared/types/page';
import {type Category, CategoryImpl} from '@/music-universe/music-data/types/master-entities';

export interface CategoryDto {
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

export interface CategorySaveRequest {
    id?: number;
    name: string;
    dimensionId?: number | null;
    parentId?: number | null;
}

/**
 * Creates Category from DTO
 */
function createCategory(dto: CategoryDto): Category {
    return new CategoryImpl(
        dto.id,
        dto.name,
        dto.parentId,
        dto.parentName,
        dto.dimensionId,
        dto.dimensionName,
        dto.effectiveDimensionId,
        dto.effectiveDimensionName
    );
}

/**
 * Fetches categories from the Music Data API
 * 
 * @param params Search parameters
 * @returns Page of Category objects
 */
export async function fetchCategories(params: CategorySearchParams): Promise<Page<Category>> {
    const response = await axios.get<Page<CategoryDto>>(
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

    return {
        ...response.data,
        content: response.data.content.map(createCategory)
    };
}


/**
 * Saves a category (create or update)
 * 
 * @param category Category data to save
 * @returns The saved category if successful, null otherwise
 */
export async function saveCategory(category: CategorySaveRequest): Promise<Category | null> {
    const response = await axios.post<CategoryDto>(
        `${MusicDataConfig.baseApiUrl}/categories`,
        category
    );
    
    return createCategory(response.data);
}

/**
 * Deletes a category
 * 
 * @param categoryId The category ID to delete
 * @returns True if successful, false otherwise
 */
export async function deleteCategory(categoryId: number): Promise<boolean> {
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/categories/${categoryId}`
    );
    
    return response.data;
}
