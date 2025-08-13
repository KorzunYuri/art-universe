// hooks
import { useMasterEntityTable } from "@/music-universe/music-data/hooks/useMasterEntityTable";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { DimensionsTableRow } from "@/music-universe/music-data/components/DimensionsTableRow";
// styles
import styles from './DimensionsTable.module.css';

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true, className: styles.name },
    { key: 'actions', label: 'Actions', className: styles.actions },
];

export const DimensionsTable = () => {
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
        refresh
    } = useMasterEntityTable("dimension");

    return (
        <EntityTable
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={refresh}
            searchPlaceholder="Search dimension name..."
            
            columns={columns}
            emptyMessage="No dimensions found"
            
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
            {entityIds?.map(entityId => (
                <DimensionsTableRow
                    key={entityId}
                    entityId={entityId}
                />
            ))}
        </EntityTable>
    );
};
