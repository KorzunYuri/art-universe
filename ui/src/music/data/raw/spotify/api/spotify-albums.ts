import { SpotifyAlbum } from "@/music/data/raw/spotify/types/spotify-album.ts";
import type { Album } from "@/music/shared/types/entities.ts";
import type { BasePageSearchParams } from "@/shared/types/page.ts";
import { SpotifyConfig } from "@/music/data/raw/spotify/config/spotifyconfig.ts";

export interface SpotifyAlbumDto {
    id: number;
    name: string;
    spotifyId: string;
    albumType: number | null;
    totalTracks: number | null;
    releaseDate: string | null;
    releaseDatePrecision: number | null;
    spotifyUrl: string | null;
    primaryArtistId: number | null;
    primaryArtistSpotifyId: string | null;
    primaryArtistName: string | null;
    approvalStatus: number;
}

export function createSpotifyAlbumFromDto(dto: SpotifyAlbumDto, masterEntity?: Album): SpotifyAlbum {
    return new SpotifyAlbum(
        dto.id,
        dto.name,
        dto.spotifyId,
        dto.spotifyUrl,
        dto.totalTracks,
        dto.releaseDate,
        dto.primaryArtistId,
        dto.primaryArtistName,
        masterEntity
    );
}

export interface SpotifyAlbumsPageSearchParams extends BasePageSearchParams {}

export interface SpotifyAlbumTrackDto {
    id: number;
    trackNumber: number | null;
    trackId: number;
    trackName: string;
    spotifyId: string;
    spotifyUrl: string | null;
    primaryArtistId: number | null;
    durationMs: number | null;
}

const spotifyReadApi = SpotifyConfig.readApi;

export async function fetchSpotifyAlbumTracks(albumId: number): Promise<SpotifyAlbumTrackDto[]> {
    const response = await spotifyReadApi.get<SpotifyAlbumTrackDto[]>(`/albums/${albumId}/tracks`);
    return response.data;
}
