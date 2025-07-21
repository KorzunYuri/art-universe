import {LastfmConfig} from "@/music-universe/sources/lastfm/config/lastfmconfig.ts"
import type {Page} from "@/music-universe/shared/types/page.ts";
import {createLastfmArtist, LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";
import axios from 'axios'

export interface LastfmArtistSearchParams {
    search?: string;
    minPlayCount?: number;
    minListenersCount?: number;
    approvalStatuses?: number[];
    page?: number;
    size?: number;
    sort?: string;
}

export interface LastfmArtistResponseDto {
    id: number;
    name: string;
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
}

export async function fetchArtists(params: LastfmArtistSearchParams): Promise<Page<LastfmArtist>> {
    const response = await axios.get<Page<LastfmArtistResponseDto>>(
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

    // Convert plain objects to LastfmArtist instances
    const page = response.data;
    return {
        ...page,
        content: page.content.map((artistDto) => createLastfmArtist(artistDto))
    };
}

export async function fetchArtist(id: number): Promise<LastfmArtist> {
    const response = await axios.get<LastfmArtistResponseDto>(`${LastfmConfig.baseApiUrl}/artists/${id}`);

    // Convert plain object to LastfmArtist instance
    return createLastfmArtist(response.data);
}


export async function updateArtistApprovalStatus(id: number, newStatus: number): Promise<boolean> {
    const response = await axios.patch<boolean>(`${LastfmConfig.baseApiUrl}/artists/${id}/approval`, {
        approvalStatus: newStatus,
    });

    return response.data;
}
