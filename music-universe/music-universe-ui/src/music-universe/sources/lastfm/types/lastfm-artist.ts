import type { Approvable } from "@/music-universe/shared/types/approvable";
import type { Bindable, BoundEntity } from "@/music-universe/shared/types/bindable";

export interface LastfmArtist extends Approvable, Bindable {
    id: number;
    name: string;
    mbid: string | null;
    url: string;
    approvalStatus: number;
    playCount: number;
    listenersCount: number;
    boundEntity?: BoundEntity;
}
