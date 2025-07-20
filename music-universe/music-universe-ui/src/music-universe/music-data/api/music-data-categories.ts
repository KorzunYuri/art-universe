import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { Page } from '@/music-universe/shared/types/page';
import type { MasterEntity } from '@/music-universe/shared/types/entity-reference';
import type { BoundEntityResponse } from '@/music-universe/shared/types/master';
import type { LookupEntity, BatchLookupRequestDTO, BatchLookupResponseDTO } from '@/music-universe/shared/types/lookup';
import { type Category, CategoryImpl } from '@/music-universe/music-data/types/master-entities';
import { createMasterEntity } from '@/music-universe/shared/utils/entity-helpers';

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

export interface CategoryBindToExistingRequest {
    categoryId: number;
}

export interface CategoryCreateAndBindRequest {
    name: string;
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
    try {
        const response = await axios.get<ApiResponse<Page<CategoryDto>>>(
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

        const data = response.data.data;
        return {
            ...data,
            content: data.content.map(createCategory)
        };
    } catch (error) {
        console.error('❌ Error loading categories:', error);
        throw error;
    }
}

/**
 * Searches for categories in Music Data by name
 * 
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching categories
 */
export async function lookupCategories(query: string, limit: number = 10): Promise<ApiResponse<LookupEntity[]>> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/categories/lookup`;
        const params = { name: query, limit: limit };

        const response = await axios.get<ApiResponse<LookupEntity[]>>(url, { params });
        
        return response.data;
    } catch (error) {
        console.error('❌ Error looking up categories:', error);
        if (axios.isAxiosError(error)) {
            console.error('❌ Axios error details:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
        }
        return {
            success: false,
            message: 'Failed to look up categories',
            data: []
        };
    }
}

/**
 * Performs batch lookup of categories by multiple names
 * 
 * @param names Array of category names to look up
 * @param limit Maximum number of results for each name (default: 10)
 * @returns Object with lookup results grouped by category names
 */
export async function batchLookupCategories(names: string[], limit: number = 10): Promise<ApiResponse<BatchLookupResponseDTO>> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/categories/lookup/batch`;
        const request: BatchLookupRequestDTO = { 
            names, 
            limit 
        };

        const response = await axios.post<ApiResponse<BatchLookupResponseDTO>>(url, request);
        
        return response.data;
    } catch (error) {
        console.error('❌ Error batch looking up categories:', error);
        if (axios.isAxiosError(error)) {
            console.error('❌ Axios error details:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
        }
        return {
            success: false,
            message: 'Failed to batch lookup categories',
            data: { results: {} }
        };
    }
}

/**
 * Fetches bound categories from the music-data API
 * 
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param externalIds List of external IDs to check
 * @returns List of bound categories
 */
export async function fetchBoundCategories(dataSource: string, externalIds: number[]): Promise<BoundEntityResponse[]> {
    try {
        const url = `${MusicDataConfig.baseApiUrl}/categories/bound/${dataSource}`;
        const response = await axios.get<ApiResponse<BoundEntityResponse[]>>(
            url,
            {
                params: {
                    externalIds: externalIds.join(','),
                },
            }
        );

        if (response.data.success) {
            return response.data.data;
        } else {
            console.warn(`⚠️ API returned success=false: ${response.data.message}`);
            return [];
        }
    } catch (error) {
        console.error('❌ Error fetching bound categories:', error);
        return [];
    }
}

/**
 * Binds a category to an existing category in music-data
 * 
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param externalId The external category ID
 * @param categoryId The existing category ID in music-data
 * @returns The bound category if successful, null otherwise
 */
export async function bindCategoryToExisting(dataSource: string, externalId: number, categoryId: number): Promise<MasterEntity | null> {
    try {
        const request: CategoryBindToExistingRequest = { categoryId };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/categories/bind/existing/${dataSource}/${externalId}`,
            request
        );
        
        if (response.data.success && response.data.data) {
            const result = createMasterEntity(
                response.data.data.masterId,
                response.data.data.masterName
            );
            return result;
        }
        
        return null;
    } catch (error) {
        console.error('❌ Error binding category to existing:', error);
        return null;
    }
}

/**
 * Creates a new category and binds it to external category
 * 
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param externalId The external category ID
 * @param categoryName The name of the new category
 * @returns The bound category if successful, null otherwise
 */
export async function createAndBindCategory(dataSource: string, externalId: number, categoryName: string): Promise<MasterEntity | null> {
    try {
        const request: CategoryCreateAndBindRequest = { name: categoryName };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/categories/bind/new/${dataSource}/${externalId}`,
            request
        );
        
        if (response.data.success && response.data.data) {
            return createMasterEntity(
                response.data.data.masterId,
                response.data.data.masterName
            );
        }
        
        return null;
    } catch (error) {
        console.error('❌ Error creating and binding category:', error);
        return null;
    }
}

/**
 * Unbinds a category from external source
 *
 * @param dataSource Data source (e.g., 'LASTFM')
 * @param externalId The external category ID
 * @returns True if successful, false otherwise
 */
export async function unbindCategory(dataSource: string, externalId: number): Promise<boolean> {
    try {
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/categories/unbind/${dataSource}/${externalId}`
        );

        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('Error unbinding category:', error);
        return false;
    }
}

/**
 * Saves a category (create or update)
 * 
 * @param category Category data to save
 * @returns The saved category if successful, null otherwise
 */
export async function saveCategory(category: CategorySaveRequest): Promise<Category | null> {
    try {
        const response = await axios.post<ApiResponse<CategoryDto>>(
            `${MusicDataConfig.baseApiUrl}/categories`,
            category
        );
        
        if (response.data.success && response.data.data) {
            return createCategory(response.data.data);
        }
        
        return null;
    } catch (error) {
        console.error('❌ Error saving category:', error);
        return null;
    }
}

/**
 * Deletes a category
 * 
 * @param categoryId The category ID to delete
 * @returns True if successful, false otherwise
 */
export async function deleteCategory(categoryId: number): Promise<boolean> {
    try {
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/categories/${categoryId}`
        );
        
        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error deleting category:', error);
        return false;
    }
}
