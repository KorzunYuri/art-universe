// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
import { useApprovalStatusFilter, useAdditionalSearchFields } from "@/music-universe/shared/hooks/useAdditionalSearchFields";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { LastfmTagsTableRow } from "@/music-universe/sources/lastfm/components";
// types
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
// styles
import tagStyles from "./LastfmTagsTable.module.css";
import { useState } from "react";

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

    // Additional search fields
    const { 
        approvalStatuses, 
        approvalStatusField 
    } = useApprovalStatusFilter();
    
    // Usage count filters
    const [minUsageCount, setMinUsageCount] = useState<number | ''>('');
    const [minUsageUsersCount, setMinUsageUsersCount] = useState<number | ''>('');
    
    const { createNumberField } = useAdditionalSearchFields();
    
    const minUsageCountField = createNumberField(
        'minUsageCount',
        'Min Usage Count',
        minUsageCount,
        setMinUsageCount,
        { placeholder: 'e.g. 10', min: 0 }
    );
    
    const minUsageUsersCountField = createNumberField(
        'minUsageUsersCount',
        'Min Users Count',
        minUsageUsersCount,
        setMinUsageUsersCount,
        { placeholder: 'e.g. 5', min: 0 }
    );

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
