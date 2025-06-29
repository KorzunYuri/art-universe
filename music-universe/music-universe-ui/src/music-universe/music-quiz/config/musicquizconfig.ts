export const MusicQuizConfig = {
    baseApiUrl: `http://${import.meta.env.VITE_MU_QUIZ_APP_HOST || 'localhost'}:${import.meta.env.VITE_MU_QUIZ_APP_PORT || '8083'}/api/v1`,
};
