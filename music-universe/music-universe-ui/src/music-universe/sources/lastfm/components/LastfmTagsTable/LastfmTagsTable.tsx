// hooks
import { useState, useEffect } from 'react'
// components
import {
    LastfmTagsTableHeader,
    LastfmTagsTableRow,
} from '@/music-universe/sources/lastfm/components'
// types
import type { LastfmTag } from '@/music-universe/sources/lastfm/types/lastfm-tag'
import type { LookupEntity } from '@/music-universe/shared/types/lookup'
import type { BoundEntityResponse } from '@/music-universe/shared/types/master'
import type { Category } from "@/music-universe/music-data/types/master-entities";
// api
import { fetchTags, type TagSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-tags'
import { fetchBoundCategories, batchLookupCategories } from '@/music-universe/music-data/api/music-data-categories'
// components
import { LastfmEntityTable } from '@/music-universe/sources/lastfm/components/LastfmEntityTable'
// utils
import { CategoryImpl } from '@/music-universe/music-data/types/master-entities'
// styles
import styles from './LastfmTagsTable.module.css'

interface LastfmTagsTableProps {
    initialSearch?: string;
}

export const LastfmTagsTable = ({ initialSearch = '' }: LastfmTagsTableProps) => {
    // Preloaded lookup data for categories
    const [preloadedLookupData, setPreloadedLookupData] = useState<{[name: string]: LookupEntity[]}>({})
    const [initialSearchApplied, setInitialSearchApplied] = useState(false)

    // Create Category from BoundEntityResponse
    const createCategory = (boundEntity: BoundEntityResponse): Category => {
        return new CategoryImpl(boundEntity.masterId, boundEntity.masterName);
    };

    // Load tags with search parameters
    const loadTags = async (params: TagSearchParams) => {
        try {
            // Apply initial search if provided and not yet applied
            if (initialSearch && !initialSearchApplied && !params.search) {
                params.search = initialSearch;
                setInitialSearchApplied(true);
            }
            
            const result = await fetchTags(params)
            
            // Perform batch lookup for tag names
            if (result && result.content.length > 0) {
                try {
                    // Collect tag names for batch lookup
                    const tagNames = result.content.map(tag => tag.name)
                    
                    // Perform batch lookup for all tags
                    const lookupResponse = await batchLookupCategories(tagNames)
                    
                    if (lookupResponse.success) {
                        setPreloadedLookupData(lookupResponse.data.results)
                    } else {
                        setPreloadedLookupData({})
                    }
                } catch (error) {
                    console.error('Error performing batch lookup:', error)
                    setPreloadedLookupData({})
                }
            }
            
            return result;
        } catch (error) {
            console.error('Error loading tags:', error)
            throw error;
        }
    }
    
    // Reset preloaded data when component unmounts
    useEffect(() => {
        return () => {
            setPreloadedLookupData({});
        };
    }, []);

    return (
        <div className={styles.container}>
            <LastfmEntityTable<LastfmTag, Category>
                fetchEntities={loadTags}
                fetchMasterEntities={(ids) => fetchBoundCategories('LASTFM', ids)}
                createMasterEntity={createCategory}
                renderHeader={(sort, setSort) => (
                    <LastfmTagsTableHeader sort={sort} setSort={setSort} />
                )}
                renderRow={(tag) => (
                    <LastfmTagsTableRow
                        key={tag.id}
                        entity={tag}
                        preloadedLookupData={preloadedLookupData[tag.name] || []}
                    />
                )}
                searchPlaceholder="Search tag name..."
            />
        </div>
    )
}
