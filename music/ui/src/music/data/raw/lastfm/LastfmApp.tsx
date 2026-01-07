import { useRoutes } from 'react-router-dom'

import { LastfmHome, LastfmTags, LastfmArtists, LastfmAlbums, LastfmTracks } from "./pages";
import {QueryProvider} from "@/music/shared/providers/QueryProvider.tsx";
import { NotificationProvider } from "@/music/shared/providers/NotificationProvider.tsx";
import { NotificationContainer } from "@/music/shared/components/NotificationContainer/NotificationContainer.tsx";
import { registerLastfmLookups } from "@/music/data/raw/lastfm/services/registerLastfmLookups.ts";

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