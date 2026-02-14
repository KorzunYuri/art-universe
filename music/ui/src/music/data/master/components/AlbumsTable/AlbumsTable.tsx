import { useState, useMemo, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { ColumnDef } from '@tanstack/react-table';
import { useMasterEntityTable } from '@/music/data/master/hooks/useMasterEntityTable';
import { useNotifications } from '@/music/shared/hooks';
import { useColumnPreferences, type ColumnConfig } from '@/music/shared/hooks/useColumnPreferences';
import { DataTable } from '@/music/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/music/shared/components/DataTable/sortUtils';
import { ColumnToggle } from '@/music/shared/components/ColumnToggle';
import { MasterEntityPicker } from '@/music/data/master/components/MasterEntityPicker';
import type { Album } from '@/music/shared/types/entities';
import { NameCell } from './cells/NameCell';
import { PrimaryArtistCell } from './cells/PrimaryArtistCell';
import { CategoriesCell } from './cells/CategoriesCell';
import { AddCategoryCell } from './cells/AddCategoryCell';
import { ActionsCell } from './cells/ActionsCell';
import { createAlbum } from '@/music/data/master/api/music-data-albums';
import styles from './AlbumsTable.module.css';

const CONFIGURABLE_COLUMNS: ColumnConfig[] = [
    { id: 'quiz', label: 'Quiz', defaultVisible: false },
    { id: 'attributes', label: 'Attributes', defaultVisible: false },
];

export const AlbumsTable = () => {
    const navigate = useNavigate();
    const { albumId } = useParams<{ albumId: string }>();
    const { showNotification } = useNotifications();
    const [newAlbumName, setNewAlbumName] = useState('');
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
    } = useMasterEntityTable('album');

    const {
        columnVisibility,
        onColumnVisibilityChange,
        configurableColumns,
        toggleColumn,
        resetToDefaults,
    } = useColumnPreferences('master-albums', CONFIGURABLE_COLUMNS);

    const handleCreateAlbum = async () => {
        if (!newAlbumName.trim() || !selectedArtistId || isCreating) return;
        setIsCreating(true);
        try {
            const created = await createAlbum({
                name: newAlbumName.trim(),
                primaryArtistId: selectedArtistId,
            });
            if (created) {
                setNewAlbumName('');
                setSelectedArtistId(null);
                setSelectedArtistName('');
                refresh();
            }
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create album');
        } finally {
            setIsCreating(false);
        }
    };

    const handleArtistSelected = (entity: any) => {
        setSelectedArtistId(entity.id);
        setSelectedArtistName(entity.name);
    };

    const columns = useMemo<ColumnDef<Album, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => <NameCell album={row.original} onSaved={refresh} />,
        },
        {
            id: 'primaryArtist',
            header: 'Primary Artist',
            enableSorting: false,
            meta: { headerClassName: styles.primaryArtist, cellClassName: styles.primaryArtist },
            cell: ({ row }) => <PrimaryArtistCell album={row.original} />,
        },
        {
            id: 'categories',
            header: 'Categories',
            enableSorting: false,
            meta: { headerClassName: styles.categories, cellClassName: styles.categories },
            cell: ({ row }) => <CategoriesCell album={row.original} onChanged={refresh} />,
        },
        {
            id: 'addCategory',
            header: 'Add Category',
            enableSorting: false,
            meta: { headerClassName: styles.addCategory, cellClassName: styles.addCategory },
            cell: ({ row }) => <AddCategoryCell albumId={row.original.id} onAdded={refresh} />,
        },
        {
            id: 'actions',
            header: 'Actions',
            enableSorting: false,
            meta: { headerClassName: styles.actions, cellClassName: styles.actions },
            cell: ({ row }) => <ActionsCell album={row.original} onDeleted={refresh} />,
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
        (album: Album) => {
            if (albumId && Number(albumId) === album.id) {
                navigate('.', { relative: 'path' });
            } else {
                navigate(String(album.id));
            }
        },
        [navigate, albumId],
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
                value={newAlbumName}
                onChange={(e) => setNewAlbumName(e.target.value)}
                placeholder="New album name"
                disabled={isCreating}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateAlbum()}
            />
            <button
                onClick={handleCreateAlbum}
                disabled={!newAlbumName.trim() || !selectedArtistId || isCreating}
                className={styles.createButton}
            >
                {isCreating ? '...' : 'Create'}
            </button>
        </div>
    );

    return (
        <div>
            {createSection}

            <DataTable<Album>
                columns={columns}
                data={entities ?? []}
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={refresh}
                searchPlaceholder="Search album name..."
                sorting={sorting}
                onSortingChange={handleSortingChange}
                pagination={pagination}
                onNextPage={nextPage}
                onPrevPage={prevPage}
                onGoToPage={goToPage}
                isLoading={isLoading}
                onRefresh={refresh}
                onRowClick={handleRowClick}
                activeRowId={albumId ? Number(albumId) : null}
                getRowId={(row) => String(row.id)}
                columnVisibility={columnVisibility}
                onColumnVisibilityChange={onColumnVisibilityChange}
                emptyMessage="No albums found"
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
