import { MusicDataConfig } from '../config/musicdataconfig.ts';
import type { BaseMasterEntityDto } from "@/music/data/master/api/music-data-commons.ts";
import type { CategoryDto } from "@/music/data/master/api/music-data-categories.ts";
import { TrackImpl } from "@/music/shared/types/entities.ts";
import type { BasePageSearchParams } from "@/shared/types/page.ts";

const masterDataApi = MusicDataConfig.api;

export interface TrackDto extends BaseMasterEntityDto {
    primaryArtistId: number;
}

export interface TrackWithCategoriesDto {
    id: number;
    name: string;
    primaryArtistId: number;
    categories: CategoryDto[];
}

export interface TrackPageSearchParams extends BasePageSearchParams {}

export interface TrackSaveRequest {
    id?: number;
    name: string;
    primaryArtistId?: number;
}

export interface TrackCreateRequest {
    name: string;
    primaryArtistId: number;
}

export function createTrackFromDto(dto: TrackDto) {
    return new TrackImpl(
        dto.id,
        dto.name,
        dto.primaryArtistId
    );
}

export function createTrackWithCategoriesFromDto(dto: TrackWithCategoriesDto) {
    const track = new TrackImpl(dto.id, dto.name, dto.primaryArtistId);
    track.categories = dto.categories;
    return track;
}

/**
 * Creates a new track
 */
export async function createTrack(track: TrackCreateRequest) {
    const response = await masterDataApi.post<TrackDto>(
        `/tracks`,
        track
    );

    return createTrackFromDto(response.data);
}

/**
 * Saves an existing track (PATCH-like: only sends changed fields)
 */
export async function saveTrack(track: TrackSaveRequest) {
    const response = await masterDataApi.post<TrackDto>(
        `/tracks`,
        track
    );

    return createTrackFromDto(response.data);
}

/**
 * Deletes a track
 */
export async function deleteTrack(trackId: number): Promise<boolean> {
    const response = await masterDataApi.delete<boolean>(
        `/tracks/${trackId}`
    );

    return response.data;
}

/**
 * Fetches a single track with categories by ID
 */
export async function fetchTrackWithCategories(trackId: number): Promise<TrackWithCategoriesDto | null> {
    const response = await masterDataApi.get<TrackWithCategoriesDto>(
        `/tracks/${trackId}/with-categories`
    );

    return response.data;
}

/**
 * Binds track to category
 */
export async function bindTrackToCategory(trackId: number, categoryId: number): Promise<void> {
    await masterDataApi.post(
        `/tracks/${trackId}/categories/${categoryId}`
    );
}

/**
 * Unbinds track from category
 */
export async function unbindTrackFromCategory(trackId: number, categoryId: number): Promise<void> {
    await masterDataApi.delete(
        `/tracks/${trackId}/categories/${categoryId}`
    );
}
