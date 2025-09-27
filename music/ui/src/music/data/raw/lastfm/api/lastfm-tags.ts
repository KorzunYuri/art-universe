import { LastfmConfig } from "@/music/data/raw/lastfm/config/lastfmconfig.ts";
import axios from 'axios';
import { LastfmTag } from "@/music/data/raw/lastfm/types";
import type { Category } from "@/music/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import type {BaseLastfmPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-common-fetching.ts";

export interface LastfmTagDto {
    id: number;
    name: string;
    url: string | null;
    approvalStatus: ApprovalStatusType;
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
    minUsageCount?: number;
    minUsageUsersCount?: number;
}

/**
 * DTO for tags associated with a specific entity
 */
export interface EntityTagDto {
    id: number;
    name: string;
    tagApprovalStatus: ApprovalStatusType;
    entityApprovalStatus: ApprovalStatusType;
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
