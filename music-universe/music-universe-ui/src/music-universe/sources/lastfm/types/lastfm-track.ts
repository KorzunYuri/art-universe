import type { Approvable } from "@/music-universe/shared/types/approvable";
import type { Bindable, BoundEntity } from "@/music-universe/shared/types/bindable";
import type { LastfmTrackArtistDto } from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";

export interface LastfmTrackDto {
    id:                 number;
    name:               string;
    url:                string;
    mbid:               string | null;
    approvalStatus:     number;
    playCount:          number | null;
    listenersCount:     number | null;
}

export interface LastfmTrack extends LastfmTrackDto, Approvable, Bindable {
    boundEntity?: BoundEntity;
    artist?: LastfmTrackArtistDto;
}
