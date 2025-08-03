import {BaseRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {LastfmEntity} from "./lastfm-entity";
import type {Track} from "@/music-universe/shared/types/entities.ts";
import type {LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

/**
 * LastFM Track entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTrack extends BaseRawEntity<"track"> implements LastfmEntity<"track"> {
    url: string;
    mbid: string | null;
    approvalStatus: ApprovalStatusType;
    playCount: number | null;
    listenersCount: number | null;
    artist?: LastfmArtistDto;

    constructor(
        id: number,
        name: string,
        url: string,
        mbid: string | null,
        approvalStatus: ApprovalStatusType,
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

    getEntityType(): "track" {
        return "track";
    }

    setApprovalStatus(approvalStatus: ApprovalStatusType): void {
        this.approvalStatus = approvalStatus;
    }

    getPrimaryArtistId(): number | undefined {
        return this.artist?.id;
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
    approvalStatus: ApprovalStatusType,
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
