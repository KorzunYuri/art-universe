// hooks
import { useState, useEffect } from 'react'
// components
import {
    LastfmTagsTableHeader,
    LastfmTagsTableRow,
} from '@/music-universe/sources/lastfm/components'
// types
import type { LastfmTag } from '@/music-universe/sources/lastfm/types/lastfm-tag'
import type { LookupEntity } from '@/music-universe/music-data/types/master-entities-lookup.ts'
import type {Category, MasterEntityType} from "@/music-universe/shared/types/entities.ts";
// api
import { type LastfmTagsPageSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-tags'
// components
import { LastfmEntityTable } from '@/music-universe/sources/lastfm/components/LastfmEntityTable'
// utils
import { CategoryImpl } from '@/music-universe/shared/types/entities.ts'
// styles
import styles from './LastfmTagsTable.module.css'
import {batchLookupMasterEntities} from "@/music-universe/music-data/api/music-data-commons.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import {
    type BoundEntityResponse,
    fetchBoundMasterEntities
} from "@/music-universe/music-data/api/music-data-binding.ts";

interface LastfmTagsTableProps {
    initialSearch?: string;
}

export const LastfmTagsTable = ({ initialSearch = '' }: LastfmTagsTableProps) => {

    const dataSource: DataSource = 'lastfm';
    const entityType: MasterEntityType = 'category';

    // Preloaded lookup data for categories
    const [preloadedLookupData, setPreloadedLookupData] = useState<{[name: string]: LookupEntity[]}>({})
    const [initialSearchApplied, setInitialSearchApplied] = useState(false)

    // Create Category from BoundEntityResponse
    const createCategory = (boundEntity: BoundEntityResponse): Category => {
        return new CategoryImpl(boundEntity.masterId, boundEntity.masterName);
    };

    // Load tags with search parameters
    const loadTags = async (params: LastfmTagsPageSearchParams) => {
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
                    const lookupResponse = await batchLookupMasterEntities('category', tagNames)
                    setPreloadedLookupData(lookupResponse.results || {})
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
                fetchMasterEntities={(ids) => fetchBoundMasterEntities(dataSource, entityType, ids)}
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
