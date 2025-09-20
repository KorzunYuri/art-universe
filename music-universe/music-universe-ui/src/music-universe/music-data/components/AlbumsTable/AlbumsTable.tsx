import { useMasterEntityTable } from "@/music-universe/music-data/hooks/useMasterEntityTable";
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import type { AdditionalSearchConfig } from "@/music-universe/shared/components/EntityTable/types";
import styles from './AlbumsTable.module.css';

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true, className: styles.name },
    { key: 'primaryArtist', label: 'Primary Artist', className: styles.primaryArtist },
    { key: 'actions', label: 'Actions', className: styles.actions },
];

export const AlbumsTable = () => {
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
    } = useMasterEntityTable("album");

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
            {/* TODO: Add AlbumsTableRow when implemented */}
            {entityIds?.map(entityId => (
                <div key={entityId}>Album {entityId}</div>
            ))}
        </EntityTable>
    );
};
