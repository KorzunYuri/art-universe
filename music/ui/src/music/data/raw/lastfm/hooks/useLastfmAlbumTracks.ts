import { useQuery } from "@tanstack/react-query";
import { fetchLastfmAlbumTracks, type LastfmAlbumTrackDto } from "@/music/data/raw/lastfm/api/lastfm-albums.ts";
import { lastfmAlbumTracksKeys } from "@/music/data/raw/lastfm/utils/query-keys.ts";

export function useLastfmAlbumTracks(albumId: number) {
    const { data: tracks, isLoading, isError, error } = useQuery<LastfmAlbumTrackDto[]>({
        queryKey: lastfmAlbumTracksKeys.detail(albumId),
        queryFn: () => fetchLastfmAlbumTracks(albumId),
        enabled: albumId > 0,
    });

    return { tracks: tracks ?? [], isLoading, isError, error };
}
