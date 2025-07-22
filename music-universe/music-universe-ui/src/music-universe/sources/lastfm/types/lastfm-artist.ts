import {BaseRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {LastfmEntity} from "./lastfm-entity";
import type {Artist, MasterEntityType} from "@/music-universe/music-data/types/master-entities";

/**
 * LastFM Artist entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmArtist extends BaseRawEntity<Artist> implements LastfmEntity<Artist> {
    url: string;
    mbid: string | null;
    approvalStatus: number;
    playCount: number | null;
    listenersCount: number | null;

    constructor(
        id: number,
        name: string,
        url: string,
        mbid: string | null,
        approvalStatus: number,
        playCount: number | null,
        listenersCount: number | null,
        masterEntity?: Artist
    ) {
        super(id, name, masterEntity);
        this.url = url;
        this.mbid = mbid;
        this.approvalStatus = approvalStatus;
        this.playCount = playCount;
        this.listenersCount = listenersCount;
    }

    getEntityType(): MasterEntityType {
        return "artist";
    }
    
    setApprovalStatus(approvalStatus: number): void {
        this.approvalStatus = approvalStatus;
    }
}

