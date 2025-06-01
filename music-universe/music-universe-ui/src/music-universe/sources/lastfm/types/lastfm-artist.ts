export interface LastfmArtist {
    id: number
    name: string
    mbid: string | null
    url: string
    approval_status: number
    play_count: number
    listeners_count: number
}