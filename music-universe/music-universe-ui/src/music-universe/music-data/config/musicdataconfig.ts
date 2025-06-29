export const MusicDataConfig = {
    baseApiUrl: `http://${import.meta.env.VITE_MU_DATA_APP_HOST || 'localhost'}:${import.meta.env.VITE_MU_DATA_APP_EXTERNAL_PORT || '8082'}/api/v1`,
};
