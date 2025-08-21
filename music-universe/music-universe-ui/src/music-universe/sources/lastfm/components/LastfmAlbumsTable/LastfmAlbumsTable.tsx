// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
import { useApprovalStatusFilter } from "@/music-universe/sources/shared/hooks";
import { usePlayCountFilter, useListenersCountFilter, useArtistFilter, useTagFilter } from "@/music-universe/sources/lastfm/hooks/useLastfmFilters";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { LastfmAlbumsTableRow } from "@/music-universe/sources/lastfm/components";
// types
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
// styles
import albumStyles from "./LastfmAlbumsTable.module.css";

const columns: EntityTableColumn[] = [
    { key: 'artist', label: 'Artist', className: albumStyles.artist },
    { key: 'name', label: 'Album name', sortable: true, className: albumStyles.name },
    { key: 'mbid', label: 'MusicBrainz', className: albumStyles.mbid },
    { key: 'status', label: 'Approval', className: albumStyles.status },
    { key: 'masterBinding', label: 'Master', className: albumStyles.masterBinding },
    { key: 'playCount', label: 'Plays', sortable: true, className: albumStyles.count },
    { key: 'listenersCount', label: 'Listeners', sortable: true, className: albumStyles.count },
];

interface LastfmAlbumsTableProps {
    artistId?: number;
}

export const LastfmAlbumsTable = ({ artistId }: LastfmAlbumsTableProps) => {
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
    } = useLastfmEntityTable("album", { artistId });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minPlayCount, minPlayCountField } = usePlayCountFilter();
    const { minListenersCount, minListenersCountField } = useListenersCountFilter();
    const { artistId: artistIdFilter, artistField } = useArtistFilter();
    const { tagId, tagField } = useTagFilter();

    const handleSearchSubmit = () => {
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            artistId: artistIdFilter || undefined,
            tagId: tagId || undefined
        });
    };

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: [
            minPlayCountField,
            minListenersCountField,
            // Only show artist ID filter if not already filtered by prop
            ...(artistId ? [] : [artistField]),
            tagField,
            approvalStatusField
        ]
    };

    return (
        <EntityTable
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={handleSearchSubmit}
            searchPlaceholder="Search album name..."
            
            additionalSearch={additionalSearchConfig}
            
            columns={columns}
            emptyMessage="No albums found"
            
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
                <LastfmAlbumsTableRow
                    key={rawEntityId}
                    entityId={rawEntityId}
                />
            ))}
        </EntityTable>
    );
};
