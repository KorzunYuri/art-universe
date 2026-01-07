import type {BaseMasterEntityDto} from "@/music/data/master/api/music-data-commons.ts";
import {TrackImpl} from "@/music/shared/types/entities.ts";
import type {BasePageSearchParams} from "@/music/shared/types/page.ts";

export interface TrackDto extends BaseMasterEntityDto {
    primaryArtistId: number;
}

export interface TrackPageSearchParams extends BasePageSearchParams {}

export function createTrackFromDto(dto: TrackDto) {
    return new TrackImpl(
        dto.id,
        dto.name,
        dto.primaryArtistId
    )
}