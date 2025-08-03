// Map entity type to API endpoint
import {
    type Album,
    type Artist,
    type Category,
    type Dimension,
    type MasterEntityType,
    type Track,
} from "@/music-universe/shared/types/entities.ts";

export const entityToEndpoint: Record<MasterEntityType, string> = {
    'artist': 'artists',
    'album': 'albums',
    'track': 'tracks',
    'category': 'categories',
    'dimension': 'dimensions'
};

export type EntityTypeMap = {
    artist:     Artist;
    album:      Album;
    track:      Track;
    category:   Category;
    dimension:  Dimension;
};

