import type { ReactNode } from 'react';
import { AuthContext, type Permissions } from '../contexts/AuthContext';

/**
 * Mock permissions that grant full access.
 * Replace with real implementation when user auth is added.
 */
const mockPermissions: Permissions = {
    isAdmin: () => true,
    canEditMasterEntity: () => true,
    canDeleteMasterEntity: () => true,
    canCreateMasterEntity: () => true,
    canBindRawEntity: () => true,
    canEditRawEntityApproval: () => true,
    canEditQuizBinding: () => true,
};

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
    return (
        <AuthContext.Provider value={mockPermissions}>
            {children}
        </AuthContext.Provider>
    );
};
