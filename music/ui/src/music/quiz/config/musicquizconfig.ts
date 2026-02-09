import axios from 'axios';
import { setupTracingInterceptor } from '@/music/shared/services/tracingInterceptor';
import { appConfig } from '@/music/shared/config/appConfig';

const baseApiUrl = `http://${appConfig.musicQuizHost}:${appConfig.musicQuizPort}/api/v1`;

// Create axios instance for Music Quiz API with tracing
const api = axios.create({
    baseURL: baseApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add distributed tracing headers to all Music Quiz API requests
setupTracingInterceptor(api);

export const MusicQuizConfig = {
    baseApiUrl,
    api,
};
