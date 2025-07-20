import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig";
import type { Page } from "@/music-universe/shared/types/page";
import { LastfmTag, createLastfmTag, type LastfmTagDto } from "@/music-universe/sources/lastfm/types/lastfm-tag";
import axios from 'axios';

export interface TagSearchParams {
    search?: string;
    approvalStatuses?: number[];
    page?: number;
    size?: number;
    sort?: string;
}

/**
 * Fetches tags from the LastFM API
 * 
 * @param params Search parameters
 * @returns Page of LastfmTag objects
 */
export async function fetchTags(params: TagSearchParams): Promise<Page<LastfmTag>> {
    const response = await axios.get(
        `${LastfmConfig.baseApiUrl}/tags`,
        {
            params: {
                search: params.search ?? '',
                approvalStatuses: params.approvalStatuses?.join(','),
                page: params.page ?? 0,
                size: params.size ?? 20,
                sort: params.sort ?? 'name,asc',
            },
        }
    );

    // Convert plain objects to LastfmTag instances
    const data = response.data.data;
    return {
        ...data,
        content: data.content.map((tagDto: LastfmTagDto) => createLastfmTag(tagDto))
    };
}

/**
 * Updates the approval status of a tag
 * 
 * @param id Tag ID
 * @param newStatus New approval status
 * @returns Updated tag
 */
export async function updateTagApprovalStatus(id: number, newStatus: number): Promise<LastfmTag> {
    const response = await axios.patch(`${LastfmConfig.baseApiUrl}/tags/${id}/approval`, {
        approvalStatus: newStatus,
    });

    // Convert plain object to LastfmTag instance
    return createLastfmTag(response.data.data);
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
    try {
        const response = await axios.get(
            `${LastfmConfig.baseApiUrl}/tags/entity/${entityType}/${entityId}`,
            {
                params: {
                    minUsageCount: params?.minUsageCount,
                    approvalStatuses: params?.approvalStatuses?.join(','),
                },
            }
        );

        if (response.data.success) {
            console.log(`✅ Fetched ${response.data.data.length} tags for ${entityType} ${entityId}`);
            return response.data.data;
        } else {
            console.warn(`⚠️ API returned success=false: ${response.data.message}`);
            return [];
        }
    } catch (error) {
        console.error(`❌ Error fetching tags for ${entityType} ${entityId}:`, error);
        return [];
    }
}
