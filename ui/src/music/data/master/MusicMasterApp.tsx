import { useRoutes } from 'react-router-dom'
import { Categories } from './pages/Categories'
import { Artists } from './pages/Artists'
import { Albums } from './pages/Albums'
import { Tracks } from './pages/Tracks'
import { RelationTypes } from './pages/RelationTypes'
import { MusicDataHome } from './pages/MusicDataHome'
import { ArtistDetail } from './pages/ArtistDetail'
import { AlbumDetail } from './pages/AlbumDetail'
import { TrackDetail } from './pages/TrackDetail'
import { RelationTypeDetail } from './pages/RelationTypeDetail'
import { TableWithDetailLayout } from '@/shared/components/TableWithDetailLayout'
import { registerMasterLookups } from "./services/registerMasterLookups.ts";

// Register master entity lookups on module load
registerMasterLookups();

export default function MusicMasterApp() {
    const routes = [
        { path: '/',            element: <MusicDataHome /> },
        { path: 'categories',   element: <Categories /> },
        {
            path: 'artists',
            element: <TableWithDetailLayout><Artists /></TableWithDetailLayout>,
            children: [
                { path: ':artistId', element: <ArtistDetail /> },
            ],
        },
        {
            path: 'albums',
            element: <TableWithDetailLayout><Albums /></TableWithDetailLayout>,
            children: [
                { path: ':albumId', element: <AlbumDetail /> },
            ],
        },
        {
            path: 'tracks',
            element: <TableWithDetailLayout><Tracks /></TableWithDetailLayout>,
            children: [
                { path: ':trackId', element: <TrackDetail /> },
            ],
        },
        {
            path: 'relation-types',
            element: <TableWithDetailLayout><RelationTypes /></TableWithDetailLayout>,
            children: [
                { path: ':relationTypeId', element: <RelationTypeDetail /> },
            ],
        },
    ]

    return useRoutes(routes)
}
