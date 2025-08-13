import {BaseLastfmEntity} from "./lastfm-base-entity";
import {type ArtistRelatedRawEntity} from "@/music-universe/shared/types/entities.ts";
import type {Album} from "@/music-universe/shared/types/entities.ts";
import type {LastfmArtistDto} from "@/music-universe/sources/lastfm/api/lastfm-artists.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";
import type {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";

export class LastfmAlbum
    extends BaseLastfmEntity<"album">
    implements ArtistRelatedRawEntity<"album">
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
        masterEntity?: Album
    ) {
        super(id, name, approvalStatus, masterEntity);
        this.url = url;
        this.mbid = mbid;
        this.playCount = playCount;
        this.listenersCount = listenersCount;
        this.artist = artist;
    }

    getEntityType(): "album" {
        return "album";
    }

    getExternalArtistId(): number | undefined {
        return this.artist?.id;
    }

    getMasterArtistId(): number | undefined {
        return this.getMasterEntity()?.primaryArtistId;
    }
}
