import { useContext } from 'react';
import { AuthContext, type Permissions } from '../contexts/AuthContext';
import type { Role } from '@/shared/types/auth';

function createPermissions(roles: Role[]): Permissions {
    const roleSet = new Set(roles);
    const isAdmin = () => roleSet.has('ADMIN');

    return {
        isAdmin,
        canEditMasterEntity: () => isAdmin() || roleSet.has('MASTER_CURATOR'),
        canDeleteMasterEntity: () => isAdmin() || roleSet.has('MASTER_CURATOR'),
        canCreateMasterEntity: () => isAdmin() || roleSet.has('MASTER_CURATOR'),
        canBindRawEntity: () => isAdmin() || roleSet.has('LASTFM_CURATOR') || roleSet.has('SPOTIFY_CURATOR'),
        canEditRawEntityApproval: () => isAdmin() || roleSet.has('LASTFM_CURATOR') || roleSet.has('SPOTIFY_CURATOR'),
        canEditQuizBinding: () => isAdmin() || roleSet.has('QUIZ_MASTER'),
        masterEntityAccess: isAdmin() || roleSet.has('MASTER_CURATOR') ? 'full' : 'readOnly',
        lastfmCurationAccess: isAdmin() || roleSet.has('LASTFM_CURATOR') ? 'full' : 'readOnly',
        spotifyCurationAccess: isAdmin() || roleSet.has('SPOTIFY_CURATOR') ? 'full' : 'readOnly',
        quizAccess: isAdmin() || roleSet.has('QUIZ_MASTER') ? 'full' : 'hidden',
        configAccess: isAdmin() || roleSet.has('CONFIG_MANAGER') ? 'full' : 'hidden',
        adminAccess: isAdmin() ? 'full' : 'hidden',
    };
}

export function usePermissions(): Permissions {
    const auth = useContext(AuthContext);
    if (!auth) {
        throw new Error('usePermissions must be used within an AuthProvider');
    }
    return createPermissions(auth.user?.roles ?? []);
}
