export class LastfmConfig {
    static readonly baseApiUrl = 'http://localhost:8080/api/lastfm'
    static readonly mbBaseUrls = {
        artist:   'https://musicbrainz.org/artist/',
        album:    'https://musicbrainz.org/release/',
        track:    'https://musicbrainz.org/recording/',
    }
}