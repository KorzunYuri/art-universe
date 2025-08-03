// types
import type { Category } from '@/music-universe/shared/types/entities.ts'
import type { CategorySearchParams } from '@/music-universe/music-data/api/music-data-categories'
import type { LookupEntity } from '@/music-universe/music-data/types/master-entities-lookup.ts'
// api
import { fetchCategories } from '@/music-universe/music-data/api/music-data-categories'
// components
import { MasterEntityTable } from '@/music-universe/shared/components'
import { CategoriesTableHeader } from '../CategoriesTableHeader'
import { CategoriesTableRow } from '../CategoriesTableRow'
// hooks
import { useState, useEffect, useCallback, useRef } from 'react'
// styles
import styles from './CategoriesTable.module.css'
import {batchLookupMasterEntities, lookupMasterEntities} from "@/music-universe/music-data/api/music-data-lookup.ts";

export const CategoriesTable = () => {
    // Preloaded lookup data for categories and dimensions
    const [preloadedCategories, setPreloadedCategories] = useState<{[name: string]: LookupEntity[]}>({});
    const [preloadedDimensions, setPreloadedDimensions] = useState<LookupEntity[]>([]);
    const [isInitialLoad, setIsInitialLoad] = useState(true);
    
    // Use ref to track if dimensions are loaded to prevent unnecessary reloads
    const dimensionsLoadedRef = useRef(false);
    
    // Atomic function to load all required data
    const loadAllData = useCallback(async () => {
        console.log('📊 Loading dimensions...');
        const dimensions = await lookupMasterEntities('dimension', '', 50);
        setPreloadedDimensions(dimensions);
        dimensionsLoadedRef.current = true;
    }, []);

    // Load data on component mount and when explicitly requested
    useEffect(() => {
        if (isInitialLoad) {
            console.log('🚀 Initial load triggered');
            loadAllData()
                .finally(() => setIsInitialLoad(false))
            ;
        }
    }, [isInitialLoad, loadAllData]);

    // Load parent categories using batch lookup when categories change
    const loadParentCategories = useCallback(async (categories: Category[]) => {
        // Extract unique parent category names
        const parentNames = categories
            .filter(category => category.parentName)
            .map(category => category.parentName as string)
            .filter((name, index, self) => self.indexOf(name) === index);
        
        if (parentNames.length === 0) {
            setPreloadedCategories({});
            return;
        }
        
        try {
            console.log('🔍 Batch looking up parent categories:', parentNames);
            const response = await batchLookupMasterEntities('category', parentNames);
            setPreloadedCategories(response.results);
        } catch (error) {
            console.error('❌ Error batch looking up parent categories:', error);
            setPreloadedCategories({});
        }
    }, []);

    // Enhanced load categories function that also loads parent categories
    const loadCategoriesWithParents = async (params: CategorySearchParams) => {
        try {
            const result = await fetchCategories(params);
            
            // Load parent categories using batch lookup
            loadParentCategories(result.content);
            
            return result;
        } catch (error) {
            console.error('❌ Error loading categories:', error);
            throw error;
        }
    }

    // Handle refresh - reload all data atomically
    const handleRefresh = useCallback(() => {
        console.log('🔄 Refresh triggered - reloading all data');
        loadAllData();
    }, [loadAllData]);

    return (
        <div className={styles.container}>
            <MasterEntityTable<Category>
                fetchEntities={loadCategoriesWithParents}
                renderHeader={(sort, setSort) => (
                    <CategoriesTableHeader sort={sort} setSort={setSort} />
                )}
                renderRow={(category) => (
                    <CategoriesTableRow 
                        key={category.id} 
                        entity={category}
                        preloadedCategories={preloadedCategories[category.parentName || ''] || []}
                        preloadedDimensions={preloadedDimensions}
                    />
                )}
                searchPlaceholder="Search category name..."
                onRefresh={handleRefresh}
            />
        </div>
    )
}
