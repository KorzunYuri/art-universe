// hooks
import { useCallback } from 'react'
import type { ReactNode } from 'react'
// types
import type { MasterEntity } from '@/music-universe/shared/types/entity-reference'
import type { Page } from '@/music-universe/shared/types/page'
// hooks
import { PaginatedResource } from '@/music-universe/shared/hooks/PaginatedResource'
// styles
import styles from './MasterEntityTable.module.scss'
import type {SearchParams} from "@/music-universe/shared/components/BaseEntityTable/BaseEntityTable.tsx";

interface MasterEntityTableProps<T extends MasterEntity> {
    fetchEntities: (params: SearchParams) => Promise<Page<T>>
    renderHeader: (sort: string, setSort: (sort: string) => void) => ReactNode
    renderRow: (entity: T) => ReactNode
    searchPlaceholder?: string
    pageSize?: number
    onRefresh?: () => void
}

export function MasterEntityTable<T extends MasterEntity>({
    fetchEntities,
    renderHeader,
    renderRow,
    searchPlaceholder = "Search...",
    pageSize = 20,
    onRefresh
}: MasterEntityTableProps<T>) {
    const {
        data,
        loading,
        searchInput,
        setSearchInput,
        applySearch,
        sort,
        setSort,
        nextPage,
        prevPage,
        hasNextPage,
        hasPrevPage,
        reload,
    } = PaginatedResource<T>(fetchEntities, { pageSize })

    // Handle search key press
    const handleSearchKeyPress = useCallback((e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            applySearch();
        }
    }, [applySearch]);

    const handleRefresh = () => {
        reload();
        // Call onRefresh if provided
        if (onRefresh) {
            onRefresh();
        }
    };

    return (
        <div className={styles.container}>
            {/* Search bar */}
            <div className={styles.searchBar}>
                <input
                    type="text"
                    value={searchInput}
                    onChange={(e) => setSearchInput(e.target.value)}
                    onKeyDown={handleSearchKeyPress}
                    placeholder={searchPlaceholder}
                    className={styles.searchInput}
                />
                <button 
                    onClick={applySearch}
                    className={styles.searchButton}
                    disabled={loading}
                >
                    Search
                </button>
                <button 
                    onClick={handleRefresh}
                    className={styles.refreshButton}
                    disabled={loading}
                >
                    Refresh
                </button>
            </div>

            {/* Loading indicator */}
            {loading && (
                <div className={styles.loading}>Loading...</div>
            )}

            {/* Table */}
            {!loading && data && (
                <>
                    <div className={styles.table}>
                        {/* Header */}
                        {renderHeader(sort, setSort)}
                        
                        {/* Rows */}
                        {data.content.map(entity => renderRow(entity))}
                        
                        {/* Empty state */}
                        {data.content.length === 0 && (
                            <div className={styles.emptyState}>No items found</div>
                        )}
                    </div>

                    {/* Pagination */}
                    <div className={styles.pagination}>
                        <button 
                            onClick={prevPage}
                            disabled={!hasPrevPage || loading}
                            className={styles.paginationButton}
                        >
                            Previous
                        </button>
                        <span className={styles.pageInfo}>
                            Page {data.pageable.pageNumber + 1} of {data.totalPages}
                        </span>
                        <button 
                            onClick={nextPage}
                            disabled={!hasNextPage || loading}
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
