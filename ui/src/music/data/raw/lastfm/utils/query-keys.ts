/**
 * Query key factories for lastfm-specific data
 */
export const lastfmAlbumTracksKeys = {
  all: ['lastfm', 'album-tracks'] as const,
  detail: (albumId: number) => [...lastfmAlbumTracksKeys.all, albumId] as const,
};

export const lastfmBoundAlbumTracksKeys = {
  all: ['lastfm', 'bound-album-tracks'] as const,
  detail: (albumId: number) => [...lastfmBoundAlbumTracksKeys.all, albumId] as const,
};
