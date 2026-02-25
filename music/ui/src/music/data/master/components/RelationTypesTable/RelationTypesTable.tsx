import { useState, useMemo, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { ColumnDef, SortingState } from '@tanstack/react-table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { DataTable } from '@/music/shared/components/DataTable';
import type { EntityTablePagination } from '@/music/shared/components/EntityTable/EntityTable';
import {
    fetchRelationTypes,
    type RelationTypeDTO,
} from '@/music/data/master/api/music-data-relation-types';
import { relationTypeKeys } from '@/music/shared/utils/query-keys';
import styles from './RelationTypesTable.module.css';

const EMPTY_PAGINATION: EntityTablePagination = {
    page: 0,
    totalPages: 1,
    hasNextPage: false,
    hasPrevPage: false,
};

const EMPTY_SORTING: SortingState = [];

export const RelationTypesTable = () => {
    const navigate = useNavigate();
    const { relationTypeId } = useParams<{ relationTypeId: string }>();
    const queryClient = useQueryClient();

    const [search, setSearch] = useState('');
    const [submittedSearch, setSubmittedSearch] = useState('');

    const query = useQuery({
        queryKey: relationTypeKeys.list(submittedSearch),
        queryFn: () => fetchRelationTypes(submittedSearch || undefined),
    });

    const handleSearchSubmit = useCallback(() => {
        setSubmittedSearch(search);
    }, [search]);

    const refresh = useCallback(() => {
        queryClient.invalidateQueries({ queryKey: relationTypeKeys.all });
    }, [queryClient]);

    const handleRowClick = useCallback(
        (row: RelationTypeDTO) => {
            if (relationTypeId && Number(relationTypeId) === row.id) {
                navigate('.', { relative: 'path' });
            } else {
                navigate(String(row.id));
            }
        },
        [navigate, relationTypeId],
    );

    const columns = useMemo<ColumnDef<RelationTypeDTO, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Name',
            enableSorting: false,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
        },
        {
            accessorKey: 'reverseName',
            header: 'Reverse Name',
            enableSorting: false,
            meta: { headerClassName: styles.reverseName, cellClassName: styles.reverseName },
            cell: ({ getValue }) => getValue() ?? <span className={styles.empty}>—</span>,
        },
        {
            id: 'isSymmetrical',
            header: 'Symmetrical',
            enableSorting: false,
            meta: { headerClassName: styles.symmetrical, cellClassName: styles.symmetrical },
            cell: ({ row }) => (
                <span className={row.original.isSymmetrical ? styles.badgeYes : styles.badgeNo}>
                    {row.original.isSymmetrical ? 'Yes' : 'No'}
                </span>
            ),
        },
        {
            id: 'isSystem',
            header: 'System',
            enableSorting: false,
            meta: { headerClassName: styles.system, cellClassName: styles.system },
            cell: ({ row }) => row.original.isSystem
                ? <span className={styles.badgeSystem}>System</span>
                : null,
        },
    ], []);

    return (
        <DataTable<RelationTypeDTO>
            columns={columns}
            data={query.data ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search relation type name..."
            sorting={EMPTY_SORTING}
            onSortingChange={() => {}}
            pagination={EMPTY_PAGINATION}
            onNextPage={() => {}}
            onPrevPage={() => {}}
            onGoToPage={() => {}}
            isLoading={query.isLoading}
            onRefresh={refresh}
            onRowClick={handleRowClick}
            activeRowId={relationTypeId ? Number(relationTypeId) : null}
            getRowId={(row) => String(row.id)}
            emptyMessage="No relation types found"
        />
    );
};
