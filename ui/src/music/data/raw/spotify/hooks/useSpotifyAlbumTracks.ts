import { useQuery } from "@tanstack/react-query";
import { fetchSpotifyAlbumTracks, type SpotifyAlbumTrackDto } from "@/music/data/raw/spotify/api/spotify-albums.ts";
import { spotifyAlbumTracksKeys } from "@/music/data/raw/spotify/utils/query-keys.ts";

export function useSpotifyAlbumTracks(albumId: number) {
    const { data: tracks, isLoading, isError, error } = useQuery<SpotifyAlbumTrackDto[]>({
        queryKey: spotifyAlbumTracksKeys.detail(albumId),
        queryFn: () => fetchSpotifyAlbumTracks(albumId),
        enabled: albumId > 0,
    });

    return { tracks: tracks ?? [], isLoading, isError, error };
}
