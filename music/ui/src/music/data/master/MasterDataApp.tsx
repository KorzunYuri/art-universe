import { useRoutes } from 'react-router-dom'
import { Categories } from './pages/Categories'
import { Artists } from './pages/Artists'
import { Albums } from './pages/Albums'
import { Tracks } from './pages/Tracks'
import { MusicDataHome } from './pages/MusicDataHome'
import { QueryProvider } from "@/music/shared/providers/QueryProvider.tsx";
import { registerMasterLookups } from "./services/registerMasterLookups.ts";
import {NotificationProvider} from "@/music/shared/providers/NotificationProvider.tsx";
import {NotificationContainer} from "@/music/shared/components/NotificationContainer";

// Register master entity lookups on module load
registerMasterLookups();

export default function MasterDataApp() {
    const routes = [
        { path: '/',            element: <MusicDataHome /> },
        { path: 'categories',   element: <Categories /> },
        { path: 'artists',      element: <Artists /> },
        { path: 'albums',       element: <Albums /> },
        { path: 'tracks',       element: <Tracks /> },
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
