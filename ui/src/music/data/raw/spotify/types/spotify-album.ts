import { BaseSpotifyEntity } from "./spotify-base-entity.ts";
import { type ArtistRelatedRawEntity } from "@/music/shared/types/entities.ts";
import type { Album } from "@/music/shared/types/entities.ts";

export class SpotifyAlbum
    extends BaseSpotifyEntity<"album">
    implements ArtistRelatedRawEntity<"album">
{
    spotifyId: string;
    spotifyUrl: string | null;
    totalTracks: number | null;
    releaseDate: string | null;
    primaryArtistId: number | null;
    primaryArtistName: string | null;

    constructor(
        id: number,
        name: string,
        spotifyId: string,
        spotifyUrl: string | null,
        totalTracks: number | null,
        releaseDate: string | null,
        primaryArtistId: number | null,
        primaryArtistName: string | null,
        masterEntity?: Album
    ) {
        super(id, name, masterEntity);
        this.spotifyId = spotifyId;
        this.spotifyUrl = spotifyUrl;
        this.totalTracks = totalTracks;
        this.releaseDate = releaseDate;
        this.primaryArtistId = primaryArtistId;
        this.primaryArtistName = primaryArtistName;
    }

    getEntityType(): "album" {
        return "album";
    }

    get artist(): { id: number; name: string } | undefined {
        if (this.primaryArtistId && this.primaryArtistName) {
            return { id: this.primaryArtistId, name: this.primaryArtistName };
        }
        return undefined;
    }

    getExternalArtistId(): number | undefined {
        return this.primaryArtistId ?? undefined;
    }

    getMasterArtistId(): number | undefined {
        return this.getMasterEntity()?.primaryArtistId;
    }
}
