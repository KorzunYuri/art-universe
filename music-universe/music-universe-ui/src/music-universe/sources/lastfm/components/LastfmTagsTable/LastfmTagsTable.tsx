// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
import { useApprovalStatusFilter } from "@/music-universe/sources/shared/hooks";
import { useUsageCountFilter, useUsageUsersCountFilter } from "@/music-universe/sources/lastfm/hooks/useLastfmFilters";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { LastfmTagsTableRow } from "@/music-universe/sources/lastfm/components";
// types
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
// styles
import tagStyles from "./LastfmTagsTable.module.css";

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Tag Name', sortable: true, className: tagStyles.name },
    { key: 'status', label: 'Approval', className: tagStyles.status },
    { key: 'masterBinding', label: 'Master', className: tagStyles.masterBinding },
    { key: 'usageCount', label: 'Usage', sortable: true, className: tagStyles.count },
    { key: 'usageUsersCount', label: 'Users', sortable: true, className: tagStyles.count },
];

interface LastfmTagsTableProps {
    initialSearch?: string;
}

export const LastfmTagsTable = ({ initialSearch = '' }: LastfmTagsTableProps) => {
    const {
        rawEntityIds,
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
        refresh
    } = useLastfmEntityTable("category", { search: initialSearch });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minUsageCount, minUsageCountField } = useUsageCountFilter();
    const { minUsageUsersCount, minUsageUsersCountField } = useUsageUsersCountFilter();

    const handleSearchSubmit = () => {
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minUsageCount: minUsageCount || undefined,
            minUsageUsersCount: minUsageUsersCount || undefined
        });
    };

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: [
            minUsageCountField,
            minUsageUsersCountField,
            approvalStatusField
        ]
    };

    return (
        <EntityTable
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search tag name..."
            
            additionalSearch={additionalSearchConfig}
            
            columns={columns}
            emptyMessage="No tags found"
            
            sort={sort}
            onSortChange={setSort}
            
            pagination={{
                page: pagination.page,
                totalPages: pagination.totalPages,
                hasNextPage: pagination.hasNextPage,
                hasPrevPage: pagination.hasPrevPage,
            }}
            onNextPage={nextPage}
            onPrevPage={prevPage}
            onGoToPage={goToPage}
            
            isLoading={isLoading}
            onRefresh={refresh}
        >
            {rawEntityIds?.map(rawEntityId => (
                <LastfmTagsTableRow
                    key={rawEntityId}
                    entityId={rawEntityId}
                />
            ))}
        </EntityTable>
    );
};
