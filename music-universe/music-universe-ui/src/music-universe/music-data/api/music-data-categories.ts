import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { ApiResponse } from '@/music-universe/shared/types/api-response';
import type { Page } from '@/music-universe/shared/types/page';
import type { BoundEntity, BoundEntityResponse } from '@/music-universe/shared/types/bindable';
import type { LookupEntity } from '@/music-universe/shared/components/AutocompleteInput';

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
            console.log(`🎯 Found ${response.data.data.length} bound categories`);
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
export async function bindCategoryToExisting(dataSource: string, externalId: number, categoryId: number): Promise<BoundEntity | null> {
    try {
        console.log(`🔗 Binding category ${externalId} to existing category ${categoryId}`);
        const request: CategoryBindToExistingRequest = { categoryId };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/categories/bind/existing/${dataSource}/${externalId}`,
            request
        );
        
        if (response.data.success && response.data.data) {
            return {
                referenceId: response.data.data.referenceId,
                referenceName: response.data.data.referenceName
            };
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
export async function createAndBindCategory(dataSource: string, externalId: number, categoryName: string): Promise<BoundEntity | null> {
    try {
        console.log(`🔗 Creating and binding new category ${externalId} with name "${categoryName}"`);
        const request: CategoryCreateAndBindRequest = { name: categoryName };
        
        const response = await axios.post<ApiResponse<BoundEntityResponse>>(
            `${MusicDataConfig.baseApiUrl}/categories/bind/new/${dataSource}/${externalId}`,
            request
        );
        
        if (response.data.success && response.data.data) {
            return {
                referenceId: response.data.data.referenceId,
                referenceName: response.data.data.referenceName
            };
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
        console.log(`🔓 Unbinding category ${externalId} from ${dataSource}`);

        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/categories/unbind/${dataSource}/${externalId}`
        );

        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error unbinding category:', error);
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
        console.log(`💾 Saving category:`, category.name);
        
        const response = await axios.post<ApiResponse<Category>>(
            `${MusicDataConfig.baseApiUrl}/categories`,
            category
        );
        
        if (response.data.success && response.data.data) {
            return response.data.data;
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
        console.log(`🗑️ Deleting category ${categoryId}`);
        
        const response = await axios.delete<ApiResponse<boolean>>(
            `${MusicDataConfig.baseApiUrl}/categories/${categoryId}`
        );
        
        return response.data.success ? response.data.data : false;
    } catch (error) {
        console.error('❌ Error deleting category:', error);
        return false;
    }
}
