import {type MasterEntityMap, type MasterEntityType} from "@/music/shared/types/entities.ts";
import {type ArtistDto, createArtistFromDto} from "@/music/data/master/api/music-data-artists.ts";
import {type CategoryDto, createCategoryFromDto} from "@/music/data/master/api/music-data-categories.ts";
import {type AlbumDto, createAlbumFromDto} from "@/music/data/master/api/music-data-albums.ts";
import {createTrackFromDto, type TrackDto} from "@/music/data/master/api/music-data-tracks.ts";

export const entityToEndpoint: Record<MasterEntityType, string> = {
    'artist':       'artists',
    'album':        'albums',
    'track':        'tracks',
    'category':     'categories'
};

export interface BaseMasterEntityDto {
    id: number;
    name: string;
}

export type MasterEntityDtoMap = {
    artist:     ArtistDto;
    album:      AlbumDto;
    track:      TrackDto;
    category:   CategoryDto;
};

export const masterEntityFromDtoMappers: {
    [K in MasterEntityType]: (dto: MasterEntityDtoMap[K]) => MasterEntityMap[K];
} = {
    artist:     createArtistFromDto,
    album:      createAlbumFromDto,
    track:      createTrackFromDto,
    category:   createCategoryFromDto
}
