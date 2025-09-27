import { useMasterEntityTable } from "@/music/data/master/hooks/useMasterEntityTable.ts";
import { EntityTable, type EntityTableColumn } from "@/music/shared/components/EntityTable/EntityTable.tsx";
import type { AdditionalSearchConfig } from "@/music/shared/components/EntityTable/types.ts";
import styles from './TracksTable.module.css';

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true, className: styles.name },
    { key: 'primaryArtist', label: 'Primary Artist', className: styles.primaryArtist },
    { key: 'actions', label: 'Actions', className: styles.actions },
];

export const TracksTable = () => {
    const {
        entityIds,
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
        refresh
    } = useMasterEntityTable("track");

    const additionalSearchConfig: AdditionalSearchConfig = {
        title: "Advanced Filters",
        collapsible: true,
        defaultCollapsed: true,
        fields: []
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
            {/* TODO: Add TracksTableRow when implemented */}
            {entityIds?.map(entityId => (
                <div key={entityId}>Track {entityId}</div>
            ))}
        </EntityTable>
    );
};
