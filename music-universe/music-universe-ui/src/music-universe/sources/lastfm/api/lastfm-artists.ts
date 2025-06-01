import { LastfmConfig } from "@/music-universe/sources/lastfm/config/lastfmconfig.ts"
import type {Page} from "@/music-universe/shared/types/page.ts";
import type {LastfmArtist} from "@/music-universe/sources/lastfm/types/lastfm-artist.ts";

export async function fetchArtists(params: {
    search?: string
    page?: number
    size?: number
    sort?: string
}): Promise<Page<LastfmArtist>> {
    const res = await fetch(`${LastfmConfig.baseApiUrl}/artists?${
        new URLSearchParams({
            search: params.search || '',
            page: String(params.page ?? 0),
            size: String(params.size ?? 20),
            sort: params.sort || 'name,asc',
        })
    }`)
    const json = await res.json()
    return json.data
}