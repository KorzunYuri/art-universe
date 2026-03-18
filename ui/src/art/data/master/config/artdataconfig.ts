import axios from 'axios';
import { setupApiInterceptors } from '@/shared/services/apiSetup';
import { appConfig } from '@/shared/config/appConfig';

const baseApiUrl = `http://${appConfig.artDataHost}:${appConfig.artDataPort}/api/v1`;

const api = axios.create({
    baseURL: baseApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

setupApiInterceptors(api);

export const ArtDataConfig = {
    baseApiUrl,
    api,
};
