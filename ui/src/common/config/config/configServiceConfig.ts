import axios from 'axios';
import { setupApiInterceptors } from '@/shared/services/apiSetup';
import { appConfig } from '@/shared/config/appConfig';

const api = axios.create({
    baseURL: `http://${appConfig.configServiceHost}:${appConfig.configServicePort}/api/v1/config`,
    headers: { 'Content-Type': 'application/json' },
});

setupApiInterceptors(api);

export class ConfigServiceConfig {
    static readonly api = api;
}
