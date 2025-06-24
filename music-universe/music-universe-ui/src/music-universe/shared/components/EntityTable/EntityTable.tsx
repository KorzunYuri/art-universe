// hooks
import { useEffect, useState, useRef } from 'react'
import type { ReactNode } from 'react'
// types
import type { Page } from '@/music-universe/shared/types/page'
import type { Approvable } from '@/music-universe/shared/types/approvable'
import type { Bindable } from '@/music-universe/shared/types/bindable'
// hooks
import { PaginatedResource } from '@/music-universe/shared/hooks/PaginatedResource'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './EntityTable.module.scss'

interface EntityTableProps<T extends Approvable & Bindable> {
    fetchEntities: (params: {
        page: number
        size: number
        search?: string
        sort?: string
    }) => Promise<Page<T>>
    fetchBoundEntities: (externalIds: number[]) => Promise<any[]>
    renderHeader: (sort: string, setSort: (sort: string) => void) => ReactNode
    renderRow: (entity: T, onChange: (entity: T) => void) => ReactNode
    searchPlaceholder?: string
    mapBoundEntity: (entity: T, boundEntity: any) => T
    getBoundEntityId: (entity: any) => number
}

export function EntityTable<T extends Approvable & Bindable>({
    fetchEntities,
    fetchBoundEntities,
    renderHeader,
    renderRow,
    searchPlaceholder = "Search...",
    mapBoundEntity,
    getBoundEntityId
}: EntityTableProps<T>) {
    const {
        data,
        setData,
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
    } = PaginatedResource<T>(fetchEntities)

    const [loadingBoundEntities, setLoadingBoundEntities] = useState(false)
    const skipNextFetchRef = useRef(false);
    const previousContentRef = useRef<T[]>([]);

    const loadBoundEntities = async () => {
        if (!data || data.content.length === 0) return;
        
        setLoadingBoundEntities(true);
        try {
            // Extract all entity IDs
            const entityIds = data.content.map(entity => entity.id);
            
            // Fetch bound entities
            const boundEntities = await fetchBoundEntities(entityIds);
            
            // Update the entities with bound information
            const updatedContent = data.content.map(entity => {
                const boundEntity = boundEntities.find(be => getBoundEntityId(be) === entity.id);
                if (boundEntity) {
                    return mapBoundEntity(entity, boundEntity);
                }
                 return {
                    ...entity,
                    boundEntity: undefined
                };
            });
            
            setData({ ...data, content: updatedContent });
        } catch (error) {
            console.error('Error loading bound entities:', error);
        } finally {
            setLoadingBoundEntities(false);
        }
    };

    // Load bound entities whenever the entity data changes
    useEffect(() => {
        if (!data || data.content.length === 0) return;
        
        // Skip fetch if we just updated an entity through the UI
        if (skipNextFetchRef.current) {
            skipNextFetchRef.current = false;
            return;
        }
        
        // Skip fetch if only boundEntity property changed
        if (previousContentRef.current.length === data.content.length) {
            const onlyBoundEntityChanged = data.content.every((entity, index) => {
                const prevEntity = previousContentRef.current[index];
                // If IDs don't match, content has changed
                if (prevEntity.id !== entity.id) return false;
                
                // If any property other than boundEntity changed, content has changed
                const entityWithoutBound = { ...entity };
                const prevEntityWithoutBound = { ...prevEntity };
                delete entityWithoutBound.boundEntity;
                delete prevEntityWithoutBound.boundEntity;
                
                return JSON.stringify(entityWithoutBound) === JSON.stringify(prevEntityWithoutBound);
            });
            
            if (onlyBoundEntityChanged) {
                previousContentRef.current = [...data.content];
                return;
            }
        }
        
        previousContentRef.current = [...data.content];
        loadBoundEntities();
    }, [data?.content, setData]);

    const onSearchKeyDown = (key: string) => {
        if (key === 'Enter') {
            applySearch();
        }
    };

    const onEntityChanged = (updated: T) => {
        if (!data) return;
        
        // Set flag to skip next fetch when we update an entity through the UI
        skipNextFetchRef.current = true;
        
        const newContent = data.content.map(e => e.id === updated.id ? updated : e);
        setData({ ...data, content: newContent });
    };

    const handleRefresh = () => {
        reload();
        // reset flag to trigger bindings reload
        skipNextFetchRef.current = false;
        previousContentRef.current = [];
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
                <button onClick={handleRefresh} disabled={loading || loadingBoundEntities}>
                    Refresh
                </button>
                
                {(loading || loadingBoundEntities) && (
                    <div className={styles.loading}>Loading...</div>
                )}
            </div>

            {!loading && data && (
                <>
                    <div className={styles.tableContainer}>
                        {renderHeader(sort, setSort)}
                        {data.content.map((entity) => renderRow(entity, onEntityChanged))}
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
