/**
 * Entity types in music-data system
 */
export enum MusicDataEntityType {
    ARTIST = 'ARTIST',
    ALBUM = 'ALBUM',
    TRACK = 'TRACK',
    CATEGORY = 'CATEGORY',
    DIMENSION = 'DIMENSION'
}

/**
 * Mapping between LastFM entity types and music-data entity types
 */
export const LastfmToMusicDataEntityTypeMap: Record<string, MusicDataEntityType> = {
    'ARTIST': MusicDataEntityType.ARTIST,
    'ALBUM': MusicDataEntityType.ALBUM,
    'TRACK': MusicDataEntityType.TRACK,
    'TAG': MusicDataEntityType.CATEGORY
};
