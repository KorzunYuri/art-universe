import type {LastfmArtistsPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-artists.ts";
import type {LastfmAlbumsPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-albums.ts";
import type {LastfmTracksPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-tracks.ts";
import type {LastfmTagsPageSearchParams} from "@/music/data/raw/lastfm/api/lastfm-tags.ts";
import type {
    LastfmSupportedEntityType,
    LastfmSupportedEntityTypeMap
} from "@/music/data/raw/lastfm/types/lastfm-entity.ts";
import type {BasePageSearchParams, Page} from "@/music/shared/types/page.ts";
import axios from "axios";
import {LastfmConfig} from "@/music/data/raw/lastfm/config/lastfmconfig.ts";
import {
    type LastfmEntityDtoMap,
    lastfmEntityMappers,
    lastfmEntityTypeToEndpoint
} from "@/music/data/raw/lastfm/api/lastfm-common.ts";

export interface BaseLastfmPageSearchParams extends BasePageSearchParams {
    approvalStatuses?: number[];
}

export type LastfmPageSearchParamsMap = {
    artist: LastfmArtistsPageSearchParams,
    album: LastfmAlbumsPageSearchParams,
    track: LastfmTracksPageSearchParams,
    category: LastfmTagsPageSearchParams,
}

export async function fetchLastfmEntities<T extends LastfmSupportedEntityType>(
    entityType: T,
    params: LastfmPageSearchParamsMap[T]
): Promise<Page<LastfmSupportedEntityTypeMap[T]>> {

    const endpoint = lastfmEntityTypeToEndpoint[entityType];
    const response = await axios.get<Page<LastfmEntityDtoMap[T]>>(
        `${LastfmConfig.baseApiUrl}/${endpoint}`,
        {
            params: {
                ...params,
                approvalStatuses: params.approvalStatuses?.join(',')
            },
        }
    );

    return {
        ...response.data,
        content: response.data.content.map(lastfmEntityMappers[entityType])
    };
}

export async function fetchLastfmEntity<T extends LastfmSupportedEntityType>(
    entityType: T,
    id: number
): Promise<LastfmSupportedEntityTypeMap[T]> {

    const endpoint = lastfmEntityTypeToEndpoint[entityType];
    const response = await axios.get<LastfmEntityDtoMap[T]>(
        `${LastfmConfig.baseApiUrl}/${endpoint}/${id}`
    );

    return lastfmEntityMappers[entityType](response.data);
}