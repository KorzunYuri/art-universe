import { createContext } from 'react';
import type { UserInfo, Role } from '@/shared/types/auth';

export interface AuthState {
    user: UserInfo | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (googleIdToken: string) => Promise<void>;
    logout: () => Promise<void>;
    hasRole: (role: Role) => boolean;
}

export interface Permissions {
    isAdmin(): boolean;
    canEditMasterEntity(): boolean;
    canDeleteMasterEntity(): boolean;
    canCreateMasterEntity(): boolean;
    canBindRawEntity(): boolean;
    canEditRawEntityApproval(): boolean;
    canEditQuizBinding(): boolean;
}

export const AuthContext = createContext<AuthState | null>(null);
