// hooks
import {useMasterEntityTable} from "@/music-universe/music-data/hooks/useMasterEntityTable.ts";
// components
import {CategoriesTableHeader} from "@/music-universe/music-data/components/CategoriesTableHeader";
import {CategoriesTableRow} from "@/music-universe/music-data/components/CategoriesTableRow";
// styles
import styles from './CategoriesTable.module.css'
import commonStyles from "@/music-universe/shared/styles/common.module.scss";
import {useMasterEntitiesLookup} from "@/music-universe/music-data/hooks/useMasterEntitiesLookup.ts";

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
        refresh
    } = useMasterEntityTable("category");

    // init dimensions cache
    useMasterEntitiesLookup('dimension', { search: '' }); // we need all dimensions at once

    return (
        <div className={styles.container}>
            {/* Search bar */}
            <div className={styles.searchBar}>
                <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && refresh()}
                    placeholder="Search category name..."
                    className={commonStyles.muInput}
                />
                <button
                    onClick={refresh}
                    className={styles.searchButton}
                    disabled={isLoading}
                >
                    Search
                </button>
                <button
                    onClick={refresh}
                    className={styles.refreshButton}
                    disabled={isLoading}
                >
                    Refresh
                </button>
            </div>

            {/* Loading indicator */}
            {isLoading && (
                <div className={styles.loading}>Loading...</div>
            )}

            {/* Table */}
            {!isLoading && entityIds && (
                <>
                    <div className={styles.table}>
                        {/* Header */}
                        <CategoriesTableHeader sort={sort} setSort={setSort}/>

                        {/* Rows */}
                        {entityIds.map(entityId => (
                            <CategoriesTableRow
                                key={entityId}
                                entityId={entityId}
                            />
                        ))}

                        {/* Empty state */}
                        {entityIds.length === 0 && (
                            <div className={styles.emptyState}>No categories found</div>
                        )}
                    </div>

                    {/* Pagination */}
                    <div className={styles.pagination}>
                        <button
                            onClick={prevPage}
                            disabled={!pagination.hasPrevPage || isLoading}
                            className={styles.paginationButton}
                        >
                            Previous
                        </button>
                        <span className={styles.pageInfo}>
                          Page {pagination.page + 1} of {pagination.totalPages}
                        </span>
                        <button
                            onClick={nextPage}
                            disabled={!pagination.hasNextPage || isLoading}
                            className={styles.paginationButton}
                        >
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}
