import { useState, useMemo, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { ColumnDef } from '@tanstack/react-table';
import { useMasterEntityTable } from '@/music/data/master/hooks/useMasterEntityTable';
import { useNotifications } from '@/music/shared/hooks';
import { useCategoryFilter } from '@/music/data/master/hooks/useMasterEntityFilters';
import { useColumnPreferences, type ColumnConfig } from '@/music/shared/hooks/useColumnPreferences';
import { DataTable } from '@/music/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/music/shared/components/DataTable/sortUtils';
import { ColumnToggle } from '@/music/shared/components/ColumnToggle';
import type { AdditionalSearchConfig } from '@/music/shared/components/EntityTable/types';
import type { Artist } from '@/music/shared/types/entities';
import { NameCell } from './cells/NameCell';
import { CategoriesCell } from './cells/CategoriesCell';
import { AddCategoryCell } from './cells/AddCategoryCell';
import { ActionsCell } from './cells/ActionsCell';
import { createArtist } from '@/music/data/master/api/music-data-artists';
import styles from './ArtistsTable.module.css';

const CONFIGURABLE_COLUMNS: ColumnConfig[] = [
    { id: 'quiz', label: 'Quiz', defaultVisible: false },
    { id: 'attributes', label: 'Attributes', defaultVisible: false },
];

export const ArtistsTable = () => {
    const navigate = useNavigate();
    const { artistId } = useParams<{ artistId: string }>();
    const { showNotification } = useNotifications();
    const [newArtistName, setNewArtistName] = useState('');
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
        handleSearchSubmit: submitSearch,
        updateParams,
        refresh,
    } = useMasterEntityTable('artist');

    const { categoryId, categoryField } = useCategoryFilter();

    const {
        columnVisibility,
        onColumnVisibilityChange,
        configurableColumns,
        toggleColumn,
        resetToDefaults,
    } = useColumnPreferences('master-artists', CONFIGURABLE_COLUMNS);

    const handleSearchSubmit = useCallback(() => {
        submitSearch();
        updateParams({ categoryId: categoryId || undefined });
    }, [submitSearch, updateParams, categoryId]);

    const handleCreateArtist = async () => {
        if (!newArtistName.trim() || isCreating) return;
        setIsCreating(true);
        try {
            const created = await createArtist({ name: newArtistName.trim() });
            if (created) {
                setNewArtistName('');
                refresh();
            }
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create artist');
        } finally {
            setIsCreating(false);
        }
    };

    const columns = useMemo<ColumnDef<Artist, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => (
                <NameCell artist={row.original} onSaved={refresh} />
            ),
        },
        {
            id: 'categories',
            header: 'Categories',
            enableSorting: false,
            meta: { headerClassName: styles.categories, cellClassName: styles.categories },
            cell: ({ row }) => (
                <CategoriesCell artist={row.original} onChanged={refresh} />
            ),
        },
        {
            id: 'addCategory',
            header: 'Add Category',
            enableSorting: false,
            meta: { headerClassName: styles.addCategory, cellClassName: styles.addCategory },
            cell: ({ row }) => (
                <AddCategoryCell artistId={row.original.id} onAdded={refresh} />
            ),
        },
        {
            id: 'actions',
            header: 'Actions',
            enableSorting: false,
            meta: { headerClassName: styles.actions, cellClassName: styles.actions },
            cell: ({ row }) => (
                <ActionsCell artist={row.original} onDeleted={refresh} />
            ),
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
        (artist: Artist) => {
            if (artistId && Number(artistId) === artist.id) {
                navigate('.', { relative: 'path' });
            } else {
                navigate(String(artist.id));
            }
        },
        [navigate, artistId],
    );

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: 'Advanced Filters',
        collapsible: true,
        defaultCollapsed: true,
        fields: [categoryField],
    };

    const createSection = (
        <div className={styles.createSection}>
            <input
                type="text"
                value={newArtistName}
                onChange={(e) => setNewArtistName(e.target.value)}
                placeholder="New artist name"
                disabled={isCreating}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateArtist()}
            />
            <button
                onClick={handleCreateArtist}
                disabled={!newArtistName.trim() || isCreating}
                className={styles.createButton}
            >
                {isCreating ? '...' : 'Create'}
            </button>
        </div>
    );

    return (
        <div>
            {createSection}

            <DataTable<Artist>
                columns={columns}
                data={entities ?? []}
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
                onRowClick={handleRowClick}
                activeRowId={artistId ? Number(artistId) : null}
                getRowId={(row) => String(row.id)}
                columnVisibility={columnVisibility}
                onColumnVisibilityChange={onColumnVisibilityChange}
                emptyMessage="No artists found"
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
