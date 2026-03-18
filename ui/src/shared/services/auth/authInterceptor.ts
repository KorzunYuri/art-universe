import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from './tokenStorage';
import { refreshTokens } from './authApi';

let isRefreshing = false;
let refreshSubscribers: Array<(token: string) => void> = [];

function onTokenRefreshed(newToken: string) {
    refreshSubscribers.forEach((cb) => cb(newToken));
    refreshSubscribers = [];
}

function addRefreshSubscriber(cb: (token: string) => void) {
    refreshSubscribers.push(cb);
}

/**
 * Attaches JWT Bearer token to all outgoing requests and
 * handles transparent token refresh on 401 responses.
 */
export function setupAuthInterceptor(
    axiosInstance: AxiosInstance,
    onAuthFailure: () => void,
): void {
    // Request: attach access token
    axiosInstance.interceptors.request.use(
        (config: InternalAxiosRequestConfig) => {
            const token = getAccessToken();
            if (token) {
                config.headers.set('Authorization', `Bearer ${token}`);
            }
            return config;
        },
        (error) => Promise.reject(error),
    );

    // Response: handle 401 with token refresh
    axiosInstance.interceptors.response.use(
        (response) => response,
        async (error) => {
            const originalRequest = error.config;

            if (error.response?.status !== 401 || originalRequest._retry) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            if (isRefreshing) {
                return new Promise((resolve) => {
                    addRefreshSubscriber((newToken: string) => {
                        originalRequest.headers.Authorization = `Bearer ${newToken}`;
                        resolve(axiosInstance(originalRequest));
                    });
                });
            }

            isRefreshing = true;

            try {
                const currentRefreshToken = getRefreshToken();
                if (!currentRefreshToken) {
                    throw new Error('No refresh token');
                }

                const response = await refreshTokens(currentRefreshToken);
                setTokens(response.accessToken, response.refreshToken);
                onTokenRefreshed(response.accessToken);

                originalRequest.headers.Authorization = `Bearer ${response.accessToken}`;
                return axiosInstance(originalRequest);
            } catch {
                clearTokens();
                onAuthFailure();
                return Promise.reject(error);
            } finally {
                isRefreshing = false;
            }
        },
    );
}
