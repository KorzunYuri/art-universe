import type {
    SpotifySupportedEntityType,
    SpotifySupportedEntityTypeMap
} from "@/music/data/raw/spotify/types/spotify-entity.ts";
import type { Page } from "@/shared/types/page.ts";
import { SpotifyConfig } from "@/music/data/raw/spotify/config/spotifyconfig.ts";
import {
    type SpotifyEntityDtoMap,
    spotifyEntityMappers,
    spotifyEntityTypeToEndpoint
} from "@/music/data/raw/spotify/api/spotify-common.ts";
import type { SpotifyArtistsPageSearchParams } from "./spotify-artists.ts";
import type { SpotifyAlbumsPageSearchParams } from "./spotify-albums.ts";
import type { SpotifyTracksPageSearchParams } from "./spotify-tracks.ts";
import type { SpotifyGenresPageSearchParams } from "./spotify-genres.ts";

const spotifyReadApi = SpotifyConfig.readApi;

export type SpotifyPageSearchParamsMap = {
    artist: SpotifyArtistsPageSearchParams;
    album: SpotifyAlbumsPageSearchParams;
    track: SpotifyTracksPageSearchParams;
    category: SpotifyGenresPageSearchParams;
}

export async function fetchSpotifyEntities<T extends SpotifySupportedEntityType>(
    entityType: T,
    params: SpotifyPageSearchParamsMap[T]
): Promise<Page<SpotifySupportedEntityTypeMap[T]>> {

    const endpoint = spotifyEntityTypeToEndpoint[entityType];
    const response = await spotifyReadApi.get<Page<SpotifyEntityDtoMap[T]>>(
        `/${endpoint}`,
        { params }
    );

    return {
        ...response.data,
        content: response.data.content.map(spotifyEntityMappers[entityType])
    };
}

export async function fetchSpotifyEntity<T extends SpotifySupportedEntityType>(
    entityType: T,
    id: number
): Promise<SpotifySupportedEntityTypeMap[T]> {

    const endpoint = spotifyEntityTypeToEndpoint[entityType];
    const response = await spotifyReadApi.get<SpotifyEntityDtoMap[T]>(
        `/${endpoint}/${id}`
    );

    return spotifyEntityMappers[entityType](response.data);
}
