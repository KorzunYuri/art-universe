export class LastfmConfig {
    static readonly baseApiUrl = `http://${import.meta.env.VITE_MURAW_LASTFM_APP_HOST || 'localhost'}:${import.meta.env.VITE_MURAW_LASTFM_APP_EXTERNAL_PORT || '8081'}/api/v1`
    static readonly mbBaseUrls = {
        artist:   'https://musicbrainz.org/artist/',
        album:    'https://musicbrainz.org/release/',
        track:    'https://musicbrainz.org/recording/',
    }
}
