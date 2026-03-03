import axios from 'axios';
import { setupTracingInterceptor } from '@/shared/services/tracingInterceptor';
import { appConfig } from '@/shared/config/appConfig';

const baseApiUrl = `http://${appConfig.artDataHost}:${appConfig.artDataPort}/api/v1`;

// Create axios instance for Art Data API with tracing
const api = axios.create({
    baseURL: baseApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add distributed tracing headers to all Art Data API requests
setupTracingInterceptor(api);

export const ArtDataConfig = {
    baseApiUrl,
    api,
};
