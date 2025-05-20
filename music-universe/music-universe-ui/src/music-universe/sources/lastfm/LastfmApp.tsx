import { useRoutes } from 'react-router-dom'

import { LastfmHome }       from "./pages/LastfmHome";

import { LastfmTags }       from "./pages/LastfmTags";
import { LastfmArtists }    from "./pages/LastfmArtists";
import { LastfmAlbums }     from "./pages/LastfmAlbums";
import { LastfmTracks }     from "./pages/LastfmTracks";

export default function LastfmApp() {
    const routes = [
        { path: '/',        element: <LastfmHome /> },
        { path: 'tags',     element: <LastfmTags /> },
        { path: 'artists',  element: <LastfmArtists /> },
        { path: 'albums',   element: <LastfmAlbums /> },
        { path: 'tracks',   element: <LastfmTracks /> },
    ]

    return useRoutes(routes)
}