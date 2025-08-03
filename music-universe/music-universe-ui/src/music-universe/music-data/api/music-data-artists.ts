import {ArtistImpl} from "@/music-universe/shared/types/entities.ts";
import type {BaseMasterEntityDto} from "@/music-universe/music-data/api/music-data-commons.ts";
import type {BasePageSearchParams} from "@/music-universe/shared/types/page.ts";

export interface ArtistDto extends BaseMasterEntityDto {}

export interface ArtistPageSearchParams extends BasePageSearchParams {}

export function createArtistFromDto(dto: ArtistDto) {
    return new ArtistImpl(
        dto.id,
        dto.name
    )
}