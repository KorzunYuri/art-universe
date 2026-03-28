import { useState, useMemo, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { ColumnDef } from '@tanstack/react-table';
import { usePersonTable } from '@/art/data/master/hooks/usePersonTable';
import { useNotifications } from '@/shared/hooks';
import { usePermissions } from '@/shared/hooks/usePermissions';
import { DataTable } from '@/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/shared/components/DataTable/sortUtils';
import { createPerson, type PersonDto } from '@/art/data/master/api/art-data-persons';
import { NameCell } from './cells/NameCell';
import { ActionsCell } from './cells/ActionsCell';
import styles from './PersonsTable.module.css';

export const PersonsTable = () => {
    const navigate = useNavigate();
    const { personId } = useParams<{ personId: string }>();
    const { showNotification } = useNotifications();
    const permissions = usePermissions();
    const canEdit = permissions.masterEntityAccess === 'full';
    const [newPersonName, setNewPersonName] = useState('');
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
        handleSearchSubmit,
        refresh,
    } = usePersonTable();

    const handleCreatePerson = async () => {
        if (!newPersonName.trim() || isCreating) return;
        setIsCreating(true);
        try {
            const created = await createPerson({ name: newPersonName.trim() });
            if (created) {
                setNewPersonName('');
                refresh();
            }
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to create person');
        } finally {
            setIsCreating(false);
        }
    };

    const columns = useMemo<ColumnDef<PersonDto, any>[]>(() => {
        const cols: ColumnDef<PersonDto, any>[] = [
            {
                accessorKey: 'name',
                header: 'Name',
                enableSorting: true,
                meta: { headerClassName: styles.name, cellClassName: styles.name },
                cell: ({ row }) => (
                    <NameCell person={row.original} onSaved={refresh} readOnly={!canEdit} />
                ),
            },
        ];

        if (canEdit) {
            cols.push({
                id: 'actions',
                header: 'Actions',
                enableSorting: false,
                meta: { headerClassName: styles.actions, cellClassName: styles.actions },
                cell: ({ row }) => (
                    <ActionsCell person={row.original} onDeleted={refresh} />
                ),
            });
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

    const handleRowClick = useCallback(
        (person: PersonDto) => {
            if (personId && Number(personId) === person.id) {
                navigate('.', { relative: 'path' });
            } else {
                navigate(String(person.id));
            }
        },
        [navigate, personId],
    );

    const createSection = canEdit ? (
        <div className={styles.createSection}>
            <input
                type="text"
                value={newPersonName}
                onChange={(e) => setNewPersonName(e.target.value)}
                placeholder="New person name"
                disabled={isCreating}
                onKeyDown={(e) => e.key === 'Enter' && handleCreatePerson()}
            />
            <button
                onClick={handleCreatePerson}
                disabled={!newPersonName.trim() || isCreating}
                className={styles.createButton}
            >
                {isCreating ? '...' : 'Create'}
            </button>
        </div>
    ) : null;

    return (
        <div>
            {createSection}

            <DataTable<PersonDto>
                columns={columns}
                data={entities ?? []}
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={handleSearchSubmit}
                searchPlaceholder="Search person name..."
                sorting={sorting}
                onSortingChange={handleSortingChange}
                pagination={pagination}
                onNextPage={nextPage}
                onPrevPage={prevPage}
                onGoToPage={goToPage}
                isLoading={isLoading}
                onRefresh={refresh}
                onRowClick={handleRowClick}
                activeRowId={personId ? Number(personId) : null}
                getRowId={(row) => String(row.id)}
                emptyMessage="No persons found"
            />
        </div>
    );
};
