import axios from 'axios';
import { MusicDataConfig } from '../config/musicdataconfig';
import type {BasePageSearchParams} from '@/music-universe/shared/types/page';
import { type Dimension, DimensionImpl } from '@/music-universe/shared/types/entities.ts';
import type {LookupEntity} from "@/music-universe/shared/types/lookup.ts";
import {type BaseMasterEntityDto, entityToEndpoint} from "@/music-universe/music-data/api/music-data-commons.ts";

export interface DimensionDto extends BaseMasterEntityDto {}

export interface DimensionPageSearchParams extends BasePageSearchParams{}

export interface DimensionSaveRequest {
    id?: number;
    name: string;
}

/**
 * Creates Dimension from DTO
 */
export function createDimensionFromDto(dto: DimensionDto): Dimension {
    return new DimensionImpl(dto.id, dto.name);
}

/**
 * Performs lookup of dimensions, optionally for provided search string
 *
 * @returns List of matching categories
 * @param search search string
 */
export async function lookupDimensions(
    search: string = ""
): Promise<LookupEntity[]> {
    const endpoint = entityToEndpoint['dimension'];
    const url = `${MusicDataConfig.baseApiUrl}/${endpoint}/lookup`;
    const params = search ? { search: search} : {};

    const response = await axios.get<LookupEntity[]>(url, { params });
    return response.data;
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
    
    return createDimensionFromDto(response.data);
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
