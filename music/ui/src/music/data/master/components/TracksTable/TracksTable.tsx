import { useState, useMemo, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { ColumnDef } from '@tanstack/react-table';
import { useMasterEntityTable } from '@/music/data/master/hooks/useMasterEntityTable';
import { useNotifications } from '@/shared/hooks';
import { useColumnPreferences, type ColumnConfig } from '@/shared/hooks/useColumnPreferences';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import { ColumnToggle } from '@/shared/components/ColumnToggle';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import type { Track } from '@/music/shared/types/entities';
import { NameCell } from './cells/NameCell';
import { PrimaryArtistCell } from './cells/PrimaryArtistCell';
import { CategoriesCell } from './cells/CategoriesCell';
import { AddCategoryCell } from './cells/AddCategoryCell';
import { ActionsCell } from './cells/ActionsCell';
import { createTrack } from '@/music/data/master/api/music-data-tracks';
import styles from './TracksTable.module.css';

const CONFIGURABLE_COLUMNS: ColumnConfig[] = [
    { id: 'quiz', label: 'Quiz', defaultVisible: false },
    { id: 'attributes', label: 'Attributes', defaultVisible: false },
];

export const TracksTable = () => {
    const navigate = useNavigate();
    const { trackId } = useParams<{ trackId: string }>();
    const { showNotification } = useNotifications();
    const [newTrackName, setNewTrackName] = useState('');
    const [selectedArtistId, setSelectedArtistId] = useState<number | null>(null);
    const [selectedArtistName, setSelectedArtistName] = useState<string>('');
    const [isCreating, setIsCreating] = useState(false);

    const {
        entities,
        pagination,
        search,
        sort,
        isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        refresh,
    } = useMasterEntityTable('track');

    const {
        columnVisibility,
        onColumnVisibilityChange,
        configurableColumns,
        toggleColumn,
        resetToDefaults,
    } = useColumnPreferences('master-tracks', CONFIGURABLE_COLUMNS);

    const handleCreateTrack = async () => {
        if (!newTrackName.trim() || !selectedArtistId || isCreating) return;
        setIsCreating(true);
        try {
            const created = await createTrack({
                name: newTrackName.trim(),
                primaryArtistId: selectedArtistId,
            });
            if (created) {
                setNewTrackName('');
                setSelectedArtistId(null);
                setSelectedArtistName('');
                refresh();
            }
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create track');
        } finally {
            setIsCreating(false);
        }
    };

    const handleArtistSelected = (entity: any) => {
        setSelectedArtistId(entity.id);
        setSelectedArtistName(entity.name);
    };

    const columns = useMemo<ColumnDef<Track, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => <NameCell track={row.original} onSaved={refresh} />,
        },
        {
            id: 'primaryArtist',
            header: 'Primary Artist',
            enableSorting: false,
            meta: { headerClassName: styles.primaryArtist, cellClassName: styles.primaryArtist },
            cell: ({ row }) => <PrimaryArtistCell track={row.original} />,
        },
        {
            id: 'categories',
            header: 'Categories',
            enableSorting: false,
            meta: { headerClassName: styles.categories, cellClassName: styles.categories },
            cell: ({ row }) => <CategoriesCell track={row.original} onChanged={refresh} />,
        },
        {
            id: 'addCategory',
            header: 'Add Category',
            enableSorting: false,
            meta: { headerClassName: styles.addCategory, cellClassName: styles.addCategory },
            cell: ({ row }) => <AddCategoryCell trackId={row.original.id} onAdded={refresh} />,
        },
        {
            id: 'actions',
            header: 'Actions',
            enableSorting: false,
            meta: { headerClassName: styles.actions, cellClassName: styles.actions },
            cell: ({ row }) => <ActionsCell track={row.original} onDeleted={refresh} />,
        },
        // Configurable columns
        {
            id: 'quiz',
            header: 'Quiz',
            enableSorting: false,
            cell: () => <span className={styles.stub}>-</span>,
        },
        {
            id: 'attributes',
            header: 'Attributes',
            enableSorting: false,
            cell: () => <span className={styles.stub}>-</span>,
        },
    ], [refresh]);

    const sorting = useMemo(() => stringToSortingState(sort), [sort]);
    const handleSortingChange = useCallback(
        (updater: any) => {
            const next = typeof updater === 'function' ? updater(sorting) : updater;
            setSort(sortingStateToString(next));
        },
        [sorting, setSort],
    );

    const handleRowClick = useCallback(
        (track: Track) => {
            if (trackId && Number(trackId) === track.id) {
                navigate('.', { relative: 'path' });
            } else {
                navigate(String(track.id));
            }
        },
        [navigate, trackId],
    );

    const createSection = (
        <div className={styles.createSection}>
            <MasterEntityPicker
                entityType="artist"
                buttonLabel={selectedArtistName || 'Select Artist'}
                onEntitySelected={handleArtistSelected}
            />
            <input
                type="text"
                value={newTrackName}
                onChange={(e) => setNewTrackName(e.target.value)}
                placeholder="New track name"
                disabled={isCreating}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateTrack()}
            />
            <button
                onClick={handleCreateTrack}
                disabled={!newTrackName.trim() || !selectedArtistId || isCreating}
                className={styles.createButton}
            >
                {isCreating ? '...' : 'Create'}
            </button>
        </div>
    );

    return (
        <div>
            {createSection}

            <DataTable<Track>
                columns={columns}
                data={entities ?? []}
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={refresh}
                searchPlaceholder="Search track name..."
                sorting={sorting}
                onSortingChange={handleSortingChange}
                pagination={pagination}
                onNextPage={nextPage}
                onPrevPage={prevPage}
                onGoToPage={goToPage}
                isLoading={isLoading}
                onRefresh={refresh}
                onRowClick={handleRowClick}
                activeRowId={trackId ? Number(trackId) : null}
                getRowId={(row) => String(row.id)}
                columnVisibility={columnVisibility}
                onColumnVisibilityChange={onColumnVisibilityChange}
                emptyMessage="No tracks found"
                extraActions={
                    <ColumnToggle
                        columns={configurableColumns}
                        onToggle={toggleColumn}
                        onReset={resetToDefaults}
                    />
                }
            />
        </div>
    );
};
