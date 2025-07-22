import { BaseRawEntity } from "@/music-universe/shared/types/entities.ts";
import type { LastfmEntity } from "./lastfm-entity";
import type { MasterEntityType, Track } from "@/music-universe/music-data/types/master-entities";
import type {LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";

/**
 * LastFM Track entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTrack extends BaseRawEntity<Track> implements LastfmEntity<Track> {
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;
    artist?: LastfmArtistDto;

    constructor(
        id: number,
        name: string,
        url: string,
        mbid: string | null,
        approvalStatus: number,
        playCount: number | null,
        listenersCount: number | null,
        artist?: LastfmArtistDto,
        masterEntity?: Track
    ) {
        super(id, name, masterEntity);
        this.url = url;
        this.mbid = mbid;
        this.approvalStatus = approvalStatus;
        this.playCount = playCount;
        this.listenersCount = listenersCount;
        this.artist = artist;
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
export function createLastfmTrack(
    id: number,
    name: string,
    url: string,
    mbid: string | null,
    approvalStatus: number,
    playCount: number | null,
    listenersCount: number | null,
    artist?: LastfmArtistDto,
    masterEntity?: Track
): LastfmTrack {
    return new LastfmTrack(
        id,
        name,
        url,
        mbid,
        approvalStatus,
        playCount,
        listenersCount,
        artist,
        masterEntity
    );
}
