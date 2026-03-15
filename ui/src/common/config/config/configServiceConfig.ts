import axios from 'axios';
import { setupTracingInterceptor } from '@/shared/services/tracingInterceptor';
import { appConfig } from '@/shared/config/appConfig';

const api = axios.create({
    baseURL: `http://${appConfig.configServiceHost}:${appConfig.configServicePort}/api/v1/config`,
    headers: { 'Content-Type': 'application/json' },
});

setupTracingInterceptor(api);

export class ConfigServiceConfig {
    static readonly api = api;
}
