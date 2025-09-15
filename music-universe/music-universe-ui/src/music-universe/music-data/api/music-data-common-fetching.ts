import axios from "axios";
import {MusicDataConfig} from "@/music-universe/music-data/config/musicdataconfig.ts";
import type {Page} from "@/music-universe/shared/types/page.ts";
import type {MasterEntityMap, MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {
    type MasterEntityDtoMap,
    entityToEndpoint,
    masterEntityFromDtoMappers
} from "@/music-universe/music-data/api/music-data-commons.ts";
import type {ArtistPageSearchParams} from "@/music-universe/music-data/api/music-data-artists.ts";
import {createArtistWithCategoriesFromDto} from "@/music-universe/music-data/api/music-data-artists.ts";
import type {AlbumPageSearchParams} from "@/music-universe/music-data/api/music-data-albums.ts";
import type {TrackPageSearchParams} from "@/music-universe/music-data/api/music-data-tracks.ts";
import type {CategoryPageSearchParams} from "@/music-universe/music-data/api/music-data-categories.ts";
import {createCategoryFromWithParentsDto} from "@/music-universe/music-data/api/music-data-categories.ts";
import type {ArtistWithCategoriesDto} from "@/music-universe/music-data/api/music-data-artists.ts";
import type {CategoryWithParentsDto} from "@/music-universe/music-data/api/music-data-categories.ts";


export type MasterEntityPageSearchParamsMap = {
    artist:     ArtistPageSearchParams,
    album:      AlbumPageSearchParams,
    track:      TrackPageSearchParams,
    category:   CategoryPageSearchParams
}

export async function fetchMasterEntities<T extends MasterEntityType>(
    entityType: T,
    params: MasterEntityPageSearchParamsMap[T]
): Promise<Page<MasterEntityMap[T]>> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.get<Page<MasterEntityDtoMap[T]>>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}`,
        {
            params
        }
    );

    return {
        ...response.data,
        content: response.data.content.map(masterEntityFromDtoMappers[entityType])
    };
}

export async function fetchMasterEntity<T extends MasterEntityType>(
    entityType: T,
    entityId: number
): Promise<MasterEntityMap[T]> {
    const endpoint = entityToEndpoint[entityType];
    const response = await axios.get<MasterEntityDtoMap[T]>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}/${entityId}`
    );

    return masterEntityFromDtoMappers[entityType](response.data);
}

export async function fetchMasterEntitiesWithRelations<T extends MasterEntityType>(
    entityType: T,
    params: MasterEntityPageSearchParamsMap[T]
): Promise<Page<MasterEntityMap[T]>> {
    const endpoint =
        entityType === 'category'   ?   'categories/with-parents' :
        entityType === 'artist'     ?   'artists/with-categories' :
                                        entityToEndpoint[entityType];
    
    const response = await axios.get<Page<any>>(
        `${MusicDataConfig.baseApiUrl}/${endpoint}`,
        { params }
    );

    // For entities with relations, we need special handling
    if (entityType === 'category') {
        return {
            ...response.data,
            content: response.data.content.map((dto: CategoryWithParentsDto) => 
                createCategoryFromWithParentsDto(dto)
            )
        } as Page<MasterEntityMap[T]>;
    }
    
    if (entityType === 'artist') {
        return {
            ...response.data,
            content: response.data.content.map((dto: ArtistWithCategoriesDto) => 
                createArtistWithCategoriesFromDto(dto)
            )
        } as Page<MasterEntityMap[T]>;
    }

    return {
        ...response.data,
        content: response.data.content.map(masterEntityFromDtoMappers[entityType])
    };
}