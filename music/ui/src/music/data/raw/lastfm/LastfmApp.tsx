import { useRoutes } from 'react-router-dom'

import { LastfmHome, LastfmTags, LastfmArtists, LastfmAlbums, LastfmTracks, LastfmAdmin } from "./pages";
import { LastfmArtistDetail } from "./pages/LastfmArtistDetail";
import { LastfmAlbumDetail } from "./pages/LastfmAlbumDetail";
import { LastfmTrackDetail } from "./pages/LastfmTrackDetail";
import { TableWithDetailLayout } from "@/music/shared/components/TableWithDetailLayout";
import { registerLastfmLookups } from "@/music/data/raw/lastfm/services/registerLastfmLookups.ts";

// Register LastFM lookups on module load
registerLastfmLookups();

export default function LastfmApp() {
    const routes = [
        { path: '/',        element: <LastfmHome /> },
        { path: 'tags',     element: <LastfmTags /> },
        { path: 'admin',    element: <LastfmAdmin /> },
        {
            path: 'artists',
            element: <TableWithDetailLayout><LastfmArtists /></TableWithDetailLayout>,
            children: [
                { path: ':artistId', element: <LastfmArtistDetail /> },
            ],
        },
        {
            path: 'albums',
            element: <TableWithDetailLayout wideDetail><LastfmAlbums /></TableWithDetailLayout>,
            children: [
                { path: ':albumId', element: <LastfmAlbumDetail /> },
            ],
        },
        {
            path: 'tracks',
            element: <TableWithDetailLayout><LastfmTracks /></TableWithDetailLayout>,
            children: [
                { path: ':trackId', element: <LastfmTrackDetail /> },
            ],
        },
    ]

    return useRoutes(routes)
}
