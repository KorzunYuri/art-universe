import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type { Page } from '@/music-universe/shared/types/page';
import type { MasterEntity } from '@/music-universe/shared/types/entities.ts';
import {
    type BoundEntityResponse,
    createMasterEntityFromBinding,
} from '@/music-universe/music-data/utils/master-entities-common.ts';
import type { LookupEntity, BatchLookupRequestDTO, BatchLookupResponseDTO } from '@/music-universe/shared/types/lookup';
import {type Category, CategoryImpl, type MasterEntityType} from '@/music-universe/music-data/types/master-entities';
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";

const entityType: MasterEntityType = 'category'

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
 * Searches for categories in Music Data by name
 * 
 * @param query Search query
 * @param limit Maximum number of results (default: 10)
 * @returns List of matching categories
 */
export async function lookupCategories(query: string, limit: number = 10): Promise<LookupEntity[]> {
    const url = `${MusicDataConfig.baseApiUrl}/categories/lookup`;
    const params = { name: query, limit: limit };

    const response = await axios.get<LookupEntity[]>(url, { params });
    return response.data;
}

/**
 * Performs batch lookup of categories by multiple names
 * 
 * @param names Array of category names to look up
 * @param limit Maximum number of results for each name (default: 10)
 * @returns Object with lookup results grouped by category names
 */
export async function batchLookupCategories(names: string[], limit: number = 10): Promise<BatchLookupResponseDTO> {
    const url = `${MusicDataConfig.baseApiUrl}/categories/lookup/batch`;
    const request: BatchLookupRequestDTO = { 
        names, 
        limit 
    };

    const response = await axios.post<BatchLookupResponseDTO>(url, request);
    return response.data;
}

/**
 * Fetches bound categories from the music-data API
 * 
 * @param dataSource {DataSource}
 * @param externalIds List of external IDs to check
 * @returns List of bound categories
 */
export async function fetchBoundCategories(dataSource: DataSource, externalIds: number[]): Promise<BoundEntityResponse[]> {
    const url = `${MusicDataConfig.baseApiUrl}/categories/bound/${dataSource}`;
    const response = await axios.get<BoundEntityResponse[]>(
        url,
        {
            params: {
                externalIds: externalIds.join(','),
            },
        }
    );

    return response.data;
}

/**
 * Binds a category to an existing category in music-data
 * 
 * @param dataSource {DataSource}
 * @param externalId The external category ID
 * @param categoryId The existing category ID in music-data
 * @returns The bound category if successful, null otherwise
 */
export async function bindCategoryToExisting(dataSource: DataSource, externalId: number, categoryId: number): Promise<MasterEntity | null> {
    const request: CategoryBindToExistingRequest = { categoryId };
    
    const response = await axios.post<BoundEntityResponse>(
        `${MusicDataConfig.baseApiUrl}/categories/bind/existing/${dataSource}/${externalId}`,
        request
    );
    
    return createMasterEntityFromBinding(response.data, entityType);
}

/**
 * Creates a new category and binds it to external category
 * 
 * @param dataSource {DataSource}
 * @param externalId The external category ID
 * @param categoryName The name of the new category
 * @returns The bound category if successful, null otherwise
 */
export async function createAndBindCategory(dataSource: DataSource, externalId: number, categoryName: string): Promise<MasterEntity | null> {
    const request: CategoryCreateAndBindRequest = { name: categoryName };
    
    const response = await axios.post<BoundEntityResponse>(
        `${MusicDataConfig.baseApiUrl}/categories/bind/new/${dataSource}/${externalId}`,
        request
    );

    return createMasterEntityFromBinding(response.data, entityType);
}

/**
 * Unbinds a category from external source
 *
 * @param dataSource {DataSource}
 * @param externalId The external category ID
 * @returns True if successful, false otherwise
 */
export async function unbindCategory(dataSource: DataSource, externalId: number): Promise<boolean> {
    const response = await axios.delete<boolean>(
        `${MusicDataConfig.baseApiUrl}/categories/unbind/${dataSource}/${externalId}`
    );

    return response.data;
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
