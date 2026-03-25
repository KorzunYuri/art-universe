import { SpotifyTrack } from "@/music/data/raw/spotify/types/spotify-track.ts";
import type { Track } from "@/music/shared/types/entities.ts";
import type { BasePageSearchParams } from "@/shared/types/page.ts";

export interface SpotifyTrackDto {
    id: number;
    name: string;
    spotifyId: string;
    durationMs: number | null;
    trackNumber: number | null;
    discNumber: number | null;
    hasExplicitLyrics: boolean | null;
    isPlayable: boolean | null;
    spotifyUrl: string | null;
    isrc: string | null;
    primaryArtistId: number | null;
    primaryArtistSpotifyId: string | null;
    primaryArtistName: string | null;
    albumId: number | null;
    albumSpotifyId: string | null;
    approvalStatus: number;
}

export function createSpotifyTrackFromDto(dto: SpotifyTrackDto, masterEntity?: Track): SpotifyTrack {
    return new SpotifyTrack(
        dto.id,
        dto.name,
        dto.spotifyId,
        dto.spotifyUrl,
        dto.durationMs,
        dto.trackNumber,
        dto.primaryArtistId,
        dto.primaryArtistName,
        dto.albumId,
        masterEntity
    );
}

export interface SpotifyTracksPageSearchParams extends BasePageSearchParams {}
