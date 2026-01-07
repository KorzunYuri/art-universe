import axios from 'axios';
import { setupTracingInterceptor } from '@/music/shared/services/tracingInterceptor';

const baseApiUrl = `http://${import.meta.env.VITE_MU_QUIZ_APP_HOST || 'localhost'}:${import.meta.env.VITE_MU_QUIZ_APP_EXTERNAL_PORT || '8083'}/api/v1`;

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
