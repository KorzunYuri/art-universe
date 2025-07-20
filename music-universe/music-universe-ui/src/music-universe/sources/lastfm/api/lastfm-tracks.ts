import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig";
import type { Page } from "@/music-universe/shared/types/page";
import { LastfmTrack, createLastfmTrack, type LastfmTrackDto } from "@/music-universe/sources/lastfm/types/lastfm-track";
import axios from 'axios';

export interface TrackSearchParams {
    search?: string;
    minPlayCount?: number;
    minListenersCount?: number;
    artistId?: number;
    approvalStatuses?: number[];
    page?: number;
    size?: number;
    sort?: string;
}

/**
 * Fetches tracks from the LastFM API
 * 
 * @param params Search parameters
 * @returns Page of LastfmTrack objects
 */
export async function fetchTracks(params: TrackSearchParams): Promise<Page<LastfmTrack>> {
    const response = await axios.get(
        `${LastfmConfig.baseApiUrl}/tracks`,
        {
            params: {
                search: params.search ?? '',
                minPlayCount: params.minPlayCount,
                minListenersCount: params.minListenersCount,
                artistId: params.artistId,
                approvalStatuses: params.approvalStatuses?.join(','),
                page: params.page ?? 0,
                size: params.size ?? 20,
                sort: params.sort ?? 'name,asc',
            },
        }
    );

    // Convert plain objects to LastfmTrack instances
    const data = response.data.data;
    return {
        ...data,
        content: data.content.map((trackDto: LastfmTrackDto) => createLastfmTrack(trackDto))
    };
}

/**
 * Updates the approval status of a track
 * 
 * @param id Track ID
 * @param newStatus New approval status
 * @returns Updated track
 */
export async function updateTrackApprovalStatus(id: number, newStatus: number): Promise<LastfmTrack> {
    const response = await axios.patch(`${LastfmConfig.baseApiUrl}/tracks/${id}/approval`, {
        approvalStatus: newStatus,
    });

    // Convert plain object to LastfmTrack instance
    return createLastfmTrack(response.data.data);
}
