import { useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSearchParams, useNavigate, useParams, Link } from 'react-router-dom';
import { useSpotifyEntityTable } from '@/music/data/raw/spotify/hooks/useSpotifyEntityTable';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import { ExternalLink, ReadonlyAttr } from '@/shared/components';
import { SpotifyArtistRelatedBindingCell } from '@/music/data/raw/spotify/components/cells/SpotifyArtistRelatedBindingCell';
import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig';
import type { SpotifyAlbum } from '@/music/data/raw/spotify/types/spotify-album';
import styles from './SpotifyAlbumsTable.module.css';

export const SpotifyAlbumsTable = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { albumId } = useParams<{ albumId: string }>();
    const initialSearch = searchParams.get('search') || '';

    const {
        rawEntities,
        pagination,
        search,
        sort,
        isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        handleSearchSubmit,
        refresh,
    } = useSpotifyEntityTable('album', { search: initialSearch });

    const columns = useMemo<ColumnDef<SpotifyAlbum, any>[]>(() => [
        {
            id: 'artist',
            header: 'Artist',
            enableSorting: false,
            meta: { headerClassName: styles.artist, cellClassName: styles.artist },
            cell: ({ row }) => {
                const album = row.original;
                if (album.primaryArtistId && album.primaryArtistName) {
                    return <Link to={`/music/data/raw/spotify/artists/${album.primaryArtistId}`}>{album.primaryArtistName}</Link>;
                }
                return <ReadonlyAttr value={album.primaryArtistName} />;
            },
        },
        {
            accessorKey: 'name',
            header: 'Album name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => {
                const album = row.original;
                const spotifyUrl = album.spotifyUrl || `${SpotifyConfig.spotifyBaseUrls.album}${album.spotifyId}`;
                return <ExternalLink href={spotifyUrl} label={album.name} />;
            },
        },
        {
            accessorKey: 'totalTracks',
            header: 'Tracks',
            enableSorting: false,
            meta: { headerClassName: styles.info, cellClassName: styles.info },
            cell: ({ row }) => <ReadonlyAttr value={row.original.totalTracks} />,
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <SpotifyArtistRelatedBindingCell entityType="album" entityId={row.original.id} />
            ),
        },
    ], []);

    const sorting = useMemo(() => stringToSortingState(sort), [sort]);
    const handleSortingChange = useCallback(
        (updater: any) => {
            const next = typeof updater === 'function' ? updater(sorting) : updater;
            setSort(sortingStateToString(next));
        },
        [sorting, setSort],
    );

    const handleRowClick = useCallback((album: SpotifyAlbum) => {
        if (albumId && Number(albumId) === album.id) {
            navigate('.', { relative: 'path' });
        } else {
            navigate(String(album.id));
        }
    }, [navigate, albumId]);

    return (
        <DataTable<SpotifyAlbum>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search album name..."
            sorting={sorting}
            onSortingChange={handleSortingChange}
            pagination={pagination}
            onNextPage={nextPage}
            onPrevPage={prevPage}
            onGoToPage={goToPage}
            isLoading={isLoading}
            onRefresh={refresh}
            getRowId={(row) => String(row.id)}
            onRowClick={handleRowClick}
            activeRowId={albumId ? Number(albumId) : null}
            emptyMessage="No albums found"
        />
    );
};
