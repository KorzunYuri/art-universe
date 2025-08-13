// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
import { useApprovalStatusFilter, useCountFilters, useAdditionalSearchFields } from "@/music-universe/shared/hooks/useAdditionalSearchFields";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { LastfmTracksTableRow } from "@/music-universe/sources/lastfm/components";
// types
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
// styles
import trackStyles from "./LastfmTracksTable.module.css";
import { useState } from "react";

const columns: EntityTableColumn[] = [
    { key: 'artist', label: 'Artist', className: trackStyles.artist },
    { key: 'name', label: 'Track name', sortable: true, className: trackStyles.name },
    { key: 'mbid', label: 'MusicBrainz', className: trackStyles.mbid },
    { key: 'status', label: 'Approval', className: trackStyles.status },
    { key: 'masterBinding', label: 'Master', className: trackStyles.masterBinding },
    { key: 'quizBinding', label: 'Quiz', className: trackStyles.quizBinding },
    { key: 'playCount', label: 'Plays', sortable: true, className: trackStyles.count },
    { key: 'listenersCount', label: 'Listeners', sortable: true, className: trackStyles.count },
];

interface LastfmTracksTableProps {
    artistId?: number;
}

export const LastfmTracksTable = ({ artistId }: LastfmTracksTableProps) => {
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
    } = useLastfmEntityTable("track", { artistId });

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

    // Artist ID field (if not already filtered by artistId prop)
    const [artistIdFilter, setArtistIdFilter] = useState<number | ''>('');
    const { createNumberField } = useAdditionalSearchFields();
    
    const artistIdField = createNumberField(
        'artistId',
        'Artist ID',
        artistIdFilter,
        setArtistIdFilter,
        { placeholder: 'Filter by artist ID', min: 1 }
    );

    const handleSearchSubmit = () => {
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            artistId: artistIdFilter || undefined
        });
    };

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: [
            minPlayCountField,
            minListenersCountField,
            approvalStatusField,
            // Only show artist ID filter if not already filtered by prop
            ...(artistId ? [] : [artistIdField])
        ]
    };

    return (
        <EntityTable
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search track name..."
            
            additionalSearch={additionalSearchConfig}
            
            columns={columns}
            emptyMessage="No tracks found"
            
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
                <LastfmTracksTableRow
                    key={rawEntityId}
                    entityId={rawEntityId}
                />
            ))}
        </EntityTable>
    );
};
