import { BaseSpotifyEntity } from "./spotify-base-entity.ts";

export class SpotifyGenre extends BaseSpotifyEntity<"category"> {
    spotifyId: string;

    constructor(
        id: number,
        name: string,
        spotifyId: string
    ) {
        super(id, name);
        this.spotifyId = spotifyId;
    }

    getEntityType(): "category" {
        return "category";
    }
}
