export class LastfmConfig {
    static readonly readApiUrl = `http://${import.meta.env.VITE_MURAW_LASTFM_READ_API_HOST || 'localhost'}:${import.meta.env.VITE_MURAW_LASTFM_READ_API_EXTERNAL_PORT || '8084'}/api/v1`
    static readonly writeApiUrl = `http://${import.meta.env.VITE_MURAW_LASTFM_WRITE_API_HOST || 'localhost'}:${import.meta.env.VITE_MURAW_LASTFM_WRITE_API_EXTERNAL_PORT || '8085'}/api/v1`
    
    // Backward compatibility
    static readonly baseApiUrl = LastfmConfig.readApiUrl
    
    static readonly mbBaseUrls = {
        artist:   'https://musicbrainz.org/artist/',
        album:    'https://musicbrainz.org/release/',
        track:    'https://musicbrainz.org/recording/',
    }
}
