import { useState, useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useMasterEntityTable } from '@/music/data/master/hooks/useMasterEntityTable';
import { useNotifications } from '@/shared/hooks';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import type { Category } from '@/music/shared/types/entities';
import { NameCell } from './cells/NameCell';
import { ParentsCell } from './cells/ParentsCell';
import { AddParentCell } from './cells/AddParentCell';
import { ActionsCell } from './cells/ActionsCell';
import { createCategory } from '@/music/data/master/api/music-data-categories';
import styles from './CategoriesTable.module.css';

export const CategoriesTable = () => {
    const { showNotification } = useNotifications();
    const permissions = usePermissions();
    const canEdit = permissions.masterEntityAccess === 'full';
    const [newCategoryName, setNewCategoryName] = useState('');
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
    } = useMasterEntityTable('category');

    const handleCreateCategory = async () => {
        if (!newCategoryName.trim() || isCreating) return;
        setIsCreating(true);
        try {
            const created = await createCategory({ name: newCategoryName.trim() });
            if (created) {
                setNewCategoryName('');
                refresh();
            }
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create category');
        } finally {
            setIsCreating(false);
        }
    };

    const columns = useMemo<ColumnDef<Category, any>[]>(() => {
        const cols: ColumnDef<Category, any>[] = [
            {
                accessorKey: 'name',
                header: 'Name',
                enableSorting: true,
                meta: { headerClassName: styles.name, cellClassName: styles.name },
                cell: ({ row }) => <NameCell category={row.original} onSaved={refresh} readOnly={!canEdit} />,
            },
            {
                id: 'parents',
                header: 'Parents',
                enableSorting: false,
                meta: { headerClassName: styles.parents, cellClassName: styles.parents },
                cell: ({ row }) => <ParentsCell category={row.original} onChanged={refresh} readOnly={!canEdit} />,
            },
        ];

        if (canEdit) {
            cols.push(
                {
                    id: 'addParent',
                    header: 'Add Parent',
                    enableSorting: false,
                    meta: { headerClassName: styles.addParent, cellClassName: styles.addParent },
                    cell: ({ row }) => <AddParentCell categoryId={row.original.id} onAdded={refresh} />,
                },
                {
                    id: 'actions',
                    header: 'Actions',
                    enableSorting: false,
                    meta: { headerClassName: styles.actions, cellClassName: styles.actions },
                    cell: ({ row }) => <ActionsCell category={row.original} onDeleted={refresh} />,
                },
            );
        }

        return cols;
    }, [refresh, canEdit]);

    const sorting = useMemo(() => stringToSortingState(sort), [sort]);
    const handleSortingChange = useCallback(
        (updater: any) => {
            const next = typeof updater === 'function' ? updater(sorting) : updater;
            setSort(sortingStateToString(next));
        },
        [sorting, setSort],
    );

    return (
        <div>
            {canEdit && (
                <div className={styles.createSection}>
                    <input
                        type="text"
                        value={newCategoryName}
                        onChange={(e) => setNewCategoryName(e.target.value)}
                        placeholder="New category name"
                        disabled={isCreating}
                        onKeyDown={(e) => e.key === 'Enter' && handleCreateCategory()}
                    />
                    <button
                        onClick={handleCreateCategory}
                        disabled={!newCategoryName.trim() || isCreating}
                        className={styles.createButton}
                    >
                        {isCreating ? '...' : 'Create'}
                    </button>
                </div>
            )}

            <DataTable<Category>
                columns={columns}
                data={entities ?? []}
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={refresh}
                searchPlaceholder="Search category name..."
                sorting={sorting}
                onSortingChange={handleSortingChange}
                pagination={pagination}
                onNextPage={nextPage}
                onPrevPage={prevPage}
                onGoToPage={goToPage}
                isLoading={isLoading}
                onRefresh={refresh}
                getRowId={(row) => String(row.id)}
                emptyMessage="No categories found"
            />
        </div>
    );
};
