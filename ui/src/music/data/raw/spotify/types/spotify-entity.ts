export * from './spotify-base-entity.ts';

import { SpotifyArtist } from "./spotify-artist.ts";
import { SpotifyAlbum } from "./spotify-album.ts";
import { SpotifyTrack } from "./spotify-track.ts";
import { SpotifyGenre } from "./spotify-genre.ts";

export type SpotifySupportedEntityTypeMap = {
    artist:   SpotifyArtist;
    album:    SpotifyAlbum;
    track:    SpotifyTrack;
    category: SpotifyGenre;
}

export type SpotifySupportedEntityType = keyof SpotifySupportedEntityTypeMap;
