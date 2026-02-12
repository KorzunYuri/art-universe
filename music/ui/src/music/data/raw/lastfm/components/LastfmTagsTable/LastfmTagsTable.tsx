import { useMemo, useCallback } from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useLastfmEntityTable } from '@/music/data/raw/lastfm/hooks/useLastfmEntityTable';
import { useApprovalStatusFilter } from '@/music/data/raw/shared/hooks';
import { useUsageCountFilter, useUsageUsersCountFilter } from '@/music/data/raw/lastfm/hooks/useLastfmFilters';
import { DataTable } from '@/music/shared/components/DataTable';
import { stringToSortingState, sortingStateToString } from '@/music/shared/components/DataTable/sortUtils';
import { ExternalLink, ReadonlyAttr } from '@/music/shared/components';
import { LastfmApprovalCell, LastfmBindingCell } from '@/music/data/raw/lastfm/components/cells';
import type { LastfmTag } from '@/music/data/raw/lastfm/types/lastfm-tag';
import type { AdditionalSearchConfig } from '@/music/shared/components/EntityTable/types';
import styles from './LastfmTagsTable.module.css';

interface LastfmTagsTableProps {
    initialSearch?: string;
}

export const LastfmTagsTable = ({ initialSearch = '' }: LastfmTagsTableProps) => {
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
    } = useLastfmEntityTable('category', { search: initialSearch });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minUsageCount, minUsageCountField } = useUsageCountFilter();
    const { minUsageUsersCount, minUsageUsersCountField } = useUsageUsersCountFilter();

    const handleSearchSubmit = useCallback(() => {
        submitSearch();
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minUsageCount: minUsageCount || undefined,
            minUsageUsersCount: minUsageUsersCount || undefined,
        });
    }, [submitSearch, updateParams, approvalStatuses, minUsageCount, minUsageUsersCount]);

    const columns = useMemo<ColumnDef<LastfmTag, any>[]>(() => [
        {
            accessorKey: 'name',
            header: 'Tag Name',
            enableSorting: true,
            meta: { headerClassName: styles.name, cellClassName: styles.name },
            cell: ({ row }) => {
                const tag = row.original;
                return tag.url ? <ExternalLink href={tag.url} label={tag.name} /> : <span>{tag.name}</span>;
            },
        },
        {
            id: 'status',
            header: 'Approval',
            enableSorting: false,
            meta: { headerClassName: styles.status, cellClassName: styles.status },
            cell: ({ row }) => (
                <LastfmApprovalCell entityType="category" entityId={row.original.id} />
            ),
        },
        {
            id: 'masterBinding',
            header: 'Master',
            enableSorting: false,
            meta: { headerClassName: styles.masterBinding, cellClassName: styles.masterBinding },
            cell: ({ row }) => (
                <LastfmBindingCell entityType="category" entityId={row.original.id} />
            ),
        },
        {
            accessorKey: 'usageCount',
            header: 'Usage',
            enableSorting: true,
            meta: { headerClassName: styles.count, cellClassName: styles.count },
            cell: ({ row }) => <ReadonlyAttr value={row.original.usageCount} />,
        },
        {
            accessorKey: 'usageUsersCount',
            header: 'Users',
            enableSorting: true,
            meta: { headerClassName: styles.count, cellClassName: styles.count },
            cell: ({ row }) => <ReadonlyAttr value={row.original.usageUsersCount} />,
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
        fields: [minUsageCountField, minUsageUsersCountField, approvalStatusField],
    };

    return (
        <DataTable<LastfmTag>
            columns={columns}
            data={rawEntities ?? []}
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search tag name..."
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
            emptyMessage="No tags found"
        />
    );
};
