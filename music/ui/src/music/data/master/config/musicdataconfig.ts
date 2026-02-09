import axios from 'axios';
import { setupTracingInterceptor } from '@/music/shared/services/tracingInterceptor';
import { appConfig } from '@/music/shared/config/appConfig';

const baseApiUrl = `http://${appConfig.musicDataHost}:${appConfig.musicDataPort}/api/v1`;

// Create axios instance for Master Data API with tracing
const api = axios.create({
    baseURL: baseApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add distributed tracing headers to all Master Data API requests
setupTracingInterceptor(api);

export const MusicDataConfig = {
    baseApiUrl,
    api,
};
