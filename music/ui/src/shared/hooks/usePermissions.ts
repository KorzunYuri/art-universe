import { useContext } from 'react';
import { AuthContext, type Permissions } from '../contexts/AuthContext';

export function usePermissions(): Permissions {
    const permissions = useContext(AuthContext);
    if (!permissions) {
        throw new Error('usePermissions must be used within an AuthProvider');
    }
    return permissions;
}
