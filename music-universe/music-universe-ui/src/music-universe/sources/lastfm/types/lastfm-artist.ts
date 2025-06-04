export interface LastfmArtist {
    id: number
    name: string
    mbid: string | null
    url: string
    approvalStatus: number
    playCount: number
    listenersCount: number
}