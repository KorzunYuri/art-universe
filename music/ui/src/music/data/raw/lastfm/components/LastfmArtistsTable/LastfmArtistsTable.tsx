import { useMemo, useCallback, useState } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSearchParams, useNavigate, useParams } from 'react-router-dom';
import { useLastfmEntityTable } from '@/music/data/raw/lastfm/hooks/useLastfmEntityTable';
import { useApprovalStatusFilter } from '@/music/data/raw/shared/hooks';
import { usePlayCountFilter, useListenersCountFilter, useTagFilter } from '@/music/data/raw/lastfm/hooks/useLastfmFilters';
import { useNotifications } from '@/music/shared/hooks';
import { DataTable } from '@/music/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/music/shared/components/DataTable/sortUtils';
import { ExternalLink, ReadonlyAttr } from '@/music/shared/components';
import { LastfmApprovalCell, LastfmBindingCell, LastfmQuizCell } from '@/music/data/raw/lastfm/components/cells';
import { LastfmArtistFilterButton } from '@/music/data/raw/lastfm/components';
import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig';
import { searchArtist } from '@/music/data/raw/lastfm/api/lastfm-artists';
import type { LastfmArtist } from '@/music/data/raw/lastfm/types/lastfm-artist';
import type { AdditionalSearchConfig } from '@/music/shared/components/EntityTable/types';
import styles from './LastfmArtistsTable.module.css';

export const LastfmArtistsTable = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { artistId } = useParams<{ artistId: string }>();
    const initialSearch = searchParams.get('search') || '';
    const [isSearching, setIsSearching] = useState(false);
    const { showNotification } = useNotifications();

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
        handleSearchSubmit: submitSearch,
        updateParams,
        refresh,
    } = useLastfmEntityTable('artist', { search: initialSearch });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minPlayCount, minPlayCountField } = usePlayCountFilter();
    const { minListenersCount, minListenersCountField } = useListenersCountFilter();
    const { tagId, tagField } = useTagFilter();

    const handleSearchSubmit = useCallback(() => {
        submitSearch();
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            tagId: tagId || undefined,
        });
    }, [submitSearch, updateParams, approvalStatuses, minPlayCount, minListenersCount, tagId]);

    const handleForcedSearch = async () => {
        if (!search.trim()) return;
        setIsSearching(true);
        try {
            await searchArtist(search);
            showNotification('success', `Forced search requested for: ${search}`);
            refresh();
        } catch (error) {
            console.error('Failed to trigger forced search:', error);
            showNotification('error', 'Failed to trigger forced search');
        } finally {
            setIsSearching(false);
        }
    };

    const columns = useMemo<ColumnDef<LastfmArtist, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Artist name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => {
                const artist = row.original;
                return (
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <LastfmArtistFilterButton
                            artistId={artist.id}
                            artistName={artist.name}
                            targetPage="tracks"
                        />
                        {artist.url && <ExternalLink href={artist.url} label={artist.name} />}
                    </div>
                );
            },
        },
        {
            id: 'mbid',
            header: 'MusicBrainz',
            enableSorting: false,
            meta: { headerClassName: styles.mbid, cellClassName: styles.mbid },
            cell: ({ row }) => {
                const artist = row.original;
                return artist.mbid ? (
                    <ExternalLink href={`${LastfmConfig.mbBaseUrls.artist}${artist.mbid}`} label="MusicBrainz" />
                ) : null;
            },
        },
        {
            id: 'status',
            header: 'Approval',
            enableSorting: false,
            meta: { headerClassName: styles.status, cellClassName: styles.status },
            cell: ({ row }) => (
                <LastfmApprovalCell entityType="artist" entityId={row.original.id} />
            ),
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <LastfmBindingCell entityType="artist" entityId={row.original.id} />
            ),
        },
        {
            id: 'quizBinding',
            header: 'Quiz',
            enableSorting: false,
            meta: { headerClassName: styles.quizBinding, cellClassName: styles.quizBinding },
            cell: ({ row }) => (
                <LastfmQuizCell entityType="artist" entityId={row.original.id} />
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
    ], []);

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
        fields: [minPlayCountField, minListenersCountField, tagField, approvalStatusField],
    };

    const handleRowClick = useCallback((artist: LastfmArtist) => {
        if (artistId && Number(artistId) === artist.id) {
            navigate('.', { relative: 'path' });
        } else {
            navigate(String(artist.id));
        }
    }, [navigate, artistId]);

    return (
        <DataTable<LastfmArtist>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search artist name..."
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
            activeRowId={artistId ? Number(artistId) : null}
            emptyMessage="No artists found"
            extraActions={
                <button
                    onClick={handleForcedSearch}
                    disabled={!search.trim() || isSearching || isLoading}
                    style={{ padding: '0.5rem 1rem', border: '1px solid #ccc', borderRadius: '4px', backgroundColor: '#fff', cursor: 'pointer', whiteSpace: 'nowrap' }}
                >
                    {isSearching ? 'Searching...' : 'Force Search'}
                </button>
            }
        />
    );
};
