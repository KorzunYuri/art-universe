// hooks
import { useLastfmEntityTable } from "@/music/data/raw/lastfm/hooks/useLastfmEntityTable.tsx";
import { useApprovalStatusFilter } from "@/music/data/raw/shared/hooks";
import { usePlayCountFilter, useListenersCountFilter, useTagFilter } from "@/music/data/raw/lastfm/hooks/useLastfmFilters.tsx";
import { useSearchParams } from "react-router-dom";
import { useState } from "react";
// components
import { EntityTable, type EntityTableColumn } from "@/music/shared/components/EntityTable/EntityTable.tsx";
import { LastfmArtistsTableRow } from "@/music/data/raw/lastfm/components";
// api
import { searchArtist } from "@/music/data/raw/lastfm/api/lastfm-artists.ts";
// hooks
import { useNotifications } from "@/music/shared/hooks/useNotifications.ts";
// types
import type { AdditionalSearchConfig } from "@/music/shared/components/EntityTable/types.ts";
// styles
import artistStyles from "./LastfmArtistsTable.module.css";
import entityTableStyles from "@/music/shared/components/EntityTable/EntityTable.module.scss";

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
    const [searchParams] = useSearchParams();
    const initialSearch = searchParams.get('search') || '';
    const [isSearching, setIsSearching] = useState(false);
    const { showNotification } = useNotifications();
    
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
        handleSearchSubmit: submitSearch,
        updateParams,
        refresh
    } = useLastfmEntityTable("artist", { search: initialSearch });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minPlayCount, minPlayCountField } = usePlayCountFilter();
    const { minListenersCount, minListenersCountField } = useListenersCountFilter();
    const { tagId, tagField } = useTagFilter();

    // Handle search submit with additional parameters
    const handleSearchSubmit = () => {
        submitSearch();
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            tagId: tagId || undefined,
        });
    };

    const handleForcedSearch = async () => {
        if (!search.trim()) return;
        
        setIsSearching(true);
        try {
            await searchArtist(search);
            showNotification('success', `Forced search requested for: ${search}`);
            refresh();
        } catch (error) {
            console.error('Failed to trigger forced search:', error);
            showNotification('error', 'Failed to trigger forced search');
        } finally {
            setIsSearching(false);
        }
    };

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: [
            minPlayCountField,
            minListenersCountField,
            tagField,
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
            extraActions={
                <button
                    onClick={handleForcedSearch}
                    disabled={!search.trim() || isSearching || isLoading}
                    className={`${entityTableStyles.actionButton} ${artistStyles.searchButton}`}
                >
                    {isSearching ? 'Searching...' : 'Force Search'}
                </button>
            }
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
