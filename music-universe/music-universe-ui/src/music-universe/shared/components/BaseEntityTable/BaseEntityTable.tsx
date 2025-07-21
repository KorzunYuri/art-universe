// hooks
import { useEffect } from 'react'
import type { ReactNode } from 'react'
// types
import type { Page } from '@/music-universe/shared/types/page'
import type { BaseEntity } from '@/music-universe/shared/types/entities.ts'
// hooks
import { PaginatedResource } from '@/music-universe/shared/hooks/PaginatedResource'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './BaseEntityTable.module.scss'

export interface SearchParams {
    page: number
    size: number
    search?: string
    sort?: string
}

export interface BaseEntityTableProps<T extends BaseEntity> {
    fetchEntities: (params: SearchParams) => Promise<Page<T>>
    renderHeader: (sort: string, setSort: (sort: string) => void) => ReactNode
    renderRow: (entity: T) => ReactNode
    searchPlaceholder?: string
    pageSize?: number
    renderBeforeTable?: (loading: boolean) => ReactNode
    renderAfterSearch?: (loading: boolean) => ReactNode
    onDataLoaded?: (data: Page<T>) => void
    onRefresh?: () => void
}

export function BaseEntityTable<T extends BaseEntity>({
    fetchEntities,
    renderHeader,
    renderRow,
    searchPlaceholder = "Search...",
    pageSize = 20,
    renderBeforeTable,
    renderAfterSearch,
    onDataLoaded,
    onRefresh
}: BaseEntityTableProps<T>) {
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

    // Call onDataLoaded when data changes
    useEffect(() => {
        if (data && onDataLoaded) {
            onDataLoaded(data);
        }
    }, [data, onDataLoaded]);

    const onSearchKeyDown = (key: string) => {
        if (key === 'Enter') {
            applySearch();
        }
    };

    const handleRefresh = () => {
        reload();
        // Call onRefresh if provided
        if (onRefresh) {
            onRefresh();
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.searchBar}>
                <input
                    type="text"
                    value={searchInput}
                    placeholder={searchPlaceholder}
                    onChange={(e) => setSearchInput(e.target.value)}
                    onKeyDown={(e) => onSearchKeyDown(e.key)}
                    className={commonStyles.muLabel}
                />
                <button onClick={applySearch}>Search</button>
                <button onClick={handleRefresh} disabled={loading}>
                    Refresh
                </button>
                
                {loading && (
                    <div className={styles.loading}>Loading...</div>
                )}
            </div>

            {renderAfterSearch && renderAfterSearch(loading)}

            {renderBeforeTable && renderBeforeTable(loading)}

            {!loading && data && (
                <>
                    <div className={styles.tableContainer}>
                        {renderHeader(sort, setSort)}
                        {data.content.map((entity) => renderRow(entity))}
                    </div>

                    <div className={styles.pagination}>
                        <button disabled={!hasPrevPage} onClick={prevPage}>
                            Previous
                        </button>
                        <span>
                            Page {data.pageable.pageNumber + 1} of {data.totalPages}
                        </span>
                        <button disabled={!hasNextPage} onClick={nextPage}>
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}
