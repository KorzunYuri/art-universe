import { useRoutes, Navigate } from 'react-router-dom';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { ConfigProperties } from './pages/ConfigProperties/ConfigProperties';

export default function ConfigApp() {
    const permissions = usePermissions();

    if (permissions.configAccess === 'hidden') {
        return <Navigate to="/" replace />;
    }

    const routes = [
        { path: '/', element: <ConfigProperties /> },
    ];
    return useRoutes(routes);
}
