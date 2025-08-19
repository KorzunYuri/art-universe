import { useRoutes } from 'react-router-dom'
import { Categories } from './pages/Categories'
import { Dimensions } from './pages/Dimensions'
import { MusicDataHome } from './pages/MusicDataHome'
import { QueryProvider } from "@/music-universe/shared/providers/QueryProvider.tsx";
import { registerMasterLookups } from "./services/registerMasterLookups";

// Register master entity lookups on module load
registerMasterLookups();

export default function MusicDataApp() {
    const routes = [
        { path: '/',            element: <MusicDataHome /> },
        { path: 'categories',   element: <Categories /> },
        { path: 'dimensions',   element: <Dimensions /> },
    ]

    return <QueryProvider>
        { useRoutes(routes) }
    </QueryProvider>
}
