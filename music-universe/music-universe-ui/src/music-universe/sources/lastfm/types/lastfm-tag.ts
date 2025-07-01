import type { Approvable } from "@/music-universe/shared/types/approvable";
import type { Bindable, BoundEntity } from "@/music-universe/shared/types/bindable";

export interface LastfmTagDto {
    id: number;
    name: string;
    url: string | null;
    approvalStatus: number;
    usageCount: number | null;
    usageUsersCount: number | null;
}

export interface LastfmTag extends LastfmTagDto, Approvable, Bindable {
    boundEntity?: BoundEntity;
}
