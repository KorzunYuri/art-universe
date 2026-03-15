import { useRoutes } from 'react-router-dom';
import { ConfigProperties } from './pages/ConfigProperties/ConfigProperties';

export default function ConfigApp() {
    const routes = [
        { path: '/', element: <ConfigProperties /> },
    ];
    return useRoutes(routes);
}
