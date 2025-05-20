export interface LastfmArtist {
    id: number
    name: string
    mbid: string | null
    url: string
    approval_status: number
    play_count: number
    listeners_count: number
}

export async function fetchArtists(): Promise<LastfmArtist[]> {

    // TODO unmock data
    return [
        {
            id: 1,
            name: 'The Beatles',
            mbid: 'b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d',
            url: 'https://www.last.fm/music/The+Beatles',
            approval_status: 1,
            listeners_count: 12345678,
            play_count: 987654321,
        },
        {
            id: 3,
            name: 'Radiohead with a very very very very very long name for tooltip',
            mbid: null,
            url: 'https://www.last.fm/music/Radiohead',
            approval_status: 4,
            listeners_count: 5678901,
            play_count: 123456789,
        },
    ]

    // const response = await fetch('http://localhost:8080/api/lastfm/artists')
    // if (!response.ok) throw new Error('Failed to load artists')
    // return response.json()
}