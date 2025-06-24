import {LastfmConfig} from "@/music-universe/sources/lastfm/config/lastfmconfig.ts"
import type {Page} from "@/music-universe/shared/types/page.ts";
import type {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";
import axios from 'axios'

export interface ArtistSearchParams {
    search?: string;
    minPlayCount?: number;
    minListenersCount?: number;
    approvalStatuses?: number[];
    page?: number;
    size?: number;
    sort?: string;
}

export async function fetchArtists(params: ArtistSearchParams): Promise<Page<LastfmArtist>> {
    const response = await axios.get(
        `${LastfmConfig.baseApiUrl}/artists`,
        {
            params: {
                search: params.search ?? '',
                minPlayCount: params.minPlayCount,
                minListenersCount: params.minListenersCount,
                approvalStatuses: params.approvalStatuses?.join(','),
                page: params.page ?? 0,
                size: params.size ?? 20,
                sort: params.sort ?? 'name,asc',
            },
        });

    return response.data.data;
}

export async function updateArtistApprovalStatus(id: number, newStatus: number): Promise<LastfmArtist> {
    const response = await axios.patch(`${LastfmConfig.baseApiUrl}/artists/${id}/approval`, {
        approvalStatus: newStatus,
    });

    return response.data.data;
}