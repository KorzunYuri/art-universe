import { useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSearchParams, useNavigate, useParams, Link } from 'react-router-dom';
import { useSpotifyEntityTable } from '@/music/data/raw/spotify/hooks/useSpotifyEntityTable';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import { ExternalLink, ReadonlyAttr } from '@/shared/components';
import { SpotifyArtistRelatedBindingCell } from '@/music/data/raw/spotify/components/cells/SpotifyArtistRelatedBindingCell';
import { SpotifyConfig } from '@/music/data/raw/spotify/config/spotifyconfig';
import type { SpotifyTrack } from '@/music/data/raw/spotify/types/spotify-track';
import styles from './SpotifyTracksTable.module.css';

function formatDuration(ms: number | null): string {
    if (ms === null) return '';
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

export const SpotifyTracksTable = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { trackId } = useParams<{ trackId: string }>();
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
    } = useSpotifyEntityTable('track', { search: initialSearch });

    const columns = useMemo<ColumnDef<SpotifyTrack, any>[]>(() => [
        {
            id: 'artist',
            header: 'Artist',
            enableSorting: false,
            meta: { headerClassName: styles.artist, cellClassName: styles.artist },
            cell: ({ row }) => {
                const track = row.original;
                if (track.primaryArtistId && track.primaryArtistName) {
                    return <Link to={`/music/data/raw/spotify/artists/${track.primaryArtistId}`}>{track.primaryArtistName}</Link>;
                }
                return <ReadonlyAttr value={track.primaryArtistName} />;
            },
        },
        {
            accessorKey: 'name',
            header: 'Track name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => {
                const track = row.original;
                const spotifyUrl = track.spotifyUrl || `${SpotifyConfig.spotifyBaseUrls.track}${track.spotifyId}`;
                return <ExternalLink href={spotifyUrl} label={track.name} />;
            },
        },
        {
            accessorKey: 'durationMs',
            header: 'Duration',
            enableSorting: false,
            meta: { headerClassName: styles.duration, cellClassName: styles.duration },
            cell: ({ row }) => formatDuration(row.original.durationMs),
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <SpotifyArtistRelatedBindingCell entityType="track" entityId={row.original.id} />
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

    const handleRowClick = useCallback((track: SpotifyTrack) => {
        if (trackId && Number(trackId) === track.id) {
            navigate('.', { relative: 'path' });
        } else {
            navigate(String(track.id));
        }
    }, [navigate, trackId]);

    return (
        <DataTable<SpotifyTrack>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search track name..."
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
            activeRowId={trackId ? Number(trackId) : null}
            emptyMessage="No tracks found"
        />
    );
};
