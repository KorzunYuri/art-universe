// components
import {
    LastfmArtistsTableHeader,
    LastfmArtistsTableRow,
} from '@/music-universe/sources/lastfm/components'
// styles
import styles from './LastfmArtistsTable.module.css'
import {useLastfmArtistsTable} from "@/music-universe/sources/lastfm/query/useLastfmArtistsTable.tsx";

export const LastfmArtistsTable = () => {
    const {
        rawEntityIds,
        rawEntities,
        pagination,
        search,
        sort,
        isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        refresh
    } = useLastfmArtistsTable();

    return (
        <div className={styles.container}>
            <h2>Artists (TanStack Query)</h2>

            {/* Search bar */}
            <div className={styles.searchBar}>
                <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && refresh()}
                    placeholder="Search artist name..."
                    className={styles.searchInput}
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
            {!isLoading && rawEntities && (
                <>
                    <div className={styles.table}>
                        {/* Header */}
                        <LastfmArtistsTableHeader sort={sort} setSort={setSort}/>

                        {/* Rows */}
                        {rawEntities.map(entity => (
                            <LastfmArtistsTableRow
                                key={entity.id}
                                entity={entity}
                            />
                        ))}

                        {/* Empty state */}
                        {rawEntities.length === 0 && (
                            <div className={styles.emptyState}>No artists found</div>
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
