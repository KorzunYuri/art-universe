import axios from 'axios';
import { setupApiInterceptors } from '@/shared/services/apiSetup';
import { appConfig } from '@/shared/config/appConfig';

const baseApiUrl = `http://${appConfig.musicQuizHost}:${appConfig.musicQuizPort}/api/v1`;

const api = axios.create({
    baseURL: baseApiUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

setupApiInterceptors(api);

export const MusicQuizConfig = {
    baseApiUrl,
    api,
};
