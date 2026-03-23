import axios from 'axios';
import { appConfig } from '@/shared/config/appConfig';
import type { AuthResponse, UserInfo } from '@/shared/types/auth';

const authApi = axios.create({
    baseURL: `http://${appConfig.authServiceHost}:${appConfig.authServicePort}/api/v1/auth`,
    headers: { 'Content-Type': 'application/json' },
});

export async function loginWithGoogle(googleIdToken: string): Promise<AuthResponse> {
    const { data } = await authApi.post<AuthResponse>('/login', { googleIdToken });
    return data;
}

export async function refreshTokens(refreshToken: string): Promise<AuthResponse> {
    const { data } = await authApi.post<AuthResponse>('/refresh', { refreshToken });
    return data;
}

export async function logout(refreshToken: string): Promise<void> {
    await authApi.post('/logout', { refreshToken });
}

export async function fetchCurrentUser(accessToken: string): Promise<UserInfo> {
    const { data } = await authApi.get<UserInfo>('/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
    });
    return data;
}
