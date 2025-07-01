// hooks
import { useEffect, useState } from 'react'
// components
import {
    LastfmTagsTableHeader,
    LastfmTagsTableRow,
} from '@/music-universe/sources/lastfm/components'
// types
import type { LastfmTag } from '@/music-universe/sources/lastfm/types/lastfm-tag'
import type { Page } from '@/music-universe/shared/types/page'
// api
import { fetchTags, type TagSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-tags'
import { fetchBoundCategories } from '@/music-universe/music-data/api/music-data-categories'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './LastfmTagsTable.module.css'

export const LastfmTagsTable = () => {
    // Search state
    const [searchName, setSearchName] = useState('')
    const [approvalStatuses, setApprovalStatuses] = useState<number[] | undefined>(undefined)
    
    // Data state
    const [data, setData] = useState<Page<LastfmTag> | null>(null)
    const [loading, setLoading] = useState(false)
    const [loadingBoundTags, setLoadingBoundTags] = useState(false)
    const [page, setPage] = useState(0)
    const [sort, setSort] = useState('name,asc')
    const pageSize = 20

    // Load tags with current search parameters and their bound entities
    const loadTags = async () => {
        setLoading(true)
        try {
            const params: TagSearchParams = {
                search: searchName || undefined,
                approvalStatuses: approvalStatuses,
                page,
                size: pageSize,
                sort
            }
            
            console.log('🔄 Loading tags...')
            const result = await fetchTags(params)
            
            // Load bound tags immediately in the same function to avoid useEffect cycles
            if (result && result.content.length > 0) {
                console.log('🔄 Loading bound tags immediately after tags load')
                setLoadingBoundTags(true)
                try {
                    // Extract all tag IDs
                    const tagIds = result.content.map(tag => tag.id)
                    console.log(`📋 Requesting bindings for ${tagIds.length} tags: ${tagIds.join(', ')}`)
                    
                    // Fetch bound tags from music-data API
                    const boundTags = await fetchBoundCategories('LASTFM', tagIds)
                    console.log(`✅ Bound tags loaded: ${boundTags.length} items`)

                    // Update the tags with bound information
                    const updatedContent = result.content.map(tag => {
                        const boundTag = boundTags.find(bt => bt.externalId === tag.id)
                        if (boundTag) {
                            return {
                                ...tag,
                                boundEntity: {
                                    referenceId: boundTag.referenceId,
                                    referenceName: boundTag.referenceName
                                }
                            }
                        }
                        // explicitly set boundEntity for non-bound tags
                        return {
                            ...tag,
                            boundEntity: undefined
                        }
                    })
                    
                    console.log('📝 Setting final data with bound tags')
                    setData({ ...result, content: updatedContent })
                } catch (error) {
                    console.error('❌ Error loading bound tags:', error)
                    // Set data without bound tags if binding fails
                    setData(result)
                } finally {
                    setLoadingBoundTags(false)
                }
            } else {
                // Set data even if no content
                setData(result)
            }
        } catch (error) {
            console.error('❌ Error loading tags:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load tags when search parameters or pagination changes
    useEffect(() => {
        console.log('🔄 Effect triggered: page or sort changed')
        loadTags()
    }, [page, sort])

    // Handle tag changes from child components
    const onTagChanged = (updated: LastfmTag) => {
        if (!data) return
        
        console.log(`🔄 Tag changed: (${updated.name})`)
        
        const newContent = data.content.map(t => t.id === updated.id ? updated : t)
        setData({ ...data, content: newContent })
    }

    // Handle search form submission
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        console.log('🔍 Search submitted')
        setPage(0) // Reset to first page when searching
        loadTags()
    }

    // Handle pagination
    const nextPage = () => {
        if (data && page + 1 < data.totalPages) {
            console.log('📄 Moving to next page')
            setPage(page + 1)
        }
    }

    const prevPage = () => {
        if (page > 0) {
            console.log('📄 Moving to previous page')
            setPage(page - 1)
        }
    }

    // Handle refresh button
    const handleRefresh = () => {
        console.log('🔄 Refresh button clicked')
        loadTags()
    }

    return (
        <div className={styles.container}>
            <form onSubmit={handleSearch} className={styles.searchForm}>
                <div className={styles.searchRow}>
                    <div className={`${styles.searchField} ${styles.nameField}`}>
                        <input
                            type="text"
                            value={searchName}
                            placeholder="Search tag name..."
                            onChange={(e) => setSearchName(e.target.value)}
                            className={commonStyles.muLabel}
                        />
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.statusField}`}>
                        <select 
                            value={approvalStatuses?.join(',') || ''}
                            onChange={(e) => {
                                const value = e.target.value
                                setApprovalStatuses(value ? value.split(',').map(Number) : undefined)
                            }}
                            className={commonStyles.muLabel}
                        >
                            <option value="">All statuses</option>
                            <option value="1">Pending</option>
                            <option value="2">Approved</option>
                            <option value="3">Declined</option>
                            <option value="4">Auto-approved</option>
                            <option value="2,4">Approved & Auto-approved</option>
                        </select>
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.bindingField}`}>
                        {/* Binding field is not searchable */}
                    </div>
                </div>
                
                <div className={styles.searchActions}>
                    <button type="submit" disabled={loading || loadingBoundTags}>
                        Search
                    </button>
                    <button type="button" onClick={handleRefresh} disabled={loading || loadingBoundTags}>
                        Refresh
                    </button>
                    
                    {(loading || loadingBoundTags) && (
                        <div className={styles.loading}>Loading...</div>
                    )}
                </div>
            </form>

            {!loading && data && (
                <>
                    <div className={styles.tableContainer}>
                        <LastfmTagsTableHeader
                            sort={sort}
                            setSort={setSort}
                        />
                        {data.content.map((tag) => (
                            <LastfmTagsTableRow
                                key={tag.id}
                                tag={tag}
                                onChange={onTagChanged}
                            />
                        ))}
                    </div>

                    <div className={styles.pagination}>
                        <button disabled={page === 0} onClick={prevPage}>
                            Previous
                        </button>
                        <span>
                            Page {page + 1} of {data.totalPages}
                        </span>
                        <button disabled={page + 1 >= data.totalPages} onClick={nextPage}>
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}
