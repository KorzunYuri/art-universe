import type {BaseMasterEntityDto} from "@/music-universe/music-data/api/music-data-commons.ts";
import {AlbumImpl} from "@/music-universe/shared/types/entities.ts";
import type {BasePageSearchParams} from "@/music-universe/shared/types/page.ts";

export interface AlbumDto extends BaseMasterEntityDto {
    primaryArtistId: number;
}

export interface AlbumPageSearchParams extends BasePageSearchParams {}

export function createAlbumFromDto(dto: AlbumDto) {
    return new AlbumImpl(
        dto.id,
        dto.name,
        dto.primaryArtistId
    )
}