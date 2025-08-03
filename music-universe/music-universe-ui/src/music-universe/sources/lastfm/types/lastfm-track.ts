import {BaseLastfmEntity} from "./lastfm-entity";
import {type ArtistRelatedRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {Track} from "@/music-universe/shared/types/entities.ts";
import type {LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";

/**
 * LastFM Track entity that extends BaseRawEntity and implements LastfmEntity
 */
export class LastfmTrack
    extends
        BaseLastfmEntity<"track">
    implements
        ArtistRelatedRawEntity<"track">
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
        return this.getMasterEntity()?.id;
    }
}