import {BaseLastfmEntity} from "./lastfm-base-entity";
import type {Artist} from "@/music-universe/shared/types/entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

export class LastfmArtist extends BaseLastfmEntity<"artist"> {
    url: string;
    mbid: string | null;
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
        super(id, name, approvalStatus, masterEntity);
        this.url = url;
        this.mbid = mbid;
        this.playCount = playCount;
        this.listenersCount = listenersCount;
    }

    getEntityType(): "artist" {
        return "artist";
    }
}
