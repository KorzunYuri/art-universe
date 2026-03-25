import { BaseSpotifyEntity } from "./spotify-base-entity.ts";
import { type ArtistRelatedRawEntity } from "@/music/shared/types/entities.ts";
import type { Track } from "@/music/shared/types/entities.ts";

export class SpotifyTrack
    extends BaseSpotifyEntity<"track">
    implements ArtistRelatedRawEntity<"track">
{
    spotifyId: string;
    spotifyUrl: string | null;
    durationMs: number | null;
    trackNumber: number | null;
    primaryArtistId: number | null;
    primaryArtistName: string | null;
    albumId: number | null;

    constructor(
        id: number,
        name: string,
        spotifyId: string,
        spotifyUrl: string | null,
        durationMs: number | null,
        trackNumber: number | null,
        primaryArtistId: number | null,
        primaryArtistName: string | null,
        albumId: number | null,
        masterEntity?: Track
    ) {
        super(id, name, masterEntity);
        this.spotifyId = spotifyId;
        this.spotifyUrl = spotifyUrl;
        this.durationMs = durationMs;
        this.trackNumber = trackNumber;
        this.primaryArtistId = primaryArtistId;
        this.primaryArtistName = primaryArtistName;
        this.albumId = albumId;
    }

    getEntityType(): "track" {
        return "track";
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
