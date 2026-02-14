import { useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSearchParams, useNavigate, useParams } from 'react-router-dom';
import { useLastfmEntityTable } from '@/music/data/raw/lastfm/hooks/useLastfmEntityTable';
import { useApprovalStatusFilter } from '@/music/data/raw/shared/hooks';
import { usePlayCountFilter, useListenersCountFilter, useArtistFilter, useTagFilter } from '@/music/data/raw/lastfm/hooks/useLastfmFilters';
import { DataTable } from '@/music/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/music/shared/components/DataTable/sortUtils';
import { ExternalLink, ReadonlyAttr } from '@/music/shared/components';
import { LastfmApprovalCell, LastfmBindingCell, LastfmQuizCell } from '@/music/data/raw/lastfm/components/cells';
import { LastfmArtistFilterButton } from '@/music/data/raw/lastfm/components';
import { LastfmArtistLink } from '@/music/data/raw/lastfm/components/LastfmArtistLink';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig';
import type { LastfmTrack } from '@/music/data/raw/lastfm/types/lastfm-track';
import type { AdditionalSearchConfig } from '@/music/shared/components/EntityTable/types';
import styles from './LastfmTracksTable.module.css';

interface LastfmTracksTableProps {
    artistId?: number;
}

export const LastfmTracksTable = ({ artistId }: LastfmTracksTableProps) => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { trackId } = useParams<{ trackId: string }>();
    const urlArtistId = searchParams.get('artistId') ? parseInt(searchParams.get('artistId')!) : undefined;
    const effectiveArtistId = artistId || urlArtistId;

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
        updateParams,
        refresh,
        handleSearchSubmit: submitSearch,
    } = useLastfmEntityTable('track', { artistId: effectiveArtistId });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minPlayCount, minPlayCountField } = usePlayCountFilter();
    const { minListenersCount, minListenersCountField } = useListenersCountFilter();
    const { artistId: artistIdFilter, artistField, setSelectedArtist } = useArtistFilter();
    const { tagId, tagField } = useTagFilter();

    const handleArtistFilter = useCallback((filteredArtistId: number, artistName: string) => {
        setSelectedArtist({ id: filteredArtistId, name: artistName });
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            artistId: filteredArtistId,
            tagId: tagId || undefined,
        });
    }, [setSelectedArtist, updateParams, approvalStatuses, minPlayCount, minListenersCount, tagId]);

    const handleSearchSubmit = useCallback(() => {
        submitSearch();
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            artistId: artistIdFilter || effectiveArtistId || undefined,
            tagId: tagId || undefined,
        });
    }, [submitSearch, updateParams, approvalStatuses, minPlayCount, minListenersCount, artistIdFilter, effectiveArtistId, tagId]);

    const columns = useMemo<ColumnDef<LastfmTrack, any>[]>(() => [
        {
            id: 'artist',
            header: 'Artist',
            enableSorting: false,
            meta: { headerClassName: styles.artist, cellClassName: styles.artist },
            cell: ({ row }) => {
                const track = row.original;
                if (!track.artist) return null;
                return (
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <LastfmArtistFilterButton
                            artistId={track.artist.id}
                            artistName={track.artist.name}
                            onFilter={handleArtistFilter}
                            targetPage="tracks"
                        />
                        <LastfmArtistLink artistName={track.artist.name} />
                    </div>
                );
            },
        },
        {
            accessorKey: 'name',
            header: 'Track name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => {
                const track = row.original;
                return track.url ? <ExternalLink href={track.url} label={track.name} /> : <span>{track.name}</span>;
            },
        },
        {
            id: 'mbid',
            header: 'MusicBrainz',
            enableSorting: false,
            meta: { headerClassName: styles.mbid, cellClassName: styles.mbid },
            cell: ({ row }) => {
                const track = row.original;
                return track.mbid ? (
                    <ExternalLink href={`${LastfmConfig.mbBaseUrls.track}${track.mbid}`} label="MusicBrainz" />
                ) : null;
            },
        },
        {
            id: 'status',
            header: 'Approval',
            enableSorting: false,
            meta: { headerClassName: styles.status, cellClassName: styles.status },
            cell: ({ row }) => (
                <LastfmApprovalCell entityType="track" entityId={row.original.id} />
            ),
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <LastfmBindingCell entityType="track" entityId={row.original.id} />
            ),
        },
        {
            id: 'quizBinding',
            header: 'Quiz',
            enableSorting: false,
            meta: { headerClassName: styles.quizBinding, cellClassName: styles.quizBinding },
            cell: ({ row }) => (
                <LastfmQuizCell entityType="track" entityId={row.original.id} />
            ),
        },
        {
            accessorKey: 'playCount',
            header: 'Plays',
            enableSorting: true,
            meta: { headerClassName: styles.count, cellClassName: styles.count },
            cell: ({ row }) => <ReadonlyAttr value={row.original.playCount} />,
        },
        {
            accessorKey: 'listenersCount',
            header: 'Listeners',
            enableSorting: true,
            meta: { headerClassName: styles.count, cellClassName: styles.count },
            cell: ({ row }) => <ReadonlyAttr value={row.original.listenersCount} />,
        },
    ], [handleArtistFilter]);

    const sorting = useMemo(() => stringToSortingState(sort), [sort]);
    const handleSortingChange = useCallback(
        (updater: any) => {
            const next = typeof updater === 'function' ? updater(sorting) : updater;
            setSort(sortingStateToString(next));
        },
        [sorting, setSort],
    );

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: 'Advanced Filters',
        collapsible: true,
        defaultCollapsed: true,
        fields: [minPlayCountField, minListenersCountField, artistField, tagField, approvalStatusField],
    };

    const handleRowClick = useCallback((track: LastfmTrack) => {
        if (trackId && Number(trackId) === track.id) {
            navigate('.', { relative: 'path' });
        } else {
            navigate(String(track.id));
        }
    }, [navigate, trackId]);

    return (
        <DataTable<LastfmTrack>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search track name..."
            additionalSearch={additionalSearchConfig}
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
