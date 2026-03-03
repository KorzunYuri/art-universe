import { useRoutes } from 'react-router-dom';
import { ArtDataHome } from './pages/ArtDataHome/ArtDataHome';
import { Persons } from './pages/Persons';
import { PersonDetail } from './pages/PersonDetail';
import { TableWithDetailLayout } from '@/shared/components/TableWithDetailLayout';
import { registerArtLookups } from './services/registerArtLookups';

// Register art entity lookups on module load
registerArtLookups();

export default function ArtMasterApp() {
    const routes = [
        { path: '/', element: <ArtDataHome /> },
        {
            path: 'persons',
            element: <TableWithDetailLayout><Persons /></TableWithDetailLayout>,
            children: [
                { path: ':personId', element: <PersonDetail /> },
            ],
        },
    ];

    return useRoutes(routes);
}
