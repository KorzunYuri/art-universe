import axios from 'axios';
import { setupTracingInterceptor } from '@/music/shared/services/tracingInterceptor';

const readApiUrl = `http://${import.meta.env.VITE_MURAW_LASTFM_READ_API_HOST || 'localhost'}:${import.meta.env.VITE_MURAW_LASTFM_READ_API_EXTERNAL_PORT || '8084'}/api/v1`;
const writeApiUrl = `http://${import.meta.env.VITE_MURAW_LASTFM_WRITE_API_HOST || 'localhost'}:${import.meta.env.VITE_MURAW_LASTFM_WRITE_API_EXTERNAL_PORT || '8085'}/api/v1`;

// Create axios instance for LastFM Read API with tracing
const readApi = axios.create({
    baseURL: readApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Create axios instance for LastFM Write API with tracing
const writeApi = axios.create({
    baseURL: writeApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add distributed tracing headers to all LastFM API requests
setupTracingInterceptor(readApi);
setupTracingInterceptor(writeApi);

export class LastfmConfig {
    // Axios instances with tracing enabled
    static readonly readApi = readApi;
    static readonly writeApi = writeApi;

    static readonly mbBaseUrls = {
        artist:   'https://musicbrainz.org/artist/',
        album:    'https://musicbrainz.org/release/',
        track:    'https://musicbrainz.org/recording/',
    };
}
