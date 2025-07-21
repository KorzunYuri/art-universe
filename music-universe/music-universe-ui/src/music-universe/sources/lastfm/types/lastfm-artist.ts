import {BaseRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {LastfmEntity} from "./lastfm-entity";
import type {Artist, MasterEntityType} from "@/music-universe/music-data/types/master-entities";
import type {LastfmArtistResponseDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";

/**
 * LastFM Artist entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmArtist extends BaseRawEntity<Artist> implements LastfmEntity<Artist> {
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;

    constructor(data: LastfmArtistResponseDto, masterEntity?: Artist) {
        super(data.id, data.name, masterEntity);
        this.url = data.url;
        this.mbid = data.mbid;
        this.approvalStatus = data.approvalStatus;
        this.playCount = data.playCount;
        this.listenersCount = data.listenersCount;
    }

    getEntityType(): MasterEntityType {
        return "artist";
    }
    
    setApprovalStatus(approvalStatus: number): void {
        this.approvalStatus = approvalStatus;
    }
}

/**
 * Factory function to create LastfmArtist from API response
 */
export function createLastfmArtist(data: LastfmArtistResponseDto, masterEntity?: Artist): LastfmArtist {
    return new LastfmArtist(data, masterEntity);
}
