import {BaseLastfmEntity} from "./lastfm-base-entity.ts";
import {type ArtistRelatedRawEntity} from "@/music/shared/types/entities.ts";
import type {Track} from "@/music/shared/types/entities.ts";
import type {LastfmArtistDto} from "@/music/data/raw/lastfm/api/lastfm-artists.ts";
import type {ApprovalStatusType} from "@/music/data/raw/lastfm/constants/approvalStatus.ts";
import type {LastfmArtist} from "@/music/data/raw/lastfm/types/lastfm-artist.ts";

export class LastfmTrack
    extends BaseLastfmEntity<"track">
    implements ArtistRelatedRawEntity<"track">
{
    url: string;
    mbid: string | null;
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
        artist?: LastfmArtist,
        masterEntity?: Track
    ) {
        super(id, name, approvalStatus, masterEntity);
        this.url = url;
        this.mbid = mbid;
        this.playCount = playCount;
        this.listenersCount = listenersCount;
        this.artist = artist;
    }

    getEntityType(): "track" {
        return "track";
    }

    getExternalArtistId(): number | undefined {
        return this.artist?.id;
    }

    getMasterArtistId(): number | undefined {
        return this.getMasterEntity()?.primaryArtistId;
    }
}
