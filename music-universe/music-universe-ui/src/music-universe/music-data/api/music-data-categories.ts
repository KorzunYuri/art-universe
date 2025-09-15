import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type {BasePageSearchParams} from '@/music-universe/shared/types/page';
import {type Category, CategoryImpl} from '@/music-universe/shared/types/entities.ts';
import type {BaseMasterEntityDto} from "@/music-universe/music-data/api/music-data-commons.ts";

export interface CategoryDto extends BaseMasterEntityDto {}

export interface CategoryWithParentsDto {
    id: number;
    name: string;
    parents: CategoryDto[];
}

export interface CategoryPageSearchParams extends BasePageSearchParams {}

export interface CategorySaveRequest {
    id?: number;
    name: string;
}

export interface CategoryCreateRequest {
    name: string;
}

export interface CategorySaveWithParentsRequest {
    id?: number;
    name: string;
    parents?: number[];
}

export interface CategoryRelation {
    sourceId: number;
    targetId: number;
}

/**
 * Creates Category from DTO (legacy support)
 */
export function createCategoryFromDto(dto: CategoryDto): Category {
    return new CategoryImpl(
        dto.id,
        dto.name
    );
}

/**
 * Creates Category from CategoryWithParentsDto
 */
export function createCategoryFromWithParentsDto(dto: CategoryWithParentsDto): Category {
    const category = new CategoryImpl(dto.id, dto.name);
    category.parents = dto.parents;
    return category;
}

/**
 * Creates a new category
 * 
 * @param category Category data to create
 * @returns The created category if successful, null otherwise
 */
export async function createCategory(category: CategoryCreateRequest): Promise<Category | null> {
    const response = await axios.post<CategoryDto>(
        `${MusicDataConfig.baseApiUrl}/categories`,
        category
    );
    
    return createCategoryFromDto(response.data);
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
    
    return createCategoryFromDto(response.data);
}

/**
 * Saves a category with parents
 * 
 * @param category Category data with parents to save
 * @returns The saved category with parents if successful, null otherwise
 */
export async function saveCategoryWithParents(category: CategorySaveWithParentsRequest): Promise<CategoryWithParentsDto | null> {
    const response = await axios.post<CategoryWithParentsDto>(
        `${MusicDataConfig.baseApiUrl}/categories/with-parents`,
        category
    );
    
    return response.data;
}

/**
 * Fetches a single category with parents by ID
 * 
 * @param categoryId The category ID to fetch
 * @returns The category with its parents if successful, null otherwise
 */
export async function fetchCategoryWithParents(categoryId: number): Promise<CategoryWithParentsDto | null> {
    const response = await axios.get<CategoryWithParentsDto>(
        `${MusicDataConfig.baseApiUrl}/categories/${categoryId}/with-parents`
    );
    
    return response.data;
}

/**
 * Fetches categories with parents
 * 
 * @param query Optional search query
 * @returns List of categories with their parents
 */
export async function fetchCategoriesWithParents(query?: string): Promise<CategoryWithParentsDto[]> {
    const response = await axios.get<CategoryWithParentsDto[]>(
        `${MusicDataConfig.baseApiUrl}/categories/with-parents`,
        { params: query ? { query } : {} }
    );
    
    return response.data;
}

/**
 * Creates a category relation
 * 
 * @param relation Relation to create
 */
export async function createCategoryRelation(relation: CategoryRelation): Promise<void> {
    await axios.post(
        `${MusicDataConfig.baseApiUrl}/categories/relations`,
        relation
    );
}

/**
 * Deletes a category relation
 * 
 * @param relation Relation to delete
 */
export async function deleteCategoryRelation(relation: CategoryRelation): Promise<void> {
    await axios.delete(
        `${MusicDataConfig.baseApiUrl}/categories/relations`,
        { data: relation }
    );
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

export interface CategoryDagNodeDto {
    id: number;
    name: string;
    isRoot: boolean;
    childrenCount: number;
    artistsCount: number;
    tracksCount: number;
}

export interface CategoryDagEdgeDto {
    source: number;
    target: number;
}

export interface CategoryDagDto {
    nodes: CategoryDagNodeDto[];
    edges: CategoryDagEdgeDto[];
}

/**
 * Fetches category DAG data
 * 
 * @returns Category DAG with nodes and edges
 */
export async function fetchCategoryDag(): Promise<CategoryDagDto> {
    const response = await axios.get<CategoryDagDto>(
        `${MusicDataConfig.baseApiUrl}/categories/dag`
    );
    
    return response.data;
}
