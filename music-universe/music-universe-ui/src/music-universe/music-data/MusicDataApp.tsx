import { useRoutes } from 'react-router-dom'
import { Categories } from './pages/Categories'
import { MusicDataHome } from './pages/MusicDataHome'
import { QueryProvider } from "@/music-universe/shared/providers/QueryProvider.tsx";
import { registerMasterLookups } from "./services/registerMasterLookups";
import {NotificationProvider} from "@/music-universe/shared/providers/NotificationProvider.tsx";
import {NotificationContainer} from "@/music-universe/shared/components/NotificationContainer";

// Register master entity lookups on module load
registerMasterLookups();

export default function MusicDataApp() {
    const routes = [
        { path: '/',            element: <MusicDataHome /> },
        { path: 'categories',   element: <Categories /> },
    ]

    return (
        <QueryProvider>
            <NotificationProvider>
                { useRoutes(routes) }
                <NotificationContainer />
            </NotificationProvider>
        </QueryProvider>
    )
}
