import styles from './LastfmTagsTable.module.css'
import {useLastfmEntityTable} from "@/music-universe/sources/lastfm/query/useLastfmEntityTable.tsx";
import {
    LastfmTagsTableHeader,
    LastfmTagsTableRow
} from "@/music-universe/sources/lastfm/components";

interface LastfmTagsTableProps {
    initialSearch?: string;
}

export const LastfmTagsTable = (
    {
        initialSearch = ''
    }: LastfmTagsTableProps
) => {

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
        refresh
    } = useLastfmEntityTable("category", { search: initialSearch});

    return (
        <div className={styles.container}>
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
            {!isLoading && rawEntityIds && (
                <>
                    <div className={styles.table}>
                        {/* Header */}
                        <LastfmTagsTableHeader sort={sort} setSort={setSort}/>

                        {/* Rows */}
                        {rawEntityIds.map(rawEntityId => (
                            <LastfmTagsTableRow
                                key={rawEntityId}
                                entityId={rawEntityId}
                            />
                        ))}

                        {/* Empty state */}
                        {rawEntityIds.length === 0 && (
                            <div className={styles.emptyState}>No tags found</div>
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
    )
}
