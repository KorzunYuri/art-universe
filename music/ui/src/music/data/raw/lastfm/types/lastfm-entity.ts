// Re-export interfaces and base class
export * from './lastfm-interfaces.ts';
export * from './lastfm-base-entity.ts';

// Import concrete implementations for type mapping
import {LastfmArtist} from "@/music/data/raw/lastfm/types/lastfm-artist.ts";
import {LastfmAlbum} from "@/music/data/raw/lastfm/types/lastfm-album.ts";
import {LastfmTrack} from "@/music/data/raw/lastfm/types/lastfm-track.ts";
import {LastfmTag} from "@/music/data/raw/lastfm/types/lastfm-tag.ts";

// Map of LastFM entity types to their corresponding implementations
export type LastfmSupportedEntityTypeMap = {
    artist:     LastfmArtist,
    album:      LastfmAlbum,
    track:      LastfmTrack,
    category:   LastfmTag,
}
