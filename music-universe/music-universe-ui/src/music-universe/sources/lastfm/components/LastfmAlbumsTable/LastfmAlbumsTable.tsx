// hooks
import { useLastfmEntityTable } from "@/music-universe/sources/lastfm/hooks/useLastfmEntityTable";
// components
import {
    LastfmAlbumsTableHeader,
    LastfmAlbumsTableRow
} from "@/music-universe/sources/lastfm/components";
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss';
import styles from './LastfmAlbumsTable.module.css';

interface LastfmAlbumsTableProps {
    artistId?: number;
}

export const LastfmAlbumsTable = (
    {
        artistId = undefined
    }: LastfmAlbumsTableProps) =>
{
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
    } = useLastfmEntityTable("album", { artistId: artistId });

    return (
        <div className={styles.container}>
            {/* Search bar */}
            <div className={styles.searchBar}>
                <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && refresh()}
                    placeholder="Search album name..."
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
            {!isLoading && rawEntityIds && (
                <>
                    <div className={styles.table}>
                        {/* Header */}
                        <LastfmAlbumsTableHeader sort={sort} setSort={setSort} />

                        {/* Rows */}
                        {rawEntityIds.map(rawEntityId => (
                            <LastfmAlbumsTableRow
                                key={rawEntityId}
                                entityId={rawEntityId}
                            />
                        ))}

                        {/* Empty state */}
                        {rawEntityIds.length === 0 && (
                            <div className={styles.emptyState}>No albums found</div>
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
