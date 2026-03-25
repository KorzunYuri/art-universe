import { BaseSpotifyEntity } from "./spotify-base-entity.ts";
import type { Artist } from "@/music/shared/types/entities.ts";

export class SpotifyArtist extends BaseSpotifyEntity<"artist"> {
    spotifyId: string;
    spotifyUrl: string | null;

    constructor(
        id: number,
        name: string,
        spotifyId: string,
        spotifyUrl: string | null,
        masterEntity?: Artist
    ) {
        super(id, name, masterEntity);
        this.spotifyId = spotifyId;
        this.spotifyUrl = spotifyUrl;
    }

    getEntityType(): "artist" {
        return "artist";
    }
}
