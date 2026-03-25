import { SpotifyGenre } from "@/music/data/raw/spotify/types/spotify-genre.ts";
import type { BasePageSearchParams } from "@/shared/types/page.ts";

export interface SpotifyGenreDto {
    id: number;
    name: string;
    spotifyId: string;
    approvalStatus: number;
}

export function createSpotifyGenreFromDto(dto: SpotifyGenreDto): SpotifyGenre {
    return new SpotifyGenre(
        dto.id,
        dto.name,
        dto.spotifyId
    );
}

export interface SpotifyGenresPageSearchParams extends BasePageSearchParams {}
