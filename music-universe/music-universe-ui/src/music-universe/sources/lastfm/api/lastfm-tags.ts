import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig";
import axios from 'axios';
import {type BaseLastfmPageSearchParams, fetchEntities} from "@/music-universe/sources/lastfm/api/lastfm-common.ts";
import type { Page } from "@/music-universe/shared/types/page.ts";
import { LastfmTag } from "@/music-universe/sources/lastfm/types";
import type { Category } from "@/music-universe/music-data/types/master-entities";

export interface LastfmTagDto {
    id: number;
    name: string;
    url: string | null;
    approvalStatus: number;
    usageCount: number | null;
    usageUsersCount: number | null;
}

/**
 * Factory function to create LastfmTag from API response DTO
 * This function handles the conversion from API DTO to domain entity
 */
export function createLastfmTagFromDto(dto: LastfmTagDto, masterEntity?: Category): LastfmTag {
    return new LastfmTag(
        dto.id,
        dto.name,
        dto.url,
        dto.approvalStatus,
        dto.usageCount,
        dto.usageUsersCount,
        masterEntity
    );
}

export interface LastfmTagsPageSearchParams extends BaseLastfmPageSearchParams{
}

export async function fetchTags(params: LastfmTagsPageSearchParams) : Promise<Page<LastfmTag>> {
    return fetchEntities(
        'category',
        {
            search: params.search ?? '',
            approvalStatuses: params.approvalStatuses,
            page: params.page ?? 0,
            size: params.size ?? 20,
            sort: params.sort ?? 'name,asc',
        } as LastfmTagsPageSearchParams
    )
}

/**
 * DTO for tags associated with a specific entity
 */
export interface EntityTagDto {
    id: number;
    name: string;
    approvalStatus: number;
    tagApprovalStatus: number;
    entityApprovalStatus: number;
    usageCount: number | null;
}

/**
 * Search parameters for entity tags
 */
export interface EntityTagSearchParams {
    minUsageCount?: number;
    approvalStatuses?: number[];
}

/**
 * Fetches tags associated with a specific entity from the LastFM API
 *
 * @param entityType Type of entity (ARTIST, ALBUM, TRACK)
 * @param entityId ID of the entity
 * @param params Search parameters
 * @returns List of EntityTagDto objects
 */
export async function fetchEntityTags(
    entityType: string,
    entityId: number,
    params?: EntityTagSearchParams
): Promise<EntityTagDto[]> {
    const response = await axios.get<EntityTagDto[]>(
        `${LastfmConfig.baseApiUrl}/tags/entity/${entityType}/${entityId}`,
        {
            params: {
                minUsageCount: params?.minUsageCount,
                approvalStatuses: params?.approvalStatuses?.join(','),
            },
        }
    );

    return response.data;
}
