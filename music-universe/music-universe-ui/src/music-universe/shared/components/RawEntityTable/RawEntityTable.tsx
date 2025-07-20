// hooks
import { useCallback, useEffect } from 'react'
import type { ReactNode } from 'react'
// types
import type { RawEntity, MasterEntity } from '@/music-universe/shared/types/entity-reference'
import type { Page } from '@/music-universe/shared/types/page'
import type { BoundEntityResponse } from '@/music-universe/shared/types/master'
// hooks
import { useRawEntityTable } from '@/music-universe/shared/hooks/useRawEntityTable.ts'
// styles
import styles from './RawEntityTable.module.scss'
import type {SearchParams} from "@/music-universe/shared/components/BaseEntityTable/BaseEntityTable.tsx";


interface RawEntityTableProps<T extends RawEntity<M>, M extends MasterEntity> {
    fetchEntities: (params: SearchParams) => Promise<Page<T>>
    fetchMasterEntities: (externalIds: number[]) => Promise<BoundEntityResponse[]>
    renderHeader: (sort: string, setSort: (sort: string) => void) => ReactNode
    renderRow: (entity: T) => ReactNode
    searchPlaceholder?: string
    createMasterEntity: (masterEntityData: BoundEntityResponse) => M
    pageSize?: number
}

export function RawEntityTable<T extends RawEntity<M>, M extends MasterEntity>({
    fetchEntities,
    fetchMasterEntities,
    renderHeader,
    renderRow,
    searchPlaceholder = "Search...",
    createMasterEntity,
    pageSize = 20
}: RawEntityTableProps<T, M>) {
    // Use the entity table hook for atomic loading of entities and their master entities
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
        reload
    } = useRawEntityTable<T, M>(
        fetchEntities,
        fetchMasterEntities,
        createMasterEntity,
        { pageSize }
    );

    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 RawEntityTable MOUNTED with props:', {
            searchPlaceholder,
            pageSize
        });
        return () => {
            console.log('🔧 RawEntityTable UNMOUNTED');
        };
    }, []);

    // Render logging
    console.log('🔧 RawEntityTable RENDER with props:', {
        searchPlaceholder,
        pageSize,
        loading,
        dataContentLength: data?.content?.length || 0,
        searchInput,
        sort
    });

    // Handle search key press
    const handleSearchKeyPress = useCallback((e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            applySearch();
        }
    }, [applySearch]);

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
                    onClick={reload}
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
