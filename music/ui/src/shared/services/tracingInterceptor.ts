/**
 * Axios interceptor for adding W3C Trace Context headers to all HTTP requests
 *
 * This interceptor automatically adds distributed tracing headers to enable
 * request tracking across the frontend and backend services.
 */

import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { createTraceparent } from '../utils/tracing';

/**
 * Request interceptor that adds W3C Trace Context headers
 */
function tracingRequestInterceptor(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
  // Generate a new traceparent for each request
  // The backend will extract the trace-id and create child spans
  config.headers.set('traceparent', createTraceparent());

  return config;
}

/**
 * Sets up tracing interceptor on a specific axios instance
 *
 * @param axiosInstance The axios instance to configure
 */
export function setupTracingInterceptor(axiosInstance: AxiosInstance): void {
  axiosInstance.interceptors.request.use(
    tracingRequestInterceptor,
    (error) => Promise.reject(error)
  );
}

/**
 * Sets up tracing interceptor on the default axios instance
 *
 * This will apply to all axios calls that use the default axios import
 */
export function setupGlobalTracingInterceptor(): void {
  setupTracingInterceptor(axios);
}
