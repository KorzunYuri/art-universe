// Re-export interfaces and base class
export * from './lastfm-interfaces';
export * from './lastfm-base-entity';

// Import concrete implementations for type mapping
import {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";
import {LastfmTrack} from "@/music-universe/sources/lastfm/types/lastfm-track.ts";
import {LastfmTag} from "@/music-universe/sources/lastfm/types/lastfm-tag.ts";

// Map of LastFM entity types to their corresponding implementations
export type LastfmSupportedEntityTypeMap = {
    artist:     LastfmArtist,
    track:      LastfmTrack,
    category:   LastfmTag,
}
