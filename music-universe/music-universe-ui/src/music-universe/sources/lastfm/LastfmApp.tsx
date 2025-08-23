import { useRoutes } from 'react-router-dom'

import { LastfmHome, LastfmTags, LastfmArtists, LastfmAlbums, LastfmTracks } from "./pages";
import {QueryProvider} from "@/music-universe/shared/providers/QueryProvider.tsx";
import { NotificationProvider } from "@/music-universe/shared/providers/NotificationProvider.tsx";
import { NotificationContainer } from "@/music-universe/shared/components/NotificationContainer/NotificationContainer";
import { registerLastfmLookups } from "./services/registerLastfmLookups";

// Register LastFM lookups on module load
registerLastfmLookups();

export default function LastfmApp() {
    const routes = [
        { path: '/',        element: <LastfmHome /> },
        { path: 'tags',     element: <LastfmTags /> },
        { path: 'artists',  element: <LastfmArtists /> },
        { path: 'albums',   element: <LastfmAlbums /> },
        { path: 'tracks',   element: <LastfmTracks /> },
    ]

    return (
        <QueryProvider>
            <NotificationProvider>
                {useRoutes(routes)}
                <NotificationContainer />
            </NotificationProvider>
        </QueryProvider>
    );
}