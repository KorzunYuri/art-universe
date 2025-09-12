// hooks
import { useMasterEntityTable } from "@/music-universe/music-data/hooks/useMasterEntityTable";
// components
import { EntityTable, type EntityTableColumn } from "@/music-universe/shared/components/EntityTable/EntityTable";
import { CategoriesTableRow } from "@/music-universe/music-data/components/CategoriesTableRow";
// styles
import styles from './CategoriesTable.module.css';

const columns: EntityTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true, className: styles.name },
    { key: 'parents', label: 'Parents', className: styles.parents },
    { key: 'addParent', label: 'Add Parent', className: styles.addParent },
    { key: 'actions', label: 'Actions', className: styles.actions },
];

export const CategoriesTable = () => {
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
    } = useMasterEntityTable("category");

    return (
        <EntityTable
            search={search}
            onSearchChange={setSearch}
            onSearchSubmit={refresh}
            searchPlaceholder="Search category name..."
            
            columns={columns}
            emptyMessage="No categories found"
            
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
                <CategoriesTableRow
                    key={entityId}
                    entityId={entityId}
                />
            ))}
        </EntityTable>
    );
};
