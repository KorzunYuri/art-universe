import type { AxiosInstance } from 'axios';
import { setupTracingInterceptor } from './tracingInterceptor';
import { setupAuthInterceptor } from './auth/authInterceptor';
import { clearTokens } from './auth/tokenStorage';

function onAuthFailure() {
    clearTokens();
    window.location.reload();
}

/**
 * Configures an axios instance with tracing and auth interceptors.
 */
export function setupApiInterceptors(axiosInstance: AxiosInstance): void {
    setupTracingInterceptor(axiosInstance);
    setupAuthInterceptor(axiosInstance, onAuthFailure);
}
