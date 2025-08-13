// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
import { useApprovalStatusFilter, useCountFilters } from "@/music-universe/shared/hooks/useAdditionalSearchFields";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { LastfmArtistsTableRow } from "@/music-universe/sources/lastfm/components";
// types
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
// styles
import artistStyles from "./LastfmArtistsTable.module.css";

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Artist name', sortable: true, className: artistStyles.name },
    { key: 'mbid', label: 'MusicBrainz', className: artistStyles.mbid },
    { key: 'status', label: 'Approval', className: artistStyles.status },
    { key: 'masterBinding', label: 'Master', className: artistStyles.masterBinding },
    { key: 'quizBinding', label: 'Quiz', className: artistStyles.quizBinding },
    { key: 'playCount', label: 'Plays', sortable: true, className: artistStyles.count },
    { key: 'listenersCount', label: 'Listeners', sortable: true, className: artistStyles.count },
];

export const LastfmArtistsTable = () => {
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
    } = useLastfmEntityTable("artist");

    // Additional search fields
    const { 
        approvalStatuses, 
        approvalStatusField 
    } = useApprovalStatusFilter();
    
    const { 
        minPlayCount, 
        minListenersCount,
        minPlayCountField,
        minListenersCountField 
    } = useCountFilters();

    // Handle search submit with additional parameters
    const handleSearchSubmit = () => {
        // Update params with additional search criteria
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
        });
    };

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: [
            minPlayCountField,
            minListenersCountField,
            approvalStatusField
        ]
    };

    return (
        <EntityTable
            // Search functionality
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search artist name..."
            
            // Additional search fields
            additionalSearch={additionalSearchConfig}
            
            // Table structure
            columns={columns}
            emptyMessage="No artists found"
            
            // Sorting
            sort={sort}
            onSortChange={setSort}
            
            // Pagination
            pagination={{
                page: pagination.page,
                totalPages: pagination.totalPages,
                hasNextPage: pagination.hasNextPage,
                hasPrevPage: pagination.hasPrevPage,
            }}
            onNextPage={nextPage}
            onPrevPage={prevPage}
            onGoToPage={goToPage}
            
            // Loading state
            isLoading={isLoading}
            
            // Actions
            onRefresh={refresh}
        >
            {/* Table rows */}
            {rawEntityIds?.map(rawEntityId => (
                <LastfmArtistsTableRow
                    key={rawEntityId}
                    entityId={rawEntityId}
                />
            ))}
        </EntityTable>
    );
};
