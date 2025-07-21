import { BaseRawEntity } from "@/music-universe/shared/types/entities.ts";
import type { LastfmEntity } from "./lastfm-entity";
import type {Artist, MasterEntityType} from "@/music-universe/music-data/types/master-entities";

export interface LastfmTrackArtistDto {
    id: number;
    name: string;
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
}

/**
 * LastFM Artist entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmArtist extends BaseRawEntity<Artist> implements LastfmEntity<Artist> {
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;

    constructor(data: LastfmTrackArtistDto, masterEntity?: Artist) {
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
export function createLastfmArtist(data: LastfmTrackArtistDto, masterEntity?: Artist): LastfmArtist {
    return new LastfmArtist(data, masterEntity);
}
