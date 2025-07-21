import { BaseRawEntity } from "@/music-universe/shared/types/entities.ts";
import type { LastfmTrackArtistDto } from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";
import type { LastfmEntity } from "./lastfm-entity";
import type { MasterEntityType, Track } from "@/music-universe/music-data/types/master-entities";

export interface LastfmTrackDto {
    id: number;
    name: string;
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
    artist?: LastfmTrackArtistDto;
}

/**
 * LastFM Track entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTrack extends BaseRawEntity<Track> implements LastfmEntity<Track> {
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
    artist?: LastfmTrackArtistDto;

    constructor(data: LastfmTrackDto, masterEntity?: Track) {
        super(data.id, data.name, masterEntity);
        this.url = data.url;
        this.mbid = data.mbid;
        this.approvalStatus = data.approvalStatus;
        this.playCount = data.playCount;
        this.listenersCount = data.listenersCount;
        this.artist = data.artist;
    }

    getEntityType(): MasterEntityType {
        return "track";
    }
    
    setApprovalStatus(approvalStatus: number): void {
        this.approvalStatus = approvalStatus;
    }
}

/**
 * Factory function to create LastfmTrack from API response
 */
export function createLastfmTrack(data: LastfmTrackDto, masterEntity?: Track): LastfmTrack {
    return new LastfmTrack(data, masterEntity);
}
