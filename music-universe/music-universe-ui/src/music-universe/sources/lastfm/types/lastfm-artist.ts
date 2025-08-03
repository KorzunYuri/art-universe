import {BaseRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {LastfmEntity} from "./lastfm-entity";
import type {Artist} from "@/music-universe/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

/**
 * LastFM Artist entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmArtist extends BaseRawEntity<"artist"> implements LastfmEntity<"artist"> {
    url: string;
    mbid: string | null;
    approvalStatus: ApprovalStatusType;
    playCount: number | null;
    listenersCount: number | null;

    constructor(
        id: number,
        name: string,
        url: string,
        mbid: string | null,
        approvalStatus: ApprovalStatusType,
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

    getEntityType(): "artist" {
        return "artist";
    }

    setApprovalStatus(approvalStatus: ApprovalStatusType): void {
        this.approvalStatus = approvalStatus;
    }
}

