import { useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSearchParams } from 'react-router-dom';
import { useSpotifyEntityTable } from '@/music/data/raw/spotify/hooks/useSpotifyEntityTable';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import { ReadonlyAttr } from '@/shared/components';
import { SpotifyBindingCell } from '@/music/data/raw/spotify/components/cells/SpotifyBindingCell';
import type { SpotifyGenre } from '@/music/data/raw/spotify/types/spotify-genre';
import styles from './SpotifyGenresTable.module.css';

export const SpotifyGenresTable = () => {
    const [searchParams] = useSearchParams();
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
    } = useSpotifyEntityTable('category', { search: initialSearch });

    const columns = useMemo<ColumnDef<SpotifyGenre, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Genre name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => <ReadonlyAttr value={row.original.name} />,
        },
        {
            accessorKey: 'spotifyId',
            header: 'Spotify ID',
            enableSorting: false,
            meta: { headerClassName: styles.spotifyId, cellClassName: styles.spotifyId },
            cell: ({ row }) => <ReadonlyAttr value={row.original.spotifyId} />,
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <SpotifyBindingCell entityType="category" entityId={row.original.id} />
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

    return (
        <DataTable<SpotifyGenre>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search genre name..."
            sorting={sorting}
            onSortingChange={handleSortingChange}
            pagination={pagination}
            onNextPage={nextPage}
            onPrevPage={prevPage}
            onGoToPage={goToPage}
            isLoading={isLoading}
            onRefresh={refresh}
            getRowId={(row) => String(row.id)}
            emptyMessage="No genres found"
        />
    );
};
