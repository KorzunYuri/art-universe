import type { BaseEntity } from "@/music/shared/types/entities.ts";
import { SpotifyArtist } from "@/music/data/raw/spotify/types/spotify-artist.ts";
import type { Artist } from "@/music/shared/types/entities.ts";
import type { BasePageSearchParams } from "@/shared/types/page.ts";

export interface SpotifyArtistDto extends BaseEntity {
    spotifyId: string;
    spotifyUrl: string | null;
    approvalStatus: number;
}

export function createSpotifyArtistFromDto(dto: SpotifyArtistDto, masterEntity?: Artist): SpotifyArtist {
    return new SpotifyArtist(
        dto.id,
        dto.name,
        dto.spotifyId,
        dto.spotifyUrl,
        masterEntity
    );
}

export interface SpotifyArtistsPageSearchParams extends BasePageSearchParams {}
