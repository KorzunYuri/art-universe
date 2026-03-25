import { useRoutes } from 'react-router-dom'

import { SpotifyHome, SpotifyArtists, SpotifyAlbums, SpotifyTracks, SpotifyGenres } from "./pages";
import { SpotifyArtistDetail } from "./pages/SpotifyArtistDetail";
import { SpotifyAlbumDetail } from "./pages/SpotifyAlbumDetail";
import { SpotifyTrackDetail } from "./pages/SpotifyTrackDetail";
import { TableWithDetailLayout } from "@/shared/components/TableWithDetailLayout";
import { registerSpotifyLookups } from "@/music/data/raw/spotify/services/registerSpotifyLookups.ts";

// Register Spotify lookups on module load
registerSpotifyLookups();

export default function SpotifyApp() {
    const routes = [
        { path: '/',        element: <SpotifyHome /> },
        { path: 'genres',   element: <SpotifyGenres /> },
        {
            path: 'artists',
            element: <TableWithDetailLayout><SpotifyArtists /></TableWithDetailLayout>,
            children: [
                { path: ':artistId', element: <SpotifyArtistDetail /> },
            ],
        },
        {
            path: 'albums',
            element: <TableWithDetailLayout wideDetail><SpotifyAlbums /></TableWithDetailLayout>,
            children: [
                { path: ':albumId', element: <SpotifyAlbumDetail /> },
            ],
        },
        {
            path: 'tracks',
            element: <TableWithDetailLayout><SpotifyTracks /></TableWithDetailLayout>,
            children: [
                { path: ':trackId', element: <SpotifyTrackDetail /> },
            ],
        },
    ]

    return useRoutes(routes)
}
