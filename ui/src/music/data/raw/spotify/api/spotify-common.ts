import { createSpotifyArtistFromDto, type SpotifyArtistDto } from "./spotify-artists.ts";
import { createSpotifyAlbumFromDto, type SpotifyAlbumDto } from "./spotify-albums.ts";
import { createSpotifyTrackFromDto, type SpotifyTrackDto } from "./spotify-tracks.ts";
import { createSpotifyGenreFromDto, type SpotifyGenreDto } from "./spotify-genres.ts";
import type {
    SpotifySupportedEntityType,
    SpotifySupportedEntityTypeMap
} from "@/music/data/raw/spotify/types/spotify-entity.ts";

export const spotifyEntityTypeToEndpoint: Record<SpotifySupportedEntityType, string> = {
    'artist': 'artists',
    'album': 'albums',
    'track': 'tracks',
    'category': 'genres'
};

export type SpotifyEntityDtoMap = {
    artist:   SpotifyArtistDto;
    album:    SpotifyAlbumDto;
    track:    SpotifyTrackDto;
    category: SpotifyGenreDto;
};

export const spotifyEntityMappers: {
    [K in SpotifySupportedEntityType]: (dto: SpotifyEntityDtoMap[K]) => SpotifySupportedEntityTypeMap[K];
} = {
    artist:   createSpotifyArtistFromDto,
    album:    createSpotifyAlbumFromDto,
    track:    createSpotifyTrackFromDto,
    category: createSpotifyGenreFromDto,
};
