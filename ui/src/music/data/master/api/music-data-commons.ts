import {type MasterEntityMap, type MasterEntityType} from "@/music/shared/types/entities.ts";
import {type ArtistDto, createArtistFromDto} from "@/music/data/master/api/music-data-artists.ts";
import {type CategoryDto, createCategoryFromDto} from "@/music/data/master/api/music-data-categories.ts";
import {type AlbumDto, createAlbumFromDto} from "@/music/data/master/api/music-data-albums.ts";
import {createTrackFromDto, type TrackDto} from "@/music/data/master/api/music-data-tracks.ts";
import type {PersonDto} from "@/art/data/master/api/art-data-persons.ts";

/** Music-domain entity type — excludes cross-domain types like person */
export type MusicMasterEntityType = "artist" | "album" | "track" | "category";

export const entityToEndpoint: Record<MusicMasterEntityType, string> = {
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
    person:     PersonDto;
};

export const masterEntityFromDtoMappers: {
    [K in MusicMasterEntityType]: (dto: MasterEntityDtoMap[K]) => MasterEntityMap[K];
} = {
    artist:     createArtistFromDto,
    album:      createAlbumFromDto,
    track:      createTrackFromDto,
    category:   createCategoryFromDto
}
