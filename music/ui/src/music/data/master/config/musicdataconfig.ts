import axios from 'axios';
import { setupTracingInterceptor } from '@/music/shared/services/tracingInterceptor';

const baseApiUrl = `http://${import.meta.env.VITE_MU_DATA_APP_HOST || 'localhost'}:${import.meta.env.VITE_MU_DATA_APP_EXTERNAL_PORT || '8082'}/api/v1`;

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
