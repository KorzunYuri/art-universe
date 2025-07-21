import { useRoutes } from 'react-router-dom'

import { LastfmHome, LastfmTags, LastfmArtists, LastfmAlbums, LastfmTracks } from "./pages";
import {QueryProvider} from "@/music-universe/shared/providers/QueryProvider.tsx";

export default function LastfmApp() {
    const routes = [
        { path: '/',        element: <LastfmHome /> },
        { path: 'tags',     element: <LastfmTags /> },
        { path: 'artists',  element: <LastfmArtists /> },
        { path: 'albums',   element: <LastfmAlbums /> },
        { path: 'tracks',   element: <LastfmTracks /> },
    ]

    return <QueryProvider>
        { useRoutes(routes) }
    </QueryProvider>
}