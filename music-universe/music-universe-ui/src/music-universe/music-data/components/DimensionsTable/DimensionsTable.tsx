// hooks
import {useMasterEntityTable} from "@/music-universe/music-data/hooks/useMasterEntityTable.ts";
// components
import {DimensionsTableHeader} from "@/music-universe/music-data/components/DimensionsTableHeader";
import {DimensionsTableRow} from "@/music-universe/music-data/components/DimensionsTableRow";
// styles
import styles from './DimensionsTable.module.css'
import commonStyles from "@/music-universe/shared/styles/common.module.scss";

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
        refresh
    } = useMasterEntityTable("dimension");

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
                        <DimensionsTableHeader sort={sort} setSort={setSort}/>

                        {/* Rows */}
                        {entityIds.map(entityId => (
                            <DimensionsTableRow
                                key={entityId}
                                entityId={entityId}
                            />
                        ))}

                        {/* Empty state */}
                        {entityIds.length === 0 && (
                            <div className={styles.emptyState}>No dimensions found</div>
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
