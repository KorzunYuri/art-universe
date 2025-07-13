/**
 * Entity types in music-data system
 */
export const MusicDataEntityType = {
    ARTIST: 'ARTIST',
    ALBUM: 'ALBUM',
    TRACK: 'TRACK',
    CATEGORY: 'CATEGORY',
    DIMENSION: 'DIMENSION'
} as const;

export type MusicDataEntityType = typeof MusicDataEntityType[keyof typeof MusicDataEntityType];

/**
 * Mapping between LastFM entity types and music-data entity types
 */
export const LastfmToMusicDataEntityTypeMap: Record<string, string> = {
    'ARTIST': MusicDataEntityType.ARTIST,
    'ALBUM': MusicDataEntityType.ALBUM,
    'TRACK': MusicDataEntityType.TRACK,
    'TAG': MusicDataEntityType.CATEGORY
};
