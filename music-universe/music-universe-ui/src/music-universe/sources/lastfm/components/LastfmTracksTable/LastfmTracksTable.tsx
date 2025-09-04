// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
import { useApprovalStatusFilter } from "@/music-universe/sources/shared/hooks";
import { usePlayCountFilter, useListenersCountFilter, useArtistFilter, useTagFilter } from "@/music-universe/sources/lastfm/hooks/useLastfmFilters";
import { useSearchParams } from "react-router-dom";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { LastfmTracksTableRow } from "@/music-universe/sources/lastfm/components";
// types
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
// styles
import trackStyles from "./LastfmTracksTable.module.css";

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
    const [searchParams] = useSearchParams();
    const urlArtistId = searchParams.get('artistId') ? parseInt(searchParams.get('artistId')!) : undefined;
    const effectiveArtistId = artistId || urlArtistId;

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
    } = useLastfmEntityTable("track", { artistId: effectiveArtistId });

    // Filter hooks
    const { approvalStatuses, approvalStatusField } = useApprovalStatusFilter();
    const { minPlayCount, minPlayCountField } = usePlayCountFilter();
    const { minListenersCount, minListenersCountField } = useListenersCountFilter();
    const { artistId: artistIdFilter, artistField, setSelectedArtist } = useArtistFilter();
    const { tagId, tagField } = useTagFilter();

    const handleArtistFilter = (artistId: number, artistName: string) => {
        setSelectedArtist({ id: artistId, name: artistName });
        // Trigger search immediately
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            artistId: artistId,
            tagId: tagId || undefined
        });
    };

    const handleSearchSubmit = () => {
        updateParams({
            approvalStatuses: approvalStatuses.length > 0 ? approvalStatuses : undefined,
            minPlayCount: minPlayCount || undefined,
            minListenersCount: minListenersCount || undefined,
            artistId: artistIdFilter || effectiveArtistId || undefined, // Preserve URL artistId
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
            artistField,
            tagField,
            approvalStatusField
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
                    onArtistFilter={handleArtistFilter}
                />
            ))}
        </EntityTable>
    );
};
