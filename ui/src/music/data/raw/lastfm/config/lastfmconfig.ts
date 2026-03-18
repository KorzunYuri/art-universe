import axios from 'axios';
import { setupApiInterceptors } from '@/shared/services/apiSetup';
import { appConfig } from '@/shared/config/appConfig';

const readApiUrl = `http://${appConfig.lastfmReadApiHost}:${appConfig.lastfmReadApiPort}/api/v1`;
const writeApiUrl = `http://${appConfig.lastfmWriteApiHost}:${appConfig.lastfmWriteApiPort}/api/v1`;

const readApi = axios.create({
    baseURL: readApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

const writeApi = axios.create({
    baseURL: writeApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

setupApiInterceptors(readApi);
setupApiInterceptors(writeApi);

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
