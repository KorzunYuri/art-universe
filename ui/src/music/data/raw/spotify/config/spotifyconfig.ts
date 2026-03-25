import axios from 'axios';
import { setupApiInterceptors } from '@/shared/services/apiSetup';
import { appConfig } from '@/shared/config/appConfig';

const readApiUrl = `http://${appConfig.spotifyReadApiHost}:${appConfig.spotifyReadApiPort}/api/v1/spotify`;

const readApi = axios.create({
    baseURL: readApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

setupApiInterceptors(readApi);

export class SpotifyConfig {
    static readonly readApi = readApi;

    static readonly spotifyBaseUrls = {
        artist: 'https://open.spotify.com/artist/',
        album:  'https://open.spotify.com/album/',
        track:  'https://open.spotify.com/track/',
    };
}
