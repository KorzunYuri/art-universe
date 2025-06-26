import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig";
import type { Page } from "@/music-universe/shared/types/page";
import type { LastfmTag } from "@/music-universe/sources/lastfm/types/lastfm-tag";
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

    return response.data.data;
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

    return response.data.data;
}
