import { createContext } from 'react';
import type { UserInfo, Role } from '@/shared/types/auth';
import type { AccessLevel } from '@/shared/types/access';

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
    masterEntityAccess: AccessLevel;
    lastfmCurationAccess: AccessLevel;
    spotifyCurationAccess: AccessLevel;
    quizAccess: AccessLevel;
    configAccess: AccessLevel;
    adminAccess: AccessLevel;
}

export const AuthContext = createContext<AuthState | null>(null);
