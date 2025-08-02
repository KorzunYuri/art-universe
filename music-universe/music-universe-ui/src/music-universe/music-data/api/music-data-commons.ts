import {type MasterEntityMap, type MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import {type ArtistDto, createArtistFromDto} from "@/music-universe/music-data/api/music-data-artists.ts";
import {createDimensionFromDto, type DimensionDto} from "@/music-universe/music-data/api/music-data-dimensions.ts";
import {type CategoryDto, createCategoryFromDto} from "@/music-universe/music-data/api/music-data-categories.ts";
import {type AlbumDto, createAlbumFromDto} from "@/music-universe/music-data/api/music-data-albums.ts";
import {createTrackFromDto, type TrackDto} from "@/music-universe/music-data/api/music-data-tracks.ts";

export const entityToEndpoint: Record<MasterEntityType, string> = {
    'artist':       'artists',
    'album':        'albums',
    'track':        'tracks',
    'category':     'categories',
    'dimension':    'dimensions'
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
    dimension:  DimensionDto;
};

export const masterEntityFromDtoMappers: {
    [K in MasterEntityType]: (dto: MasterEntityDtoMap[K]) => MasterEntityMap[K];
} = {
    artist:     createArtistFromDto,
    album:      createAlbumFromDto,
    track:      createTrackFromDto,
    category:   createCategoryFromDto,
    dimension:  createDimensionFromDto
}
