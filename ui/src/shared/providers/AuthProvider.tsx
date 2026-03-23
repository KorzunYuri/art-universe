import { type ReactNode, useState, useEffect, useCallback } from 'react';
import { AuthContext, type AuthState } from '../contexts/AuthContext';
import type { UserInfo, Role } from '@/shared/types/auth';
import {
    loginWithGoogle,
    logout as logoutApi,
    fetchCurrentUser,
    getAccessToken,
    getRefreshToken,
    setTokens,
    clearTokens,
} from '@/shared/services/auth';

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
    const [user, setUser] = useState<UserInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    // On mount, try to restore session from stored tokens
    useEffect(() => {
        const token = getAccessToken();
        if (token) {
            fetchCurrentUser(token)
                .then(setUser)
                .catch(() => clearTokens())
                .finally(() => setIsLoading(false));
        } else {
            setIsLoading(false);
        }
    }, []);

    const login = useCallback(async (googleIdToken: string) => {
        const response = await loginWithGoogle(googleIdToken);
        setTokens(response.accessToken, response.refreshToken);
        setUser(response.user);
    }, []);

    const logout = useCallback(async () => {
        const refreshToken = getRefreshToken();
        if (refreshToken) {
            try {
                await logoutApi(refreshToken);
            } catch {
                // Ignore logout API errors — clear local state regardless
            }
        }
        clearTokens();
        setUser(null);
    }, []);

    const hasRole = useCallback(
        (role: Role) => user?.roles.includes(role) ?? false,
        [user],
    );

    const authState: AuthState = {
        user,
        isAuthenticated: user !== null,
        isLoading,
        login,
        logout,
        hasRole,
    };

    return (
        <AuthContext.Provider value={authState}>
            {children}
        </AuthContext.Provider>
    );
};
