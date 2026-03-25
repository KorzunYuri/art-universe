export const spotifyAlbumTracksKeys = {
  all: ['spotify', 'album-tracks'] as const,
  detail: (albumId: number) => [...spotifyAlbumTracksKeys.all, albumId] as const,
};
