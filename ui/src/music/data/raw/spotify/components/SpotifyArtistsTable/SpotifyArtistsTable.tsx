import { useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSearchParams, useNavigate, useParams } from 'react-router-dom';
import { useSpotifyEntityTable } from '@/music/data/raw/spotify/hooks/useSpotifyEntityTable';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import { ExternalLink } from '@/shared/components';
import { SpotifyBindingCell } from '@/music/data/raw/spotify/components/cells/SpotifyBindingCell';
import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig';
import type { SpotifyArtist } from '@/music/data/raw/spotify/types/spotify-artist';
import styles from './SpotifyArtistsTable.module.css';

export const SpotifyArtistsTable = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { artistId } = useParams<{ artistId: string }>();
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
    } = useSpotifyEntityTable('artist', { search: initialSearch });

    const columns = useMemo<ColumnDef<SpotifyArtist, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Artist name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => {
                const artist = row.original;
                const spotifyUrl = artist.spotifyUrl || `${SpotifyConfig.spotifyBaseUrls.artist}${artist.spotifyId}`;
                return <ExternalLink href={spotifyUrl} label={artist.name} />;
            },
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <SpotifyBindingCell entityType="artist" entityId={row.original.id} />
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

    const handleRowClick = useCallback((artist: SpotifyArtist) => {
        if (artistId && Number(artistId) === artist.id) {
            navigate('.', { relative: 'path' });
        } else {
            navigate(String(artist.id));
        }
    }, [navigate, artistId]);

    return (
        <DataTable<SpotifyArtist>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search artist name..."
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
            activeRowId={artistId ? Number(artistId) : null}
            emptyMessage="No artists found"
        />
    );
};
