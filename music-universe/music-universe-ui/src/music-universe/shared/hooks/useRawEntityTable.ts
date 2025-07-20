import { useState, useCallback, useEffect, useRef } from 'react';
import type { RawEntity, MasterEntity } from '@/music-universe/shared/types/entity-reference';
import type { Page } from '@/music-universe/shared/types/page';
import type { BoundEntityResponse } from '@/music-universe/shared/types/master';
import type { SearchParams } from '@/music-universe/shared/components/BaseEntityTable/BaseEntityTable';

interface UseEntityTableOptions {
    initialSearch?: string;
    initialSort?: string;
    pageSize?: number;
}

/**
 * Hook for handling entity table with atomic loading of entities and their master entities
 * 
 * @param fetchEntities Function to fetch raw entities
 * @param fetchMasterEntities Function to fetch master entities
 * @param createMasterEntity Function to create a master entity from API data
 * @param options Options for pagination and initial state
 * @returns Object with data, loading state, and functions to control the table
 */
export function useRawEntityTable<T extends RawEntity<M>, M extends MasterEntity>(
    fetchEntities: (params: SearchParams) => Promise<Page<T>>,
    fetchMasterEntities: (externalIds: number[]) => Promise<BoundEntityResponse[]>,
    createMasterEntity: (masterEntityData: BoundEntityResponse) => M,
    options?: UseEntityTableOptions
) {
    // State for table data and UI
    const [data, setData] = useState<Page<T> | null>(null);
    const [loading, setLoading] = useState(false);
    const [searchInput, setSearchInput] = useState(options?.initialSearch || '');
    const [search, setSearch] = useState(options?.initialSearch || '');
    const [sort, setSort] = useState(options?.initialSort || '');
    const [page, setPage] = useState(0);
    const size = options?.pageSize || 20;
    
    // Use a ref to track if we're already loading to prevent infinite loops
    const isLoadingRef = useRef(false);
    // Use a ref to track if we need to reload data
    const needsReloadRef = useRef(true);

    // Hook mount/unmount logging
    useEffect(() => {
        console.log('🔧 useRawEntityTable MOUNTED with options:', {
            initialSearch: options?.initialSearch,
            initialSort: options?.initialSort,
            pageSize: options?.pageSize
        });
        return () => {
            console.log('🔧 useRawEntityTable UNMOUNTED');
        };
    }, []);

    // Hook render logging
    console.log('🔧 useRawEntityTable RENDER with state:', {
        loading,
        dataContentLength: data?.content?.length || 0,
        searchInput,
        search,
        sort,
        page,
        size,
        isLoading: isLoadingRef.current,
        needsReload: needsReloadRef.current
    });

    /**
     * Load entities and their master entities in a single atomic operation
     */
    const loadEntities = useCallback(async () => {
        // Prevent concurrent loads and infinite loops
        if (isLoadingRef.current || !needsReloadRef.current) return;
        
        isLoadingRef.current = true;
        needsReloadRef.current = false;
        
        setLoading(true);
        try {
            // Step 1: Load raw entities
            const rawEntities = await fetchEntities({ page, size, search, sort });
            
            if (!rawEntities || !rawEntities.content || rawEntities.content.length === 0) {
                setData(rawEntities);
                return;
            }
            
            // Step 2: Extract entity IDs for master entity lookup
            const entityIds = rawEntities.content.map(entity => entity.id);
            
            // Step 3: Load master entities
            const masterEntities = await fetchMasterEntities(entityIds);
            
            // Step 4: Combine raw entities with their master entities
            const combinedEntities = rawEntities.content.map(rawEntity => {
                // Find the master entity for this entity using externalId
                const masterEntityData = masterEntities.find(me => me.externalId === rawEntity.id);
                
                // Work directly with the original class instance to preserve all methods
                if (masterEntityData) {
                    // Create a master entity from the API data
                    const masterEntity = createMasterEntity(masterEntityData);
                    
                    // Set the master entity using the setMasterEntity method
                    rawEntity.setMasterEntity(masterEntity);
                } else {
                    rawEntity.setMasterEntity(undefined);
                }
                
                return rawEntity;
            });
            
            // Step 5: Update state with combined data
            const newEntities = {
                ...rawEntities,
                content: combinedEntities
            };
            setData(newEntities);
            
        } catch (error) {
            console.error('Error loading entities:', error);
        } finally {
            setLoading(false);
            isLoadingRef.current = false;
        }
    }, [page, size, search, sort, fetchEntities, fetchMasterEntities, createMasterEntity]);

    // Load entities when dependencies change
    useEffect(() => {
        needsReloadRef.current = true;
        loadEntities();
    }, [page, size, search, sort]);

    /**
     * Apply search and reset to first page
     */
    const applySearch = useCallback(() => {
        if (searchInput !== search) {
            setSearch(searchInput);
            setPage(0);
        }
    }, [searchInput, search]);

    /**
     * Navigate to next page if available
     */
    const nextPage = useCallback(() => {
        if (data && data.pageable.pageNumber + 1 < data.totalPages) {
            setPage(data.pageable.pageNumber + 1);
        }
    }, [data]);

    /**
     * Navigate to previous page if available
     */
    const prevPage = useCallback(() => {
        if (data && data.pageable.pageNumber > 0) {
            setPage(data.pageable.pageNumber - 1);
        }
    }, [data]);

    /**
     * Force reload all data
     */
    const reload = useCallback(() => {
        needsReloadRef.current = true;
        loadEntities();
    }, [loadEntities]);

    return {
        data,
        loading,
        searchInput,
        setSearchInput,
        applySearch,
        sort,
        setSort,
        nextPage,
        prevPage,
        hasNextPage: data ? data.pageable.pageNumber + 1 < data.totalPages : false,
        hasPrevPage: data ? data.pageable.pageNumber > 0 : false,
        reload
    };
}
